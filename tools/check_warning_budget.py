"""Count javac project warnings in a completed build log; Gradle notices are excluded."""
import re
import sys
from pathlib import Path

log = Path(sys.argv[1]).read_text(encoding='utf-8', errors='replace')
count = len(re.findall(r'^.*\.java:\d+:\s*(?:warning|警告)[:：]', log, re.MULTILINE))
# The current Forge 1.20.1 full compile emits 119 upstream deprecation warnings.
# Keep this explicit baseline so CI catches new warnings without treating the
# existing migration backlog as a build failure.
budget = 119
print(f'Project javac warnings: {count}/{budget}')
if count > budget:
    raise SystemExit('Warning budget exceeded; fix new warnings before release')
