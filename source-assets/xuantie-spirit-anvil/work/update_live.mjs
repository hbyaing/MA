import fs from 'node:fs/promises';
import {connect,call,getText} from '../../spirit-eater-beetle/work/mcp.mjs';
const dir='E:/五行仙途/source-assets/xuantie-spirit-anvil';
const geom=JSON.parse(await fs.readFile(dir+'/work/geometry.json','utf8'));
const atlas='data:image/png;base64,'+(await fs.readFile(dir+'/xuantie_anvil_atlas.png')).toString('base64');
const glow='data:image/png;base64,'+(await fs.readFile(dir+'/xuantie_anvil_glow.png')).toString('base64');
await connect();
console.log(getText(await call('risky_eval',{code:`(async()=>{
if(Project.name!=='玄铁灵砧')throw Error('Wrong active project');
const data=${JSON.stringify(geom)};
const existing=Cube.all.slice(),edited=existing.slice();
const iron=Texture.all.find(t=>t.name==='玄铁灵砧_锻造图集'),glow=Texture.all.find(t=>t.name==='玄铁灵砧_灵光');
Undo.initEdit({elements:edited,outliner:true,textures:[iron,glow],bitmap:true});
iron.fromDataURL(${JSON.stringify(atlas).replaceAll('/', '\\u002f')});glow.fromDataURL(${JSON.stringify(glow).replaceAll('/', '\\u002f')});
Project.texture_width=data.texture_size[0];Project.texture_height=data.texture_size[1];
for(const t of [iron,glow]){t.uv_width=data.texture_size[0];t.uv_height=data.texture_size[1];t.updateMaterial();}
const duplicate=new Set();
for(const p of data.parts){
 const key=p.group+'::'+p.name;
 const c=existing.find(c=>c.name===p.name&&c.parent.name===p.group&&!duplicate.has(c.uuid))||new Cube({name:p.name,from:p.from,to:p.to,origin:[0,0,0],box_uv:false,autouv:0}).addTo(Group.all.find(g=>g.name===p.group)).init();
 duplicate.add(c.uuid);if(!existing.includes(c))edited.push(c);
 c.from=p.from.slice();c.to=p.to.slice();
 for(const [f,d] of Object.entries(p.faces)){c.faces[f].uv=d.uv.slice();c.faces[f].texture=(d.glow?glow:iron).uuid;}
 c.shade=!p.glow;
}
Canvas.updateAll();Undo.finishEdit('精修玄铁材质与四面灵纹');
return {cubes:Cube.all.length,textures:Texture.all.length};})()`})));
await new Promise(r=>setTimeout(r,250));
console.log('Model updated');
