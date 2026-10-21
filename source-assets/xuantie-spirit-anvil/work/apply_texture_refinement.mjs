import fs from 'node:fs/promises';
import {connect,call,getText} from '../../spirit-eater-beetle/work/mcp.mjs';
const dir='E:/五行仙途/source-assets/xuantie-spirit-anvil';
const data=JSON.parse(await fs.readFile(dir+'/work/refined_uv.json','utf8'));
await connect();
console.log(getText(await call('risky_eval',{code:`(()=>{
if(Project.name!=='玄铁灵砧')throw Error('Wrong active project');
const iron=Texture.all.find(t=>t.name==='玄铁灵砧_锻造图集'),glow=Texture.all.find(t=>t.name==='玄铁灵砧_灵光');
if(!iron||!glow)throw Error('Missing model textures');
Undo.initEdit({elements:Cube.all.slice(),textures:[iron,glow],bitmap:true,uv_mode:true});
iron.fromPath('${dir}/xuantie_anvil_atlas.png');iron.name='玄铁灵砧_锻造图集';
glow.fromPath('${dir}/xuantie_anvil_glow.png');glow.name='玄铁灵砧_灵光';
return {stage:'textures loaded from disk',cubes:Cube.all.length};})()`})));
await new Promise(r=>setTimeout(r,500));
for(let offset=0;offset<data.assignments.length;offset+=40){
const chunk=data.assignments.slice(offset,offset+40);
console.log(getText(await call('risky_eval',{code:`(()=>{
const iron=Texture.all.find(t=>t.name==='玄铁灵砧_锻造图集'),glow=Texture.all.find(t=>t.name==='玄铁灵砧_灵光');
const chunk=${JSON.stringify(chunk)};
for(const a of chunk){const c=Cube.all.find(c=>c.uuid===a.uuid);if(!c)throw Error('Missing cube '+a.uuid);for(const [f,d] of Object.entries(a.faces)){c.faces[f].uv=d.uv.slice();c.faces[f].texture=(d.glow?glow:iron).uuid;}}
return {updated:${offset+chunk.length}};})()`})));
await new Promise(r=>setTimeout(r,80));
}
console.log(getText(await call('risky_eval',{code:`(()=>{
Project.texture_width=${data.resolution[0]};Project.texture_height=${data.resolution[1]};
for(const t of Texture.all){t.uv_width=${data.resolution[0]};t.uv_height=${data.resolution[1]};t.updateMaterial();}
Canvas.updateAll();Undo.finishEdit('细化玄铁锻痕、铜饰工艺及灵晶切面');
return {finished:true,cubes:Cube.all.length};})()`})));
