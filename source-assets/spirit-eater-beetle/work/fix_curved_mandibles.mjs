import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
await call('export_model',{codec_id:'project',path:dir+'/before_curved_mandibles.bbmodel',overwrite:true,max_content_length:0});
console.log(getText(await call('risky_eval',{code:`(()=>{
const old=Mesh.all.filter(m=>m.name.includes('mandible'));const edits=old.concat(Mesh.all.filter(m=>m.name.includes('eye_')||m.name.includes('前胸盾片')||m.name.includes('额甲')));Undo.initEdit({elements:edits,outliner:true});for(const m of old)m.remove();const tx=Texture.all.find(t=>t.name==='噬灵虫_三视图原画纹理.png');
const outline=[[.25,1.82],[.46,1.81],[.62,1.65],[.69,1.43],[.63,1.24],[.49,1.10],[.26,1.04],[.13,1.15],[.29,1.19],[.39,1.31],[.43,1.45],[.39,1.61],[.24,1.68]];
for(const s of [-1,1]){
const side=s<0?'left':'right',group=Group.all.find(g=>g.name===side+'_mandible_短弯口器'),N=outline.length;
const contour=outline.map(p=>new THREE.Vector2(p[0]*s,p[1]));
const tris=THREE.ShapeUtils.triangulateShape(contour,[]),vertices={},faces={};
for(let i=0;i<N;i++){let [x,y]=outline[i];let z=-3.94-(1.82-y)*.35,tip=i>=6&&i<=8;vertices['f'+i]=[x*s,y,z-(tip?.016:.078)];vertices['b'+i]=[x*s,y,z+(tip?.016:.12)];}
for(let i=0;i<N;i++){
 let j=(i+1)%N;let ids=['f'+i,'f'+j,'b'+j,'b'+i];if(s<0)ids.reverse();faces['edge'+i]={vertices:ids,uv:Object.fromEntries(ids.map((k,v)=>[k,[[515,674],[526,674],[526,680],[515,680]][v]])),texture:tx.uuid};
}
for(let i=0;i<tris.length;i++)for(const sideFace of ['f','b']){
 let ids=tris[i].map(k=>sideFace+k);const v=ids.map(k=>new THREE.Vector3(...vertices[k]));const normal=v[1].clone().sub(v[0]).cross(v[2].clone().sub(v[0]));if((sideFace==='f'&&normal.z>0)||(sideFace==='b'&&normal.z<0))ids.reverse();
 faces[sideFace+i]={vertices:ids,uv:Object.fromEntries(ids.map(k=>{const p=vertices[k];return[k,[513+(Math.abs(p[0])-.13)/.56*18,667+(1.82-p[1])/.78*18]];})),texture:tx.uuid};
}
const m=new Mesh({name:side+'_mandible_弧形内收钳',vertices,faces}).addTo(group).init();edits.push(m);
}
let repaired=0;
function inside(p,a,b,c){const sign=(p,a,b)=>(p[0]-b[0])*(a[1]-b[1])-(a[0]-b[0])*(p[1]-b[1]);const q=[sign(p,a,b),sign(p,b,c),sign(p,c,a)];return !(q.some(x=>x<0)&&q.some(x=>x>0));}
for(const m of Mesh.all.filter(m=>!m.name.includes('eye_')&&!m.name.includes('mandible'))){
 for(const f of Object.values(m.faces)){
  const uv=f.vertices.map(k=>f.uv[k]);if(uv.some(v=>!v))continue;let hit=false;
  for(let i=1;i<uv.length-1;i++)for(const p of [[523,630],[519,630],[526,633],[163,654],[241,654]])if(inside(p,uv[0],uv[i],uv[i+1]))hit=true;
  if(hit){if(!edits.includes(m))edits.push(m);f.vertices.forEach((k,i)=>f.uv[k]=[[506,609],[524,609],[524,620],[506,620]][i]);repaired++;}
 }
}
for(const m of Mesh.all.filter(m=>m.name.includes('eye_小高光'))){const s=m.name.startsWith('left')?-1:1;for(const v of Object.values(m.vertices)){v[0]+=s*.025;v[2]-=.026;}}
Canvas.updateAll();Undo.finishEdit('弧形口器与局部残留眼点修正');return {jawMeshes:2,repairedBakedEyeFaces:repaired};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['detail_mouth',[0,3.9,-16],[0,1.95,-3.6],4.2],['detail_eyes',[0,3.5,-15],[0,2.2,-3.4],5],['detail_eye_side',[-12,5,-12],[-.4,2.3,-3.3],5],['perspective',[-13,9,-16],[0,3.3,0],1.3],['front',[0,6,-20],[0,3.3,0],1.3]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});let pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
await call('set_camera_angle',{position:[-12,5,-15],target:[0,2.4,-2],projection:'orthographic',zoom:2});
