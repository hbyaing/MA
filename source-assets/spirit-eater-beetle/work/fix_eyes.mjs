import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
await call('export_model',{codec_id:'project',path:dir+'/before_eye_revision.bbmodel',overwrite:true,max_content_length:0});
for(const [name,color]of [['eye_socket_chitin','#292b20'],['eye_lens_green','#4b7d47'],['eye_catchlight','#bdd69a']])await call('create_texture',{name:name+'.png',width:16,height:16,fill_color:color,render_sides:'front'});
const code=`(()=>{
const old=Mesh.all.filter(m=>m.parent?.name.includes('_eye_'));const elements=old.slice();Undo.initEdit({elements,outliner:true});for(const m of old)m.remove();
const V=(x,y,z)=>new THREE.Vector3(x,y,z);
function lens(name,g,c,n,rx,ry,depth,texture){
 const up=V(0,1,0);up.addScaledVector(n,-up.dot(n)).normalize();const tangent=new THREE.Vector3().crossVectors(up,n).normalize();
 const vs={},faces={},R=5,N=12;
 for(let j=0;j<=R;j++){const phi=Math.PI*j/R;for(let i=0;i<N;i++){const theta=2*Math.PI*i/N;const p=c.clone().addScaledVector(n,depth*Math.cos(phi)).addScaledVector(tangent,rx*Math.sin(phi)*Math.cos(theta)).addScaledVector(up,ry*Math.sin(phi)*Math.sin(theta));vs['v'+j+'_'+i]=p.toArray();}}
 let fi=0;for(let j=0;j<R;j++)for(let i=0;i<N;i++){let a='v'+j+'_'+i,b='v'+j+'_'+((i+1)%N),d='v'+(j+1)+'_'+i,e='v'+(j+1)+'_'+((i+1)%N);let f=j===0?[a,e,d]:j===R-1?[a,b,d]:[a,b,e,d];const p=f.map(k=>new THREE.Vector3(...vs[k]));const normal=p[1].clone().sub(p[0]).cross(p[2].clone().sub(p[0]));const fc=p.reduce((a,v)=>a.add(v),V(0,0,0)).multiplyScalar(1/p.length);if(normal.dot(fc.sub(c))<0)f.reverse();faces['f'+fi++]={vertices:f,uv:Object.fromEntries(f.map(k=>[k,[8,8]])),texture};}
 const m=new Mesh({name,vertices:vs,faces}).addTo(g).init();elements.push(m);return m;
}
for(const s of [-1,1]){
 const side=s<0?'left':'right',g=Group.all.find(g=>g.name===side+'_eye_复眼'),n=V(s*.75,.08,-.66).normalize(),c=V(s*.81,2.21,-3.47);
 lens(side+'_eye_贴甲眼眶',g,c,n,.145,.156,.044,Texture.all.find(t=>t.name==='eye_socket_chitin.png').uuid);
 lens(side+'_eye_深绿复眼',g,c.clone().addScaledVector(n,.041),n,.103,.116,.025,Texture.all.find(t=>t.name==='eye_lens_green.png').uuid);
 const highlight=c.clone().addScaledVector(n,.066).add(V(s*-.022,.032,-.006));
 lens(side+'_eye_小高光',g,highlight,n,.019,.027,.006,Texture.all.find(t=>t.name==='eye_catchlight.png').uuid);
}
const head=Mesh.all.find(m=>m.name.includes('头部底壳'));
elements.push(head);
for(const f of Object.values(head.faces)){
 const c=f.vertices.reduce((a,k)=>a.map((v,i)=>v+head.vertices[k][i]/f.vertices.length),[0,0,0]);
 if(Math.abs(c[0])>.45&&c[1]>1.95){f.vertices.forEach((k,i)=>f.uv[k]=[[191,638],[205,638],[205,647],[191,647]][i]);}
}
Canvas.updateAll();Undo.finishEdit('小型贴甲深绿复眼');return {eyeGroups:Group.all.filter(g=>g.name.includes('_eye_')).length};})()`;
console.log(getText(await call('risky_eval',{code})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['detail_eyes',[0,3.5,-15],[0,2.2,-3.4],5],['detail_eye_side',[-12,5,-12],[-.4,2.3,-3.3],5],['perspective',[-13,9,-16],[0,3.3,0],1.3],['front',[0,6,-20],[0,3.3,0],1.3]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});let pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
await call('set_camera_angle',{position:[-12,5,-15],target:[0,2.4,-2],projection:'orthographic',zoom:2});
