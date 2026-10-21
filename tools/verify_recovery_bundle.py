"""Actually clone a source recovery bundle in a new directory and verify its tracked trees.
Leaves the checkout intact as acceptance evidence; never deletes caller files.
"""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess

def git(*args):
    return subprocess.check_output(["git", *map(str, args)], text=True, encoding="utf-8").strip()

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("bundle", type=Path)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--branch", required=True)
    parser.add_argument("--expected-head", required=True)
    parser.add_argument("--report", type=Path, required=True)
    args = parser.parse_args()
    source = args.bundle.resolve(strict=True)
    target = args.directory.resolve()
    if target.exists() and any(target.iterdir()):
        raise SystemExit("Recovery verification requires a new or empty directory")
    target.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["git", "clone", "--branch", args.branch, str(source), str(target)], check=True)
    subprocess.run(["git", "-C", str(target), "fsck", "--full", "--no-reflogs", "--no-dangling"], check=True)
    actual = git("-C", target, "rev-parse", "HEAD")
    if actual != args.expected_head:
        raise SystemExit("Restored HEAD differs from the frozen acceptance commit")
    if git("-C", target, "status", "--porcelain"):
        raise SystemExit("Fresh recovery checkout is unexpectedly dirty")
    baseline = json.loads((target / "docs/integration/acceptance-baseline.json").read_text(encoding="utf-8"))
    checked = []
    for item in baseline["branches"]:
        tree = git("-C", target, "rev-parse", "refs/remotes/origin/" + item["branch"] + "^{tree}")
        if tree != item["sourceTree"]:
            raise SystemExit("Recovered source tree mismatch: " + item["branch"])
        checked.append({"branch": item["branch"], "tree": tree, "matchesOriginalTree": True})
    digest = hashlib.sha256()
    with source.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    report = {"passed": True, "independentClone": True, "fsckFull": True, "workingTreeClean": True,
              "head": actual, "branch": args.branch, "bundleSHA256": digest.hexdigest(),
              "sourceBranches": checked}
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))

if __name__ == "__main__":
    main()
