#!/usr/bin/env node
// Sincroniza a coleção do Bruno (bruno/) com o OpenAPI.
//
// 1. Acrescenta as requests do OpenAPI que ainda não existem na coleção.
// 2. Nas que já existem, mescla por campo só o que vem do contrato: o corpo JSON (campos novos
//    entram com o valor padrão, os que sumiram saem, os que continuam mantêm o valor que está na
//    coleção) e os parâmetros de query (mesma regra). Nome, docs, testes, scripts, auth e exemplos
//    não são tocados.
// Nunca apaga uma request (a que sumiu do OpenAPI só é avisada) e não mexe em environments/.
// Uma request é considerada a mesma pelo método + URL, e não pelo nome do arquivo.
//
// Uso: node scripts/bruno-sync.mjs --generated <pasta gerada pelo bru import> [--target bruno] [--check]
//   --check  não grava nada; lista o que faltaria ou estaria desatualizado e sai com código 1 se houver

import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import { fileURLToPath } from 'node:url';
import { isDeepStrictEqual } from 'node:util';

const REQUEST = /^(get|post|put|delete|patch|options|head)\s*\{[^}]*?url:\s*(.+)$/im;
const SEQ = /^(\s*seq:\s*)(\d+)/m;
const NOT_REQUESTS = new Set(['folder.bru', 'collection.bru']);
const METHOD_BLOCK = /^(?:get|post|put|delete|patch|options|head) \{\n[\s\S]*?^\}$/m;

// Bloco de primeiro nível (começa e termina na coluna 0); os que ficam dentro de "example" são indentados
const block = (name) => new RegExp(`^${name} \\{\\n([\\s\\S]*?)^\\}$`, 'm');

function listRequests(dir) {
    const requests = [];
    const walk = (current) => {
        for (const entry of fs.readdirSync(current, { withFileTypes: true })) {
            const full = path.join(current, entry.name);
            if (entry.isDirectory()) {
                if (entry.name !== 'environments') {
                    walk(full);
                }
            } else if (entry.name.endsWith('.bru') && !NOT_REQUESTS.has(entry.name)) {
                const text = fs.readFileSync(full, 'utf8');
                const match = REQUEST.exec(text);
                if (match) {
                    requests.push({
                        file: path.relative(dir, full).split(path.sep).join('/'),
                        key: `${match[1].toUpperCase()} ${match[2].trim()}`,
                        seq: Number(SEQ.exec(text)?.[2] ?? 0),
                        text,
                    });
                }
            }
        }
    };
    walk(dir);
    return requests;
}

const isObject = (value) => value !== null && typeof value === 'object' && !Array.isArray(value);

// Campos do contrato, na ordem dele; do valor atual só se mantém o que continua existindo
function mergeJson(current, contract) {
    if (!isObject(current) || !isObject(contract)) {
        return current;
    }
    const merged = {};
    for (const [key, value] of Object.entries(contract)) {
        merged[key] = key in current ? mergeJson(current[key], value) : value;
    }
    return merged;
}

function parseJson(inner) {
    try {
        return JSON.parse(inner.replace(/^ {2}/gm, ''));
    } catch {
        return undefined;
    }
}

function insertAfterMethod(text, extra) {
    const method = METHOD_BLOCK.exec(text)[0];
    return text.replace(method, () => `${method}\n\n${extra}`);
}

function mergeBody(text, wanted) {
    const contract = block('body:json').exec(wanted);
    if (!contract) {
        return text;
    }
    const current = block('body:json').exec(text);
    if (!current) {
        const method = METHOD_BLOCK.exec(text)[0];
        const json = method.replace(/^([ \t]*body:[ \t]*)\w+/m, '$1json');
        return text.replace(method, () => `${json}\n\n${contract[0]}`);
    }
    const currentValue = parseJson(current[1]);
    const contractValue = parseJson(contract[1]);
    if (currentValue === undefined || contractValue === undefined) {
        return text;
    }
    const merged = mergeJson(currentValue, contractValue);
    if (isDeepStrictEqual(merged, currentValue)) {
        return text;
    }
    const inner = JSON.stringify(merged, null, 2).replace(/^/gm, '  ');
    return text.replace(current[0], () => `body:json {\n${inner}\n}`);
}

function parseParams(inner) {
    return inner
        .split('\n')
        .map((line) => ({ line, match: /^\s*~?([^:\s]+)\s*:/.exec(line) }))
        .filter((param) => param.match)
        .map((param) => ({ line: param.line, name: param.match[1] }));
}

