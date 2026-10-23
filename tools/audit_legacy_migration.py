"""Read-only SHA-256 inventory of the retired local project's durable files."""
import hashlib
import json
import os
from pathlib import Path

SOURCE = Path('E:/五行仙途')
TARGET = Path(__file__).resolve().parents[1]
SKIP = {'.git', '.gradle', '.gradle-local', '.gradle-tmp', 'build', '__pycache__'}

def digest(path):
    h = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()

def main():
    rows = []
    paths = []
    for directory, subdirs, files in os.walk(SOURCE):
        subdirs[:] = [name for name in subdirs if name not in SKIP]
        paths.extend(Path(directory) / name for name in files)
    for path in sorted(paths):
        relative = path.relative_to(SOURCE)
        if any(part in SKIP for part in relative.parts) or not path.is_file():
            continue
        dest = TARGET / relative
        sha = digest(path)
        state = 'missing' if not dest.is_file() else ('identical' if digest(dest) == sha else 'different')
        rows.append({'path': relative.as_posix(), 'bytes': path.stat().st_size, 'sha256': sha, 'state': state})
    output = TARGET / 'reports/legacy-migration-inventory.json'
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding='utf-8')
    from collections import Counter
    print(json.dumps(dict(Counter(r['state'] for r in rows)), ensure_ascii=False))
    print(json.dumps(dict(Counter(r['path'].split('/')[0] + ':' + r['state'] for r in rows if r['state'] != 'identical')), ensure_ascii=False, indent=2))

if __name__ == '__main__':
    main()
