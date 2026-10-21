"""Fail closed unless this run actually completed Forge GameTests successfully."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import sys

def result(text, exit_code):
    complete = re.findall(r"(?:=+\s*)?(\d+)\s+GAME TESTS COMPLETE", text)
    passed = re.findall(r"All\s+(\d+)\s+required tests passed", text, re.IGNORECASE)
    failed = re.findall(r"(\d+)\s+required tests failed", text, re.IGNORECASE)
    total = int(complete[-1]) if complete else 0
    required = int(passed[-1]) if passed else 0
    failures = max(map(int, failed), default=0)
    ok = exit_code == 0 and total > 0 and 0 < required <= total and failures == 0
    return {"passed": ok, "exitCode": exit_code, "completedTests": total,
            "passedRequiredTests": required, "failedRequiredTests": failures,
            "completionMarker": bool(complete), "passMarker": bool(passed)}

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("log", type=Path)
    parser.add_argument("--exit-code", required=True, type=int)
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    data = args.log.read_bytes()
    report = result(data.decode("utf-8", errors="replace"), args.exit_code)
    report["logSHA256"] = hashlib.sha256(data).hexdigest()
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))
    return 0 if report["passed"] else 1

if __name__ == "__main__":
    sys.exit(main())
