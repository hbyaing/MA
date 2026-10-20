import fs from 'node:fs/promises';
import {connect,call,getText} from '../../spirit-eater-beetle/work/mcp.mjs';
const dir='E:/五行仙途/source-assets/xuantie-spirit-anvil/reference-textured';
const data=JSON.parse(await fs.readFile(dir+'/reference_uv.json','utf8'));
await connect();
await call('risky_eval',{code:`(()=>{if(Project.name!=='玄铁灵砧_原图贴图')throw Error('Wrong project');const t=Texture.all[0];Undo.initEdit({elements:Cube.all.slice(),textures:[t],bitmap:true});t.fromPath('${dir}/reference_preview.png');t.name='玄铁灵砧_参考图提取';return true;})()`});
await new Promise(r=>setTimeout(r,300));
for(let offset=0;offset<data.assignments.length;offset+=40){
const chunk=data.assignments.slice(offset,offset+40);
await call('risky_eval',{code:`(()=>{const data=${JSON.stringify(chunk)};for(const a of data){const c=Cube.all.find(c=>c.uuid===a.uuid);if(!c)throw Error('Missing cube');for(const [f,d] of Object.entries(a.faces)){c.faces[f].uv=d.uv;c.faces[f].texture=Texture.all[0].uuid;}}return ${offset+chunk.length};})()`});
await new Promise(r=>setTimeout(r,60));
}
console.log(getText(await call('risky_eval',{code:`(()=>{Project.texture_width=${data.resolution[0]};Project.texture_height=${data.resolution[1]};Texture.all[0].uv_width=${data.resolution[0]};Texture.all[0].uv_height=${data.resolution[1]};Canvas.updateAll();Undo.finishEdit('校正原图顶面与包角投射');return {finished:true,cubes:Cube.all.length};})()`})));
