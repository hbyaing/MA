"""Structural validation for the independent formation reserve library."""
from __future__ import annotations
import argparse,hashlib,json,sys,zipfile
from pathlib import Path
from PIL import Image
ROOT=Path(__file__).resolve().parents[2]
def main(out):
 out=Path(out);cat=json.loads((out/'catalog/materials.json').read_text('utf-8'));fs=json.loads((out/'catalog/formations.json').read_text('utf-8'));manifest=json.loads((out/'manifest.json').read_text('utf-8'))
 assert len(cat)>=54 and len(fs)==36 and len(manifest['files'])>0
 assert len({m['id'] for m in cat})==len(cat) and len({f['id'] for f in fs})==36
 fam={f['family'] for f in fs};assert len(fam)==9
 for m in cat:
  p=out/m['file']; im=Image.open(p).convert('RGBA');assert im.size==(256,256);assert im.getbbox() is not None; assert max(a for *_,a in im.getdata())>0
  if m.get('sheetFile'): assert (out/m['sheetFile']).exists() and m.get('frames')==16
  assert (out/m['runtimeFile']).exists()
 valid={m['id'] for m in cat};layers=0
 for f in fs:
  assert f['radius']>0 and f['durationTicks']>=80
  assert all(x['role'] in {'core','rune','edge','node','spark','shard'} for x in f['layers'])
  assert all(f"{f['family']}_{x['role']}_f1" in valid for x in f['layers']);layers+=len(f['layers'])
 recipes=list((out/'recipes').glob('*.json'));assert len(recipes)==36
 fx=list((out/'resourcepack/assets/wuxingxiantu_vfx/fx/formation_v1').glob('*.fx'));proj=list((out/'photon/projects').glob('*.fxproj'));assert len(fx)==len(cat)+36 and len(proj)==len(cat)+36
 for rel,digest in manifest['files'].items():
  p=out/rel;assert p.exists(),rel;assert hashlib.sha256(p.read_bytes()).hexdigest()==digest,rel
 print(json.dumps({'passed':True,'families':len(fam),'formations':len(fs),'materials':len(cat),'layers':layers,'recipes':len(recipes),'nativeFx':len(fx),'nativeProjects':len(proj),'filesHashed':len(manifest['files'])},ensure_ascii=False))
if __name__=='__main__':
 ap=argparse.ArgumentParser();ap.add_argument('--out',type=Path,default=ROOT/'source-assets/formation-particle-library-v1');main(ap.parse_args().out)
