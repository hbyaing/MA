import pathlib,json,shutil,sys
from bb_rpc import evaluate
P=pathlib.Path(__file__).resolve().parent.parent
src=P/'玄水妖.bbmodel';backup=P/'work'/'上色版_白模审查前.bbmodel'
if not backup.exists():shutil.copyfile(src,backup)
m=json.loads(src.read_text(encoding='utf-8'));m['name']='玄水妖 · 白模审查';m['textures']=[]
for e in m['elements']:
    e['color']=0
    for f in e.get('faces',{}).values():f['texture']=False
m['view_mode']='solid'
dst=P/'玄水妖_白模.bbmodel';dst.write_text(json.dumps(m,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
code='(async()=>{if(Project?.name.startsWith("玄水妖")) await Project.close(true);Blockbench.read(['+json.dumps(str(dst))+'],{readtype:"text"},files=>{Codecs.project.load(JSON.parse(files[0].content),{path:files[0].path});Project.view_mode="solid";Canvas.updateAll();});return {name:Project.name,meshes:Mesh.all.length,textures:Texture.all.length};})()'
print(evaluate(code))
