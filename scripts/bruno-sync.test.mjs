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