function mergeQuery(text, wanted) {
    const contract = block('params:query').exec(wanted);
    const current = block('params:query').exec(text);
    const wantedParams = contract ? parseParams(contract[1]) : [];
    const currentParams = current ? parseParams(current[1]) : [];
    const wantedNames = new Set(wantedParams.map((p) => p.name));
    const kept = currentParams.filter((p) => wantedNames.has(p.name));
    const keptNames = new Set(kept.map((p) => p.name));
    const lines = [...kept, ...wantedParams.filter((p) => !keptNames.has(p.name))].map((p) => p.line);
    if (isDeepStrictEqual(lines, currentParams.map((p) => p.line))) {
        return text;
    }
    const merged = lines.length > 0 ? `params:query {\n${lines.join('\n')}\n}` : '';
    if (!current) {
        return insertAfterMethod(text, merged);
    }
    if (merged) {
        return text.replace(current[0], () => merged);
    }
    // Sem parâmetros no contrato: tira o bloco junto com a linha em branco que o separa dos vizinhos
    for (const withGap of [`${current[0]}\n\n`, `\n\n${current[0]}`, current[0]]) {
        if (text.includes(withGap)) {
            return text.replace(withGap, '');
        }
    }
    return text;
}

function uniqueFile(target, file) {
    const { dir, name, ext } = path.posix.parse(file);
    let candidate = file;
    for (let n = 2; fs.existsSync(path.join(target, candidate)); n++) {
        candidate = path.posix.join(dir, `${name}-${n}${ext}`);
    }
    return candidate;
}

export function sync({ generated, target, check = false }) {
    const existing = listRequests(target);
    const known = new Set(existing.map((r) => r.key));
    const byKey = new Map();
    const lastSeq = new Map();
    for (const r of existing) {
        if (!byKey.has(r.key)) {
            byKey.set(r.key, r);
        }
        const folder = path.posix.dirname(r.file);
        lastSeq.set(folder, Math.max(lastSeq.get(folder) ?? 0, r.seq));
    }

    const wanted = listRequests(generated).sort((a, b) => a.seq - b.seq);
    const added = [];
    const updated = [];
    for (const r of wanted) {
        const current = byKey.get(r.key);
        if (current) {
            byKey.delete(r.key);
            const withBody = mergeBody(current.text, r.text);
            const withQuery = mergeQuery(withBody, r.text);
            if (withQuery !== current.text) {
                const what = [];
                if (withBody !== current.text) {
                    what.push('corpo');
                }
                if (withQuery !== withBody) {
                    what.push('query');
                }
                updated.push({ file: current.file, key: r.key, what });
                if (!check) {
                    fs.writeFileSync(path.join(target, current.file), withQuery);
                }
            }
            continue;
        }
        if (known.has(r.key)) {
            continue;
        }
        known.add(r.key);
        const folder = path.posix.dirname(r.file);
        const seq = (lastSeq.get(folder) ?? 0) + 1;
        lastSeq.set(folder, seq);
        const file = uniqueFile(target, r.file);
        added.push({ file, key: r.key });
        if (!check) {
            const folderFile = path.join(target, folder, 'folder.bru');
            const generatedFolderFile = path.join(generated, folder, 'folder.bru');
            fs.mkdirSync(path.join(target, folder), { recursive: true });
            if (!fs.existsSync(folderFile) && fs.existsSync(generatedFolderFile)) {
                fs.copyFileSync(generatedFolderFile, folderFile);
            }
            fs.writeFileSync(path.join(target, file), r.text.replace(SEQ, `$1${seq}`));
        }
    }

    const wantedKeys = new Set(wanted.map((r) => r.key));
    const orphans = existing.filter((r) => !wantedKeys.has(r.key));
    return { added, updated, orphans };
}

function main(argv) {
    const args = { target: 'bruno', check: false };
    for (let i = 0; i < argv.length; i++) {
        if (argv[i] === '--generated') {
            args.generated = argv[++i];
        } else if (argv[i] === '--target') {
            args.target = argv[++i];
        } else if (argv[i] === '--check') {
            args.check = true;
        } else {
            console.error(`Argumento desconhecido: ${argv[i]}`);
            return 2;
        }
    }
    if (!args.generated || !fs.existsSync(args.generated) || !fs.existsSync(args.target)) {
        console.error('Uso: node scripts/bruno-sync.mjs --generated <pasta> [--target bruno] [--check]');
        console.error('As pastas informadas precisam existir.');
        return 2;
    }

    const { added, updated, orphans } = sync(args);
    for (const { file, key } of added) {
        console.log(`${args.check ? 'Falta' : 'Adicionada'}: ${file} (${key})`);
    }
    for (const { file, key, what } of updated) {
        console.log(`${args.check ? 'Desatualizada' : 'Atualizada'}: ${file} (${key}): ${what.join(' e ')}`);
    }
    for (const r of orphans) {
        console.log(`Aviso: ${r.file} (${r.key}) não existe mais no OpenAPI. Nada foi apagado.`);
    }
    if (added.length === 0 && updated.length === 0) {
        console.log('A coleção do Bruno já está em dia com o OpenAPI.');
    }
    return args.check && (added.length > 0 || updated.length > 0) ? 1 : 0;
}

if (path.resolve(process.argv[1] ?? '') === fileURLToPath(import.meta.url)) {
    process.exitCode = main(process.argv.slice(2));
}
