import fs from 'node:fs/promises';
import {connect,call,getText} from './mcp.mjs';
await connect();
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
const code=`(()=>{
Undo.initEdit({elements:Mesh.all,textures:Texture.all});
for(const m of Mesh.all){
 if(m.name.includes('头部底壳')){for(const v of Object.values(m.vertices)){v[0]*=.83;v[1]=1.99+(v[1]-1.99)*.77;}}
 for(const f of Object.values(m.faces)){
  const vs=f.vertices.map(k=>m.vertices[k]);
  const a=new THREE.Vector3(...vs[1]).sub(new THREE.Vector3(...vs[0]));
  const b=new THREE.Vector3(...vs[2]).sub(new THREE.Vector3(...vs[0]));
  const n=a.cross(b).normalize();
  if(m.name.includes('层叠背甲')&&Math.abs(n.y)>.67&&!m.name.includes('护缘')){
    for(const k of f.vertices){const p=m.vertices[k];f.uv[k]=[203+p[0]*40,Math.max(435,Math.min(628,542-p[2]*30))];}
  }
  if(m.name.includes('头部底壳')){
    for(const k of f.vertices){const p=m.vertices[k];f.uv[k]=[203+p[0]*31,658-(p[1]-1.99)*45];}
  }
 }
}
for(const m of Mesh.all.filter(m=>m.name.includes('触角末端青光'))){const vs=Object.values(m.vertices);const c=vs.reduce((a,v)=>a.map((x,i)=>x+v[i]/vs.length),[0,0,0]);for(const v of vs){v[0]=c[0]+(v[0]-c[0])*1.5;v[1]=c[1]+(v[1]-c[1])*1.2;}}
Project.texture_width=1448;Project.texture_height=1086;
Project.save_path='E:/五行仙途/source-assets/spirit-eater-beetle/噬灵虫.bbmodel';
Canvas.updateAll();Undo.finishEdit('精修头甲、背甲与触角末梢');return {meshes:Mesh.all.length,faces:Mesh.all.reduce((n,m)=>n+Object.keys(m.faces).length,0)};
})()`;
console.log(getText(await call('risky_eval',{code})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const [name,pos,target] of [
 ['perspective',[-13,9,-16],[0,2.9,0]],
 ['side',[-20,3.4,0],[0,2.8,0]],
 ['front',[0,5,-20],[0,2.8,0]],
 ['back',[0,8,20],[0,2.8,0]]
]){
 const r=await call('set_camera_angle',{position:pos,target,projection:'orthographic',zoom:1.3,max_size:1600});
 const pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));
}
await call('set_camera_angle',{position:[-13,9,-16],target:[0,2.9,0],projection:'orthographic',zoom:1.3});
