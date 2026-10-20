import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
console.log(getText(await call('risky_eval',{code:`(()=>{
const tails=Mesh.all.filter(m=>m.name.startsWith('carapace_')&&/尾背甲|尾侧甲|尾端护片/.test(m.name)),eyes=Mesh.all.filter(m=>m.name.includes('触须根部连接甲'));Undo.initEdit({elements:[...tails,...eyes],outliner:true});
const center=[0,2.772,1.89],radii=[2.02,2.02,2.36];
function radius(p){return Math.hypot(...p.map((v,i)=>(v-center[i])/radii[i]));}
function project(p,len){let r=radius(p);return p.map((v,i)=>center[i]+(v-center[i])*len/r);}
let faceTotal=0;
for(const m of tails){
 let vertices=JSON.parse(JSON.stringify(m.vertices)),triangles=[];
 for(const f of Object.values(m.faces)){let ks=f.vertices;for(let i=1;i<ks.length-1;i++)triangles.push({vs:[ks[0],ks[i],ks[i+1]],uv:[f.uv[ks[0]],f.uv[ks[i]],f.uv[ks[i+1]]],texture:f.texture});}
 let next=0;
 for(let pass=0;pass<2;pass++){
  const cache=new Map(),res=[];
  for(const f of triangles){const mid=[];for(let i=0;i<3;i++){const a=f.vs[i],b=f.vs[(i+1)%3],key=[a,b].sort().join('|');let k=cache.get(key);if(!k){k='sub'+next++;const p=vertices[a].map((v,j)=>(v+vertices[b][j])/2),len=(radius(vertices[a])+radius(vertices[b]))/2;vertices[k]=project(p,len);cache.set(key,k);}mid.push(k);}
  const u=f.uv,mu=[0,1,2].map(i=>u[i].map((v,j)=>(v+u[(i+1)%3][j])/2));
  res.push({vs:[f.vs[0],mid[0],mid[2]],uv:[u[0],mu[0],mu[2]],texture:f.texture},{vs:[mid[0],f.vs[1],mid[1]],uv:[mu[0],u[1],mu[1]],texture:f.texture},{vs:[mid[2],mid[1],f.vs[2]],uv:[mu[2],mu[1],u[2]],texture:f.texture},{vs:mid,uv:mu,texture:f.texture});}
  triangles=res;
 }
 m.vertices=vertices;m.faces={};for(let i=0;i<triangles.length;i++){const f=triangles[i];m.addFaces(new MeshFace(m,{vertices:f.vs,uv:Object.fromEntries(f.vs.map((k,j)=>[k,f.uv[j]])),texture:f.texture}));}faceTotal+=triangles.length;
}
for(const m of eyes){const s=m.name.startsWith('left')?-1:1,n=new THREE.Vector3(s*.77,.20,-.61).normalize();for(const [k,v]of Object.entries(m.vertices)){const row=Number(k.slice(1).split('_')[0]),factor=row/2;v[0]-=n.x*.12*factor;v[1]-=n.y*.12*factor;v[2]-=n.z*.12*factor;}}
Canvas.updateAll();Undo.finishEdit('尾甲细分贴体，修正直面切入腹部');return {tailPlates:tails.length,tailTriangles:faceTotal};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['left_tail',[-18,6,8],[0,2.6,2.5],2.8],['right_tail',[18,6,8],[0,2.6,2.5],2.8],['detail_eye_side',[-12,5,-12],[-.4,2.3,-3.3],5],['detail_eyes',[0,3.5,-15],[0,2.2,-3.4],5],['perspective',[-13,9,-16],[0,3.3,0],1.3]]){const rr=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});let pic=rr.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
