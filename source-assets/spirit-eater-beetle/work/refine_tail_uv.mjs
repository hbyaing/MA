import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
const before=JSON.parse(await fs.readFile(dir+'/before_tail_uv_symmetry.bbmodel','utf8'));const old=before.elements.find(m=>m.name.includes('腹部_青玉色薄膜'));
console.log(getText(await call('risky_eval',{code:`(()=>{
const m=Mesh.all.find(m=>m.name.includes('腹部_青玉色薄膜'));const old=${JSON.stringify(old.faces)};Undo.initEdit({elements:[m]});
for(const [key,f]of Object.entries(m.faces)){
f.uv=JSON.parse(JSON.stringify(old[key].uv));
for(const k of f.vertices){const p=m.vertices[k],w=Math.min(1,Math.max(0,(p[2]-2.35)/.7));if(!w)continue;
const theta=Math.atan2(Math.max(0,(p[2]-1.89)/2.36),Math.abs(p[0])/2.02);
const uv=[895+102*theta/(Math.PI/2),605-(p[1]-2.772)/2.02*78];f.uv[k]=f.uv[k].map((v,i)=>v*(1-w)+uv[i]*w);
}}
Canvas.updateAll();Undo.finishEdit('尾腹局部弧向纹理展开');return true})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position]of [['tail_fixed_left',[-18,6,8]],['tail_fixed_right',[18,6,8]],['tail_fixed_back',[0,6,20]]]){const r=await call('set_camera_angle',{position,target:[0,2.6,2.5],zoom:2.8,projection:'orthographic'});const pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
