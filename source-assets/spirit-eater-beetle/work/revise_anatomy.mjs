import fs from 'node:fs/promises';
import {connect,call,getText} from './mcp.mjs';
await connect();
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
await call('export_model',{codec_id:'project',path:dir+'/before_anatomy_revision.bbmodel',overwrite:true,max_content_length:0});
const add=(a,b)=>a.map((v,i)=>v+b[i]),sub=(a,b)=>a.map((v,i)=>v-b[i]),mul=(a,s)=>a.map(v=>v*s),dot=(a,b)=>a.reduce((s,v,i)=>s+v*b[i],0),cross=(a,b)=>[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]],unit=a=>mul(a,1/Math.hypot(...a)),mean=vs=>mul(vs.reduce(add,[0,0,0]),1/vs.length);
const P=(u,v,x=0)=>[x,(790-v)*.014,(u-760)*.014];
const created=[];
function record(name,group,vertices,faces,types){
 const center=mean(vertices);
 for(const f of faces){const n=cross(sub(vertices[f[1]],vertices[f[0]]),sub(vertices[f[2]],vertices[f[0]]));if(dot(n,sub(mean(f.map(i=>vertices[i])),center))<0)f.reverse();}
 const patches={shell:[562,680,580,697],side:[775,515,791,533],ridge:[582,687,590,699],edge:[604,682,612,692],jaw:[514,666,529,681],joint:[789,531,795,539]};
 const uvs=faces.map((f,i)=>{const r=patches[types[i]||'shell'];return f.map((_,j)=>[[r[0],r[1]],[r[2],r[1]],[r[2],r[3]],[r[0],r[3]]][j]);});
 created.push({name,group,vertices,faces,uvs});
}
function segment(name,group,a,b,width,depth,terminal=false){
 const t=unit(sub(b,a));let up=sub([0,1,0],mul(t,t[1]));if(Math.hypot(...up)<.05)up=[0,0,1];up=unit(up);const lateral=unit(cross(t,up));
 const profile=[[-.65,.75],[0,1.14],[.65,.75],[1,.17],[.73,-.65],[-.73,-.65],[-1,.17]];
 const rings=terminal?[[.03,.48],[.22,1.12],[.62,.62],[1.03,.006]]:[[.06,.57],[.20,1.13],[.72,.96],[.91,.61],[.95,.48]];
 const vs=[];
 for(const [u,r]of rings)for(const [x,y]of profile)vs.push(add(add(a,mul(sub(b,a),u)),add(mul(lateral,x*width*r),mul(up,y*depth*r))));
 const faces=[],types=[],N=7;
 for(let j=0;j<rings.length-1;j++)for(let i=0;i<N;i++){faces.push([j*N+i,j*N+(i+1)%N,(j+1)*N+(i+1)%N,(j+1)*N+i]);types.push(j===0||j===rings.length-2?'edge':i<2?'ridge':i===2||i===6?'shell':'side');}
 for(let i=1;i<N-1;i++){faces.push([0,i+1,i]);types.push('joint');let k=(rings.length-1)*N;faces.push([k,k+i,k+i+1]);types.push(terminal?'ridge':'joint');}
 record(name,group,vs,faces,types);
}
for(const s of [-1,1]){
 const side=s<0?'left':'right',g=side+'_mandible_短弯口器';
 const outline=[[.29,1.78,-3.68],[.57,1.73,-3.78],[.75,1.45,-3.94],[.69,1.16,-4.10],[.47,.95,-4.23],[.10,1.04,-4.28],[.38,1.20,-4.18],[.46,1.40,-4.04],[.29,1.56,-3.85]].map(([x,y,z])=>[x*s,y,z]);
 const N=outline.length,vs=[...outline.map(p=>add(p,[0,0,-.06])),...outline.map(p=>add(p,[0,0,.095]))];
 const c=mean(outline);c[2]-=.18;vs.push(c);
 const faces=[],types=[];
 for(let i=0;i<N;i++){let j=(i+1)%N;faces.push([i,j,2*N]);types.push(i===4||i===5||i===6?'edge':'jaw');faces.push([i,N+i,N+j,j]);types.push('side');}
 for(let i=1;i<N-1;i++){faces.push([N,N+i+1,N+i]);types.push('side');}
 record(side+'_mandible_锐利弯刃',g,vs,faces,types);
 segment(side+'_mandible_内缘短齿',g,[s*.46,1.46,-4.0],[s*.24,1.37,-4.12],.075,.055,true);
}
const legs=[
 {id:'front',cn:'前足',path:[[589,646,1.08],[617,671,1.87],[579,691,2.67],[554,735,2.98],[502,788,3.14]],w:[.22,.28,.32,.18]},
 {id:'middle',cn:'中足',path:[[702,642,1.64],[714,676,2.43],[731,728,2.88],[765,790,3.35]],w:[.20,.25,.19]},
 {id:'hind',cn:'后足',path:[[781,650,1.72],[820,673,2.4],[844,649,2.88],[868,716,3.04],[940,777,3.47]],w:[.20,.21,.25,.18]}
];
for(const s of [-1,1])for(const l of legs){const side=s<0?'left':'right',pts=l.path.map(([u,v,x])=>P(u,v,x*s));for(let i=0;i<pts.length-1;i++){const g=side+'_'+l.id+'_segment_'+i;segment(g+'__硬质棱脊甲节',g,pts[i],pts[i+1],l.w[i],l.w[i]*.82,i===pts.length-2);}}
console.log(getText(await call('risky_eval',{code:`(()=>{
 if(!Project.name.includes('噬灵虫'))throw Error('Wrong project');
 const edits=Mesh.all.filter(m=>/mandible|肢节|关节|额甲|eye_/.test(m.name));
 Undo.initEdit({elements:edits,outliner:true});
 const tx=Texture.all.find(t=>t.name==='噬灵虫_三视图原画纹理.png');
 for(const m of edits){
  if(m.name.includes('mandible')||m.name.includes('肢节')){m.remove();continue;}
  if(m.name.includes('关节')){const vs=Object.values(m.vertices),c=vs.reduce((a,v)=>a.map((x,i)=>x+v[i]/vs.length),[0,0,0]);for(const v of vs)for(let i=0;i<3;i++)v[i]=c[i]+(v[i]-c[i])*.73;}
  if(m.name.includes('额甲')||m.name.includes('眼眶')||m.name.includes('关节')){for(const f of Object.values(m.faces)){f.texture=tx.uuid;f.vertices.forEach((k,j)=>{f.uv[k]=[[771,514],[786,514],[786,527],[771,527]][j];});}}
 }
 Canvas.updateAll();Undo.finishEdit('消除额甲上的重复眼点，更新口器与足甲');return {removedOldParts:true,remainingEyes:Mesh.all.filter(m=>m.name.includes('微光复眼')).length};})()`})));
