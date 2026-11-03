import assert from 'node:assert/strict';
import { execFileSync, spawnSync } from 'node:child_process';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { afterEach, beforeEach, test } from 'node:test';
import { fileURLToPath } from 'node:url';

import { sync } from './bruno-sync.mjs';

const script = fileURLToPath(new URL('./bruno-sync.mjs', import.meta.url));
let tmp;
let target;
let generated;

function request(name, seq, method, url) {
    return `meta {\n  name: ${name}\n  type: http\n  seq: ${seq}\n}\n\n${method} {\n  url: {{baseUrl}}${url}\n  body: none\n  auth: inherit\n}\n`;
}

function write(dir, file, content) {
    const full = path.join(dir, file);
    fs.mkdirSync(path.dirname(full), { recursive: true });
    fs.writeFileSync(full, content);
}

function read(dir, file) {
    return fs.readFileSync(path.join(dir, file), 'utf8');
}

beforeEach(() => {
    tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'bruno-sync-'));
    target = path.join(tmp, 'bruno');
    generated = path.join(tmp, 'generated');
    write(target, 'environments/local.bru', 'vars {\n  baseUrl: http://127.0.0.1:8765\n}\n');
    write(target, 'profiles/folder.bru', 'meta {\n  name: profiles\n}\n');
    write(target, 'profiles/create.bru', request('create', 1, 'post', '/profiles'));
    write(generated, 'environments/local.bru', 'vars {\n  baseUrl: http://localhost:8765\n}\n');
    write(generated, 'profiles/folder.bru', 'meta {\n  name: profiles\n}\n');
    write(generated, 'profiles/createProfile.bru', request('createProfile', 2, 'post', '/profiles'));
    write(generated, 'profiles/getProfile.bru', request('getProfile', 1, 'get', '/profiles/:id'));
    write(generated, 'profiles/listProfiles.bru', request('listProfiles', 3, 'get', '/profiles'));
});

afterEach(() => {
    fs.rmSync(tmp, { recursive: true, force: true });
});

test('adds only the requests that are missing, after the last existing seq', () => {
    const { added } = sync({ generated, target });

    assert.deepEqual(added.map((a) => a.file), ['profiles/getProfile.bru', 'profiles/listProfiles.bru']);
    assert.match(read(target, 'profiles/getProfile.bru'), /seq: 2/);
    assert.match(read(target, 'profiles/listProfiles.bru'), /seq: 3/);
});

test('recognizes an existing request by method and URL, not by file name', () => {
    sync({ generated, target });

    assert.equal(fs.existsSync(path.join(target, 'profiles/createProfile.bru')), false);
    assert.equal(read(target, 'profiles/create.bru'), request('create', 1, 'post', '/profiles'));
});

test('never touches the environments folder', () => {
    sync({ generated, target });

    assert.equal(read(target, 'environments/local.bru'), 'vars {\n  baseUrl: http://127.0.0.1:8765\n}\n');
});

test('does nothing on a second run', () => {
    sync({ generated, target });

    assert.deepEqual(sync({ generated, target }).added, []);
});

test('check mode writes nothing', () => {
    const { added } = sync({ generated, target, check: true });

    assert.equal(added.length, 2);
    assert.equal(fs.existsSync(path.join(target, 'profiles/getProfile.bru')), false);
});

