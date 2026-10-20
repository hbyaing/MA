import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
await call('export_model',{codec_id:'project',path:dir+'/before_separate_antenna_roots.bbmodel',overwrite:true,max_content_length:0});
console.log(getText(await call('risky_eval',{code:`(()=>{
const old=Mesh.all.filter(m=>m.name.includes('触须根部连接甲'));const elements=old.slice();Undo.initEdit({elements,outliner:true});for(const m of old)m.remove();
const tx=Texture.all.find(t=>t.name==='噬灵虫_三视图原画纹理.png');
for(const s of [-1,1]){
 const side=s<0?'left':'right',g=Group.all.find(g=>g.name===side+'_antenna_弯曲触角'),vertices={},faces={};
 const pts=[[s*.535,2.42,-3.558],[s*.55,2.52,-3.58],[s*.573,2.633,-3.602]],r=[.090,.092,.066],N=8;
 const t=new THREE.Vector3(...pts[2]).sub(new THREE.Vector3(...pts[0])).normalize(),a=new THREE.Vector3().crossVectors(t,new THREE.Vector3(1,0,0)).normalize(),b=new THREE.Vector3().crossVectors(t,a);
 for(let j=0;j<3;j++)for(let i=0;i<N;i++){let p=new THREE.Vector3(...pts[j]).addScaledVector(a,r[j]*Math.cos(i/N*Math.PI*2)).addScaledVector(b,r[j]*Math.sin(i/N*Math.PI*2));vertices['v'+j+'_'+i]=p.toArray();}
 let n=0;for(let j=0;j<2;j++)for(let i=0;i<N;i++){const vs=['v'+j+'_'+i,'v'+j+'_'+((i+1)%N),'v'+(j+1)+'_'+((i+1)%N),'v'+(j+1)+'_'+i];faces['f'+n++]={vertices:vs,uv:Object.fromEntries(vs.map((k,q)=>[k,[[777,516],[784,516],[784,525],[777,525]][q]])),texture:tx.uuid};}
 const m=new Mesh({name:side+'_antenna_额甲独立基座',vertices,faces}).addTo(g).init();elements.push(m);
}
Canvas.updateAll();Undo.finishEdit('触角改为额甲独立基座，解除与眼睛连接');return {removedEyeConnections:old.length,independentAntennaBases:2};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['detail_eyes',[0,3.5,-15],[0,2.2,-3.4],5],['detail_eye_side',[-12,5,-12],[-.4,2.3,-3.3],5],['detail_mouth',[0,3.9,-16],[0,1.95,-3.6],4.2],['perspective',[-13,9,-16],[0,3.3,0],1.3],['front',[0,6,-20],[0,3.3,0],1.3],['side',[-20,3.5,0],[0,3.3,0],1.3],['back',[0,8,20],[0,3.3,0],1.3],['bottom',[0,-20,0],[0,2,0],1.5]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic',max_size:1100});let pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
await call('set_camera_angle',{position:[-12,5,-15],target:[0,2.4,-2],projection:'orthographic',zoom:2});
const before=JSON.parse(await fs.readFile(dir+'/before_separate_antenna_roots.bbmodel','utf8')),after=JSON.parse(await fs.readFile(dir+'/噬灵虫.bbmodel','utf8'));
const protectedUnchanged=before.elements.filter(e=>!e.name.includes('触须根部连接甲')).every(e=>JSON.stringify(e)===JSON.stringify(after.elements.find(a=>a.uuid===e.uuid)));
const report={removed_connections:before.elements.filter(e=>e.name.includes('触须根部连接甲')).length,independent_antenna_bases:after.elements.filter(e=>e.name.includes('额甲独立基座')).length,other_elements_unchanged:protectedUnchanged,meshes:after.elements.length};
await fs.writeFile(dir+'/antenna_root_validation.json',JSON.stringify(report,null,2));console.log(JSON.stringify(report));
const dest='E:/五行仙途/deliverables/噬灵虫模型';for(const n of ['噬灵虫.bbmodel','perspective.png','front.png','side.png','back.png','bottom.png','detail_mouth.png','detail_eyes.png','detail_eye_side.png','antenna_root_validation.json'])await fs.copyFile(dir+'/'+n,dest+'/'+n);
