import fs from 'node:fs/promises';import {connect,call,getText} from './mcp.mjs';
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
const d=JSON.parse(await fs.readFile(dir+'/before_tail_uv_symmetry.bbmodel','utf8'));
for(const a of d.elements.filter(m=>m.name.startsWith('carapace_')&&m.name.includes('左_'))){
const b=d.elements.find(m=>m.name===a.name.replace('左_','右_'));
const av=Object.values(a.vertices),bv=Object.values(b.vertices);let err=0;
for(const p of av)err=Math.max(err,Math.min(...bv.map(q=>Math.hypot(p[0]+q[0],p[1]-q[1],p[2]-q[2]))));
const uv=Object.values(a.faces).flatMap(f=>Object.values(f.uv));
console.log(JSON.stringify({name:a.name,verts:av.length,right:bv.length,error:err,origin:a.origin,rotation:a.rotation,uvBounds:[Math.min(...uv.map(v=>v[0])),Math.max(...uv.map(v=>v[0])),Math.min(...uv.map(v=>v[1])),Math.max(...uv.map(v=>v[1]))]}));
}
await connect();
for(const [name,position]of [['tail_current_left',[-18,6,8]],['tail_current_right',[18,6,8]],['tail_current_back',[0,6,20]]]){
const r=await call('set_camera_angle',{position,target:[0,2.6,2.5],zoom:2.8,projection:'orthographic'});const pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/work/'+name+'.png',Buffer.from(pic.data,'base64'));}
