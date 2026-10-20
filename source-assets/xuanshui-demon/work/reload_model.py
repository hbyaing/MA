from bb_rpc import evaluate
import json,pathlib
p=str(pathlib.Path(__file__).resolve().parent.parent/'玄水妖.bbmodel')
code='(async()=>{if(Project?.name === "玄水妖") await Project.close(true);Blockbench.read(['+json.dumps(p)+'],{readtype:"text"},files=>{Codecs.project.load(JSON.parse(files[0].content),{path:files[0].path});Canvas.updateAll();});return {name:Project.name,meshes:Mesh.all.length,groups:Group.all.length};})()'
print(evaluate(code))
