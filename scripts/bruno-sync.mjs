#!/usr/bin/env node
// Acrescenta à coleção do Bruno (bruno/) as requests do OpenAPI que ainda não existem nela.
//
// Só acrescenta: nunca altera nem apaga uma request existente e não mexe em environments/.
// Uma request é considerada a mesma pelo método + URL, e não pelo nome do arquivo.
//
// Uso: node scripts/bruno-sync.mjs --generated <pasta gerada pelo bru import> [--target bruno] [--check]
//   --check  não grava nada; lista o que faltaria e sai com código 1 se faltar alguma request

import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import { fileURLToPath } from 'node:url';

const REQUEST = /^(get|post|put|delete|patch|options|head)\s*\{[^}]*?url:\s*(.+)$/im;
const SEQ = /^(\s*seq:\s*)(\d+)/m;
const NOT_REQUESTS = new Set(['folder.bru', 'collection.bru']);

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
    const lastSeq = new Map();
    for (const r of existing) {
        const folder = path.posix.dirname(r.file);
        lastSeq.set(folder, Math.max(lastSeq.get(folder) ?? 0, r.seq));
    }

    const wanted = listRequests(generated).sort((a, b) => a.seq - b.seq);
    const added = [];
    for (const r of wanted) {
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
    return { added, orphans };
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

    const { added, orphans } = sync(args);
    for (const { file, key } of added) {
        console.log(`${args.check ? 'Falta' : 'Adicionada'}: ${file} (${key})`);
    }
    for (const r of orphans) {
        console.log(`Aviso: ${r.file} (${r.key}) não existe mais no OpenAPI. Nada foi apagado.`);
    }
    if (added.length === 0) {
        console.log('A coleção do Bruno já tem todas as requests do OpenAPI.');
    }
    return args.check && added.length > 0 ? 1 : 0;
}

if (path.resolve(process.argv[1] ?? '') === fileURLToPath(import.meta.url)) {
    process.exitCode = main(process.argv.slice(2));
}
