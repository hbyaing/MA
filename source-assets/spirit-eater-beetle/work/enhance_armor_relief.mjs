import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
const original=JSON.parse(await fs.readFile(dir+'/before_shell_antenna_revision.bbmodel','utf8'));
const pre=JSON.parse(await fs.readFile(dir+'/before_tail_symmetry.bbmodel','utf8'));
const specs=[];
for(const old of original.elements.filter(m=>m.name.startsWith('carapace_')&&m.name.includes('左_'))){
 const keys=Object.keys(old.vertices),N=(keys.length-1)/3,attrs={};
 keys.forEach((k,i)=>attrs[k]={p:old.vertices[k],roof:i<=2*N?1:0,crown:i===2*N?1:i>=N&&i<2*N?.45:0});
 const tail=/尾背甲|尾侧甲|尾端护片/.test(old.name);let triangles=[];
 if(tail){const seed=pre.elements.find(m=>m.name===old.name);for(const f of Object.values(seed.faces)){const vs=f.vertices;const wall=vs.some(k=>keys.indexOf(k)>2*N);for(let i=1;i<vs.length-1;i++)triangles.push({vs:[vs[0],vs[i],vs[i+1]],wall});}
 let next=0;for(let pass=0;pass<2;pass++){const cache=new Map(),res=[];for(const f of triangles){const mid=[];for(let i=0;i<3;i++){const a=f.vs[i],b=f.vs[(i+1)%3],edge=[a,b].sort().join('|');let k=cache.get(edge);if(!k){k='sub'+next++;const aa=attrs[a],bb=attrs[b];attrs[k]={p:aa.p.map((v,j)=>(v+bb.p[j])/2),roof:(aa.roof+bb.roof)/2,crown:(aa.crown+bb.crown)/2};cache.set(edge,k);}mid.push(k);}for(const vs of [[f.vs[0],mid[0],mid[2]],[mid[0],f.vs[1],mid[1]],[mid[2],mid[1],f.vs[2]],mid])res.push({vs,wall:f.wall});}triangles=res;}
 }
 const center=keys.slice(0,N).reduce((a,k)=>a.map((v,i)=>v+old.vertices[k][i]/N),[0,0,0]);
 specs.push({name:old.name,tail,attrs,triangles,center,keys,N});
}
await connect();
console.log(getText(await call('risky_eval',{code:`(()=>{
const specs=${JSON.stringify(specs)};const meshes=Mesh.all.filter(m=>m.name.startsWith('carapace_'));for(const d of specs){const m=meshes.find(m=>m.name===d.name);if(Object.keys(m.vertices).some(k=>!d.attrs[k]))throw Error('Attribute mismatch '+m.name);if(d.tail&&Object.values(m.faces).length!==d.triangles.length)throw Error('Face count mismatch');}
Undo.initEdit({elements:meshes});let updated=0;
for(const d of specs){const m=meshes.find(m=>m.name===d.name);
for(const[k,p]of Object.entries(m.vertices)){const a=d.attrs[k],lift=.035*a.roof+.10*a.crown;if(!lift)continue;let n;
if(d.tail)n=new THREE.Vector3(p[0]/(2.02**2),(p[1]-2.772)/(2.02**2),(p[2]-1.89)/(2.36**2)).normalize();
else n=new THREE.Vector3(p[0]*.25,1,(p[2]-d.center[2])*.10).normalize();
for(let i=0;i<3;i++)p[i]+=n.getComponent(i)*lift;
}
const fs=Object.values(m.faces);
for(let i=0;i<fs.length;i++){const f=fs[i],wall=d.tail?d.triangles[i].wall:f.vertices.some(k=>d.keys.indexOf(k)>2*d.N);
if(wall){for(let j=0;j<f.vertices.length;j++)f.uv[f.vertices[j]]=[[749,522],[765,522],[765,531],[749,531]][j%4];}
else if(d.tail){for(const k of f.vertices){const p=d.attrs[k].p;const u=760+(d.center[2]+(p[2]-d.center[2])*.78)/.014,v=790-(d.center[1]+(p[1]-d.center[1])*.78)/.014;f.uv[k]=[u,v];}}
}
const r=meshes.find(mm=>mm.name===m.name.replace('左_','右_'));r.vertices=Object.fromEntries(Object.entries(m.vertices).map(([k,p])=>[k,[-p[0],p[1],p[2]]]));r.faces={};for(const f of Object.values(m.faces))r.addFaces(new MeshFace(r,{vertices:f.vertices.slice().reverse(),uv:JSON.parse(JSON.stringify(f.uv)),texture:f.texture}));updated+=2;
}
Canvas.updateAll();Undo.finishEdit('增强背甲棱脊厚度与深色叠压边');return {enhancedPlates:updated};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['armor_tail',[-18,6,8],[0,3,2.8],3.3],['armor_top',[-12,11,15],[0,3,2.8],3.3],['armor_full',[-13,9,-16],[0,3.3,0],1.5]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});const pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
