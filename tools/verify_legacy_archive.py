"""Verify archived bytes after an independent clone; does not extract or execute."""
import argparse
import hashlib
import json
from pathlib import Path
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument('directory', type=Path, nargs='?', default=Path(__file__).resolve().parents[1] / 'integration/legacy-wuxing-20261024')
args = parser.parse_args()
manifest = json.loads((args.directory / 'preservation.json').read_text(encoding='utf-8'))
expected = {r['path']: r['sha256'] for r in manifest['preserved']}
verified = set()
for package in manifest['packages']:
    path = args.directory / package['file']
    assert hashlib.sha256(path.read_bytes()).hexdigest() == package['sha256'], path
    with zipfile.ZipFile(path) as archive:
        assert archive.testzip() is None, path
        assert len(archive.namelist()) == package['files'], path
        for name in archive.namelist():
            assert name not in verified, name
            assert hashlib.sha256(archive.read(name)).hexdigest() == expected[name], name
            verified.add(name)
assert verified == expected.keys()
print(f'PASS: {len(verified)} archived files and all package hashes verified')
