import fs from 'node:fs/promises';
import {connect,call,getText} from '../../spirit-eater-beetle/work/mcp.mjs';
const dir='E:/五行仙途/source-assets/xuantie-spirit-anvil';
const geom=JSON.parse(await fs.readFile(dir+'/work/geometry.json','utf8'));
await connect();
console.log(getText(await call('get_project_info')));
await call('risky_eval',{code:`(()=>{if(Project.name!=='玄铁灵砧'||Cube.all.length||Mesh.all.length)throw Error('Expected the new empty anvil project');return true})()`});
await call('create_texture',{name:'玄铁灵砧_锻造图集',data:dir+'/xuantie_anvil_atlas.png',uv_width:geom.texture_size[0],uv_height:geom.texture_size[1]});
await call('create_texture',{name:'玄铁灵砧_灵光',data:dir+'/xuantie_anvil_glow.png',uv_width:geom.texture_size[0],uv_height:geom.texture_size[1],render_mode:'emissive'});
console.log(getText(await call('risky_eval',{code:`(()=>{
const data=${JSON.stringify(geom)};
const added=[];Undo.initEdit({elements:added,outliner:true});
Project.texture_width=data.texture_size[0];Project.texture_height=data.texture_size[1];Project.box_uv=false;
const root=new Group({name:'玄铁灵砧',origin:[0,0,0]}).init();
const groups={};for(const name of data.groups)groups[name]=new Group({name,origin:[0,0,0]}).addTo(root).init();
const iron=Texture.all.find(t=>t.name==='玄铁灵砧_锻造图集'),glow=Texture.all.find(t=>t.name==='玄铁灵砧_灵光');
for(const p of data.parts){
 const c=new Cube({name:p.name,from:p.from,to:p.to,origin:[0,0,0],box_uv:false,autouv:0}).addTo(groups[p.group]).init();
 for(const [f,d] of Object.entries(p.faces)){c.faces[f].uv=d.uv.slice();c.faces[f].texture=(d.glow?glow:iron).uuid;}
 c.shade=!p.glow;added.push(c);
}
Canvas.updateAll();Undo.finishEdit('根据参考图制作玄铁灵砧');
return {cubes:Cube.all.length,groups:Group.all.length,textures:Texture.all.length};})()`})));
await call('set_camera_angle',{position:[30,28,-36],target:[0,8,0],projection:'orthographic',zoom:.47});
await call('export_model',{codec_id:'project',path:dir+'/玄铁灵砧.bbmodel',overwrite:true,max_content_length:0});
console.log('Created and exported anvil project. Run render_hd.mjs for five views.');