test('does not overwrite an existing file that has the same name but another request', () => {
    write(target, 'profiles/getProfile.bru', request('getProfile', 9, 'delete', '/profiles/:id'));

    sync({ generated, target });

    assert.equal(read(target, 'profiles/getProfile.bru'), request('getProfile', 9, 'delete', '/profiles/:id'));
    assert.match(read(target, 'profiles/getProfile-2.bru'), /get \{/);
});

test('copies folder.bru for a folder that does not exist yet', () => {
    write(generated, 'jobs/folder.bru', 'meta {\n  name: jobs\n}\n');
    write(generated, 'jobs/listJobs.bru', request('listJobs', 1, 'get', '/jobs'));

    sync({ generated, target });

    assert.equal(read(target, 'jobs/folder.bru'), 'meta {\n  name: jobs\n}\n');
    assert.match(read(target, 'jobs/listJobs.bru'), /seq: 1/);
});

test('reports requests that no longer exist in the OpenAPI without deleting them', () => {
    write(target, 'profiles/old.bru', request('old', 4, 'delete', '/old'));

    const { orphans } = sync({ generated, target });

    assert.deepEqual(orphans.map((o) => o.file), ['profiles/old.bru']);
    assert.equal(fs.existsSync(path.join(target, 'profiles/old.bru')), true);
});

test('command line: --check exits with 1 when something is missing and 0 when up to date', () => {
    const args = [script, '--generated', generated, '--target', target];
    const missing = spawnSync(process.execPath, [...args, '--check'], { encoding: 'utf8' });
    assert.equal(missing.status, 1);
    assert.match(missing.stdout, /Falta: profiles\/getProfile\.bru/);

    execFileSync(process.execPath, args);
    const upToDate = spawnSync(process.execPath, [...args, '--check'], { encoding: 'utf8' });
    assert.equal(upToDate.status, 0);
});

function requestWith(name, seq, method, url, { body, query } = {}) {
    let text = request(name, seq, method, url).replace('body: none', `body: ${body ? 'json' : 'none'}`);
    if (query) {
        text += `\nparams:query {\n${query.map((line) => `  ${line}`).join('\n')}\n}\n`;
    }
    if (body) {
        text += `\nbody:json {\n${JSON.stringify(body, null, 2).replace(/^/gm, '  ')}\n}\n`;
    }
    return text;
}

function bodyOf(text) {
    const match = /^body:json \{\n([\s\S]*?)^\}$/m.exec(text);
    return JSON.parse(match[1].replace(/^ {2}/gm, ''));
}

function queryOf(text) {
    const match = /^params:query \{\n([\s\S]*?)^\}$/m.exec(text);
    return match ? match[1].split('\n').filter(Boolean).map((line) => line.trim()) : [];
}

test('update: merges the JSON body by field, keeping the values already in the collection', () => {
    write(target, 'profiles/create.bru', requestWith('create', 1, 'post', '/profiles', {
        body: { name: 'Java Backend', old: 1, filters: { level: 'JUNIOR', gone: true }, tags: ['a'] },
    }));
    write(generated, 'profiles/createProfile.bru', requestWith('createProfile', 2, 'post', '/profiles', {
        body: { userId: 0, name: '', filters: { level: 'INTERN', area: 'QA' }, tags: [] },
    }));

    const { updated } = sync({ generated, target });

    assert.deepEqual(updated, [{ file: 'profiles/create.bru', key: 'POST {{baseUrl}}/profiles', what: ['corpo'] }]);
    assert.deepEqual(bodyOf(read(target, 'profiles/create.bru')), {
        userId: 0,
        name: 'Java Backend',
        filters: { level: 'JUNIOR', area: 'QA' },
        tags: ['a'],
    });
});

test('update: adds the JSON body when the request had none', () => {
    write(generated, 'profiles/createProfile.bru', requestWith('createProfile', 2, 'post', '/profiles', {
        body: { name: '' },
    }));

    sync({ generated, target });

    const text = read(target, 'profiles/create.bru');
    assert.match(text, /body: json/);
    assert.deepEqual(bodyOf(text), { name: '' });
});

test('update: merges the query params, keeping values and disabled marks', () => {
    write(target, 'profiles/list.bru', requestWith('list', 1, 'get', '/profiles', { query: ['~userId: 5', 'old: x'] }));
    write(generated, 'profiles/listProfiles.bru', requestWith('listProfiles', 3, 'get', '/profiles', {
        query: ['userId: ', 'page: '],
    }));
    write(target, 'profiles/create.bru', request('create', 2, 'post', '/profiles/other'));

    const { updated } = sync({ generated, target });

    const list = updated.find((u) => u.file === 'profiles/list.bru');
    assert.deepEqual(list.what, ['query']);
    assert.deepEqual(queryOf(read(target, 'profiles/list.bru')), ['~userId: 5', 'page:']);
});

test('update: adds a query block when the request had none and drops it when the contract has none', () => {
    write(target, 'profiles/list.bru', requestWith('list', 1, 'get', '/profiles', { query: ['old: x'] }));
    write(target, 'profiles/other.bru', request('other', 2, 'get', '/other'));
    write(generated, 'profiles/listProfiles.bru', requestWith('listProfiles', 3, 'get', '/profiles'));
    write(generated, 'profiles/other.bru', requestWith('other', 4, 'get', '/other', { query: ['q: '] }));

    sync({ generated, target });

    assert.deepEqual(queryOf(read(target, 'profiles/list.bru')), []);
    assert.doesNotMatch(read(target, 'profiles/list.bru'), /params:query/);
    assert.deepEqual(queryOf(read(target, 'profiles/other.bru')), ['q:']);
});

test('update: leaves a body that is not valid JSON untouched', () => {
    const custom = request('create', 1, 'post', '/profiles').replace('body: none', 'body: json')
        + '\nbody:json {\n  {\n    "name": {{profileName}}\n  }\n}\n';
    write(target, 'profiles/create.bru', custom);
    write(generated, 'profiles/createProfile.bru', requestWith('createProfile', 2, 'post', '/profiles', {
        body: { userId: 0, name: '' },
    }));

    sync({ generated, target });

    assert.equal(read(target, 'profiles/create.bru'), custom);
});

test('update: does not touch the name, tags or examples of an existing request', () => {
    const extra = '\nexample {\n  name: 200 Response\n  request: {\n    params:query: {\n      old: x\n    }\n  }\n}\n';
    write(target, 'profiles/create.bru', requestWith('my own name', 1, 'post', '/profiles', { body: { name: 'x' } }) + extra);
    write(generated, 'profiles/createProfile.bru', requestWith('createProfile', 2, 'post', '/profiles', {
        body: { name: '', userId: 0 },
    }));

    sync({ generated, target });

    const text = read(target, 'profiles/create.bru');
    assert.match(text, /name: my own name/);
    assert.ok(text.endsWith(extra));
});

test('update: is idempotent and --check reports without writing', () => {
    write(target, 'profiles/create.bru', requestWith('create', 1, 'post', '/profiles', { body: { name: 'x' } }));
    write(generated, 'profiles/createProfile.bru', requestWith('createProfile', 2, 'post', '/profiles', {
        body: { name: '', userId: 0 },
    }));
    const before = read(target, 'profiles/create.bru');

    assert.equal(sync({ generated, target, check: true }).updated.length, 1);
    assert.equal(read(target, 'profiles/create.bru'), before);

    sync({ generated, target });
    assert.deepEqual(sync({ generated, target }).updated, []);
});

test('command line: --check exits with 1 when an existing request is outdated', () => {
    write(target, 'profiles/create.bru', requestWith('create', 1, 'post', '/profiles', { body: { name: 'x' } }));
    write(generated, 'profiles/createProfile.bru', requestWith('createProfile', 2, 'post', '/profiles', {
        body: { name: '', userId: 0 },
    }));
    const args = [script, '--generated', generated, '--target', target];
    execFileSync(process.execPath, args);

    const upToDate = spawnSync(process.execPath, [...args, '--check'], { encoding: 'utf8' });
    assert.equal(upToDate.status, 0);

    write(target, 'profiles/create.bru', requestWith('create', 1, 'post', '/profiles', { body: { name: 'x' } }));
    const stale = spawnSync(process.execPath, [...args, '--check'], { encoding: 'utf8' });
    assert.equal(stale.status, 1);
    assert.match(stale.stdout, /Desatualizada: profiles\/create\.bru/);
});
