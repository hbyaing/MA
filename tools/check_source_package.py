"""Run required heart-sword checks in an isolated source fixture with no reports/build/cache."""
import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]
CONTRACT = Path("source-assets/contracts/heart_sword_v2_import.json")

def main():
    contract = json.loads((ROOT / CONTRACT).read_text(encoding="utf-8"))
    files = {CONTRACT, Path("tools/check_heart_sword_assets.py"), Path(contract["source"]),
             Path("src/main/resources/assets/wuxingxiantu/models/item/heart_sword.json"),
             Path("src/main/resources/assets/wuxingxiantu/textures/item/heart_sword_inventory.png")}
    files.update(Path(path) for path in contract["output_sha256"])
    files.update(Path(material["path"]) for material in contract["materials"])
    with tempfile.TemporaryDirectory(prefix="wuxing-source-contract-") as temp:
        isolated = Path(temp)
        for relative in files:
            source = (ROOT / relative).resolve()
            if not source.is_relative_to(ROOT) or source.parts[len(ROOT.parts)] in {"reports", "build", "deliverables"}:
                raise RuntimeError(f"Contract references non-source input: {relative}")
            destination = isolated / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, destination)
        subprocess.run([sys.executable, "tools/check_heart_sword_assets.py"], cwd=isolated, check=True)
    print(f"Source-only heart-sword contract: PASS ({len(files)} inputs, no reports/build)")

if __name__ == "__main__":
    main()
