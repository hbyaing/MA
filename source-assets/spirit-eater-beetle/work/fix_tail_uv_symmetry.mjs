import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
console.log(getText(await call('risky_eval',{code:`(()=>{
const tails=Mesh.all.filter(m=>m.name.startsWith('carapace_')&&/尾背甲|尾侧甲|尾端护片/.test(m.name));
const abdomen=Mesh.all.find(m=>m.name.includes('腹部_青玉色薄膜'));
Undo.initEdit({elements:[...tails,abdomen]});
let mirrored=0;
for(const left of tails.filter(m=>m.name.includes('左_'))){
 const right=tails.find(m=>m.name===left.name.replace('左_','右_'));
 right.vertices=Object.fromEntries(Object.entries(left.vertices).map(([k,v])=>[k,[-v[0],v[1],v[2]]]));
 right.faces={};
 for(const f of Object.values(left.faces))right.addFaces(new MeshFace(right,{vertices:f.vertices.slice().reverse(),uv:JSON.parse(JSON.stringify(f.uv)),texture:f.texture}));
 mirrored++;
}
let remapped=0;
for(const f of Object.values(abdomen.faces)){
 const ps=f.vertices.map(k=>abdomen.vertices[k]);
 if(Math.max(...ps.map(p=>p[2]))<1.89)continue;
 for(const k of f.vertices){const p=abdomen.vertices[k];f.uv[k]=[926+(p[2]-1.89)/2.36*48,610-(p[1]-2.772)/2.02*51];}
 remapped++;
}
Canvas.updateAll();Undo.finishEdit('尾甲严格镜像并清理尾腹串色');return {mirroredPairs:mirrored,abdomenFaces:remapped};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position]of [['tail_fixed_left',[-18,6,8]],['tail_fixed_right',[18,6,8]],['tail_fixed_back',[0,6,20]]]){const r=await call('set_camera_angle',{position,target:[0,2.6,2.5],zoom:2.8,projection:'orthographic'});const pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