for(const m of created)await call('place_mesh',{group:m.group,texture:'噬灵虫_三视图原画纹理.png',elements:[{name:m.name,vertices:m.vertices,faces:m.faces}]});
console.log(getText(await call('risky_eval',{code:`(()=>{const data=${JSON.stringify(created.map(({name,uvs})=>({name,uvs})))};const ms=data.map(d=>Mesh.all.find(m=>m.name===d.name));Undo.initEdit({elements:ms});data.forEach((d,i)=>Object.values(ms[i].faces).forEach((f,j)=>f.vertices.forEach((k,v)=>f.uv[k]=d.uvs[j][v])));Canvas.updateAll();Undo.finishEdit('硬甲明暗与钳刃 UV');return {meshes:Mesh.all.length};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const [name,position,target,zoom]of [
 ['detail_mouth',[0,3.9,-16],[0,1.95,-3.6],4.2],
 ['detail_legs',[-13,8,-11],[0,2.0,-.3],1.8],
 ['perspective',[-13,9,-16],[0,3.3,0],1.3],
 ['front',[0,6,-20],[0,3.3,0],1.3],
 ['side',[-20,3.5,0],[0,3.3,0],1.3],
 ['back',[0,8,20],[0,3.3,0],1.3]
]){const r=await call('set_camera_angle',{position,target,projection:'orthographic',zoom});const p=r.content.find(c=>c.type==='image');if(p)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(p.data,'base64'));}
await call('set_camera_angle',{position:[-10,6,-15],target:[0,2.3,-1],projection:'orthographic',zoom:1.65});
console.log('Saved revised model and previews');
