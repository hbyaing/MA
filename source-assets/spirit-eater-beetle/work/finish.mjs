import fs from 'node:fs/promises';
import {connect,call,getText} from './mcp.mjs';
await connect();
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
console.log(getText(await call('risky_eval',{code:`(()=>{
Undo.initEdit({elements:Mesh.all});
for(const m of Mesh.all){
 if(m.name.includes('antenna')){for(const v of Object.values(m.vertices)){v[1]+=Math.max(0,v[1]-2.7)*.30;}}
 if(/肢节|关节|腹面节甲|侧腹护缘/.test(m.name)){
  for(const f of Object.values(m.faces))for(const k of f.vertices){const uv=f.uv[k];f.uv[k]=[304+(uv[0]-758)*.29,686+(uv[1]-512)*.55];}
 }
 if(m.name.includes('层叠背甲')&&!m.name.includes('护缘')){
  for(const f of Object.values(m.faces)){
   const v=f.vertices.map(k=>m.vertices[k]);const dy=Math.max(...v.map(p=>p[1]))-Math.min(...v.map(p=>p[1]));
   const dz=Math.max(...v.map(p=>p[2]))-Math.min(...v.map(p=>p[2]));
   if(dy<.20||dz<.15){for(const k of f.vertices){const p=m.vertices[k];f.uv[k]=[778+p[0]*7,514+p[2]*2];}}
  }
 }
}
Canvas.updateAll();Undo.finishEdit('完善虫肢色泽与触角轮廓');return {meshes:Mesh.all.length,groups:Group.all.length};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
await call('create_offscreen_view',{id:'review',width:1200,height:1000,copy_view:'active'});
const shots=[['perspective',[-13,9,-16]],['front',[0,6,-20]],['side',[-20,3.5,0]],['back',[0,8,20]]];
for(const [name,position]of shots){const r=await call('set_camera_angle',{view:'review',position,target:[0,3.3,0],projection:'orthographic',zoom:1.3});let pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
await call('delete_offscreen_view',{view:'review'});
await call('set_camera_angle',{position:[-13,9,-16],target:[0,3.3,0],projection:'orthographic',zoom:1.3});
await call('risky_eval',{code:"(()=>{Project.saved=true;return {project:Project.name,save_path:Project.save_path};})()"});
const model=JSON.parse(await fs.readFile(dir+'/噬灵虫.bbmodel','utf8'));
const report={format:model.meta.model_format,meshes:model.elements.length,groups:model.groups?.length,textures:model.textures.length,faces:model.elements.reduce((n,e)=>n+Object.keys(e.faces||{}).length,0),invalid_vertices:0,invalid_uv:0};
for(const e of model.elements){for(const v of Object.values(e.vertices||{}))if(v.some(n=>!Number.isFinite(n)))report.invalid_vertices++;for(const f of Object.values(e.faces||{}))for(const uv of Object.values(f.uv||{}))if(uv.some(n=>!Number.isFinite(n)))report.invalid_uv++;}
await fs.writeFile(dir+'/validation.json',JSON.stringify(report,null,2));
await fs.writeFile(dir+'/说明.txt','噬灵虫 — 三视图建模\n\n主文件：噬灵虫.bbmodel（纹理内嵌，可直接打开）\n制作方式：Blockbench MCP；通用网格模型，103 个网格、41 个层级分组。\n正面、侧面、背面和斜视预览均由 Blockbench 实际模型渲染。\n\n已制作：层叠甲片、六足及关节、弯曲触角及节环、触角青光末梢、复眼、弯曲口器与内齿、青绿色腹部和透明外膜。\n纹理来自用户提供三视图的投射；隐藏面与视图之间的过渡为三维重建，不能保证每个视角逐像素吻合。\n\n此文件为精细通用网格美术源文件，不是可直接导入 GeckoLib 的立方体 geo.json；尚未制作动画或接入游戏。腹部灵光为贴图与预览材质表现，未实现游戏内体积光或粒子。\n');
console.log(JSON.stringify(report));
