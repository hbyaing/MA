"""Preserve audited legacy differences without changing active game resources."""
import json
import shutil
import zipfile
from pathlib import Path
from audit_legacy_migration import SOURCE, TARGET, digest

def main():
    inventory = TARGET / 'reports/legacy-migration-inventory.json'
    rows = json.loads(inventory.read_text(encoding='utf-8'))
    archive = TARGET / 'integration/legacy-wuxing-20261024'
    archive.mkdir(parents=True, exist_ok=True)
    excluded = {'.codex', 'deliverables', 'reports', 'exports', 'logs', 'mcp-debug', 'mcp-classes', 'output', 'xyz', 'META-INF', 'libs'}
    selected = [r for r in rows if r['state'] != 'identical' and r['path'].split('/')[0] not in excluded and not r['path'].startswith('.tmp') and not r['path'].endswith(('.class', '.zip'))]
    groups = {
        'authoring': [r for r in selected if r['path'].split('/')[0] in {'source-assets', 'reference_assets', 'tools', 'assets'}],
        'history': [r for r in selected if r['path'].split('/')[0] not in {'source-assets', 'reference_assets', 'tools', 'assets'}],
    }
    packages = []
    for name, entries in groups.items():
        package = archive / (name + '.zip')
        if package.exists():
            raise FileExistsError(package)
        with zipfile.ZipFile(package, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as z:
            for row in entries:
                source = SOURCE / row['path']
                if digest(source) != row['sha256']:
                    raise RuntimeError('Source changed: ' + row['path'])
                z.write(source, row['path'])
        with zipfile.ZipFile(package) as z:
            assert z.testzip() is None
            import hashlib
            for row in entries:
                assert hashlib.sha256(z.read(row['path'])).hexdigest() == row['sha256'], row['path']
        packages.append({'file': package.name, 'sha256': digest(package), 'files': len(entries), 'bytes': package.stat().st_size})
    shutil.copy2(inventory, archive / 'inventory.json')
    (archive / 'preservation.json').write_text(json.dumps({'source': str(SOURCE), 'baseline': 'bb5a2bb', 'packages': packages, 'preserved': selected, 'excluded_roots': sorted(excluded)}, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(packages, indent=2))

if __name__ == '__main__':
    main()
