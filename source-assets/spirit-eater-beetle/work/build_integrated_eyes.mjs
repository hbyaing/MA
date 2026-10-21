import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
await call('export_model',{codec_id:'project',path:dir+'/before_integrated_eyes.bbmodel',overwrite:true,max_content_length:0});
console.log(getText(await call('risky_eval',{code:`(()=>{
if(Mesh.all.some(m=>m.parent?.name.includes('_eye_')))throw Error('Expected user-deleted eyes');
const fronts=Mesh.all.filter(m=>m.name.includes('前胸盾片')),elements=fronts.slice();Undo.initEdit({elements,outliner:true});
const tex=id=>Texture.all.find(t=>t.name===id+'.png').uuid;
const V=(x,y,z)=>new THREE.Vector3(x,y,z);
function make(name,g,vertices,polys,texture){let faces={};polys.forEach((vs,i)=>faces['f'+i]={vertices:vs,uv:Object.fromEntries(vs.map(k=>[k,[8,8]])),texture});const m=new Mesh({name,vertices,faces}).addTo(g).init();elements.push(m);return m;}
function oval(name,g,c,n,rx,ry,d,texture){const up=V(0,1,0).addScaledVector(n,-n.y).normalize(),a=new THREE.Vector3().crossVectors(up,n).normalize(),vertices={},faces=[],N=12,R=6;
for(let j=0;j<=R;j++){let ph=Math.PI*j/R;for(let i=0;i<N;i++){let th=2*Math.PI*i/N;vertices['v'+j+'_'+i]=c.clone().addScaledVector(n,d*Math.cos(ph)).addScaledVector(a,rx*Math.sin(ph)*Math.cos(th)).addScaledVector(up,ry*Math.sin(ph)*Math.sin(th)).toArray();}}
for(let j=0;j<R;j++)for(let i=0;i<N;i++){let a='v'+j+'_'+i,b='v'+j+'_'+((i+1)%N),c='v'+(j+1)+'_'+i,d='v'+(j+1)+'_'+((i+1)%N),f=j===0?[a,d,c]:j===R-1?[a,b,c]:[a,b,d,c];const p=f.map(k=>new THREE.Vector3(...vertices[k]));if(p[1].clone().sub(p[0]).cross(p[2].clone().sub(p[0])).dot(p[0].clone().sub(c instanceof THREE.Vector3?c:V(...vertices[a])))<0){}faces.push(f);}
const m=make(name,g,vertices,faces,texture);for(const f of Object.values(m.faces)){const ps=f.vertices.map(k=>new THREE.Vector3(...m.vertices[k])),fc=ps.reduce((a,v)=>a.add(v),V(0,0,0)).multiplyScalar(1/ps.length),nrm=ps[1].clone().sub(ps[0]).cross(ps[2].clone().sub(ps[0]));if(nrm.dot(fc.sub(c))<0)f.vertices.reverse();}return m;}
function connector(name,g,a,b,texture){const t=b.clone().sub(a).normalize(),u=new THREE.Vector3().crossVectors(t,V(1,0,0)).normalize(),v=new THREE.Vector3().crossVectors(t,u),vertices={},faces=[],N=8;
for(let j=0;j<3;j++){let c=a.clone().lerp(b,j/2),r=[.078,.105,.135][j];for(let i=0;i<N;i++)vertices['v'+j+'_'+i]=c.clone().addScaledVector(u,r*Math.cos(i/N*Math.PI*2)).addScaledVector(v,r*.8*Math.sin(i/N*Math.PI*2)).toArray();}
for(let j=0;j<2;j++)for(let i=0;i<N;i++)faces.push(['v'+j+'_'+i,'v'+j+'_'+((i+1)%N),'v'+(j+1)+'_'+((i+1)%N),'v'+(j+1)+'_'+i]);make(name,g,vertices,faces,texture);}
let repaired=0;
for(const m of fronts)for(const [k,f]of Object.entries(m.faces))if(['S2z6Vnkx','RlKum33d','Gcn7U6FW','Q82zkIdS'].includes(k)){f.vertices.forEach((k,i)=>f.uv[k]=[[772,515],[788,515],[788,529],[772,529]][i]);repaired++;}
for(const s of [-1,1]){const side=s<0?'left':'right',g=Group.all.find(g=>g.name===side+'_eye_复眼'),c=V(s*.675,2.305,-3.36),n=V(s*.77,.20,-.61).normalize();
connector(side+'_eye_触须根部连接甲',g,V(s*.55,2.54,-3.58),c,tex('eye_socket_chitin'));
oval(side+'_eye_嵌入式眼眶',g,c,n,.139,.149,.062,tex('eye_socket_chitin'));
oval(side+'_eye_深绿复眼',g,c.clone().addScaledVector(n,.059),n,.096,.111,.035,tex('eye_lens_green'));
oval(side+'_eye_微小高光',g,c.clone().addScaledVector(n,.10).add(V(s*-.01,.025,0)),n,.018,.023,.008,tex('eye_catchlight'));
}
Canvas.updateAll();Undo.finishEdit('眼眶嵌入原眼位并连接触须根部');return {eyes:2,connectedAntennaRoots:2,repairedFaces:repaired};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['detail_eye_side',[-12,5,-12],[-.4,2.3,-3.3],5],['detail_eyes',[0,3.5,-15],[0,2.2,-3.4],5],['perspective',[-13,9,-16],[0,3.3,0],1.3],['front',[0,6,-20],[0,3.3,0],1.3]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});let pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
