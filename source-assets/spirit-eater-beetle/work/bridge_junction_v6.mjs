import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const d='E:/五行仙途/source-assets/spirit-eater-beetle';
const baseline=JSON.parse(await fs.readFile(d+'/before_thorax_junction_v6_20261007.bbmodel','utf8'));const old=baseline.elements.find(m=>m.name==='贴合腹甲_1');
const result=JSON.parse(getText(await call('risky_eval',{code:`(()=>{
if(!Project.name.includes('噬灵虫'))throw Error('Wrong project');
const m=Mesh.all.find(m=>m.name==='贴合腹甲_1'),chest=Mesh.all.find(m=>m.name.includes('胸部连续底甲')),surfaces=Mesh.all.filter(m=>/腹部_青玉色薄膜|腹部_透明保护膜/.test(m.name)),tex=Texture.all.find(t=>t.name==='腹甲_弧缘分节UV_v6.png'),old=${JSON.stringify(old.vertices)};
if(!m||!chest||surfaces.length!==2||!tex)throw Error('Missing junction mesh');
function tris(meshes){const out=[];for(const s of meshes)for(const f of Object.values(s.faces)){const keys=f.getSortedVertices();for(let i=1;i<keys.length-1;i++)out.push([keys[0],keys[i],keys[i+1]].map(k=>new THREE.Vector3(...s.vertices[k])));}return out;}
const ct=tris([chest]),at=tris(surfaces);
function surfaceY(ts,x,z){const ray=new THREE.Ray(new THREE.Vector3(x,-10,z),new THREE.Vector3(0,1,0));let y=Infinity;for(const tr of ts){const h=ray.intersectTriangle(...tr,false,new THREE.Vector3());if(h)y=Math.min(y,h.y);}return y;}
function smoothMin(a,b,k){if(!Number.isFinite(a))return b;if(!Number.isFinite(b))return a;const h=Math.max(k-Math.abs(a-b),0)/k;return Math.min(a,b)-h*h*k*.25;}
const vertices={},uv={},faces={},N=17,J=9,report={name:m.name,rootEmbeddedVertices:0,changedMeshCount:1};
for(let j=0;j<J;j++){const r=j/(J-1);for(let i=0;i<N;i++){
 const t=-1+2*i/(N-1),last=old['v5_'+i],z=-.62+.93*r+.065*(1-Math.abs(t)**1.4),x=.72*t*(1-r)+last[0]*r,cy=surfaceY(ct,x,z),ay=surfaceY(at,x,z),sy=smoothMin(cy,ay,.09),k='v'+j+'_'+i;
 if(!Number.isFinite(sy))throw Error('Junction surface missing '+x+','+z);
 const rootBlend=Math.min(1,r/.28),offset=.048+.058*r*r;
 let y=sy-offset;
 if(Number.isFinite(cy))y=(cy+.028)*(1-rootBlend)+y*rootBlend;
 if(j===J-1)y=last[1];
 const thickness=j===0?.032:Math.max(.035,offset-.013);
 vertices[k]=[x,y,z];vertices[k+'b']=[x,y+thickness,z];
 const v=3+52*Math.max(0,(r-.16)/.84);uv[k]=[8+(t+1)*.5*496,v];uv[k+'b']=uv[k].slice();
 if(j===0&&y>cy)report.rootEmbeddedVertices++;
}}
const add=(ks,flip=false)=>{if(flip)ks.reverse();faces['f'+Object.keys(faces).length]=new MeshFace(m,{vertices:ks,texture:tex.uuid,uv:Object.fromEntries(ks.map(k=>[k,uv[k]]))});};
for(let j=0;j<J-1;j++)for(let i=0;i<N-1;i++){const ks=['v'+j+'_'+i,'v'+j+'_'+(i+1),'v'+(j+1)+'_'+(i+1),'v'+(j+1)+'_'+i];add(ks);add(ks.map(k=>k+'b'),true);}
const edge=[];for(let i=0;i<N;i++)edge.push('v0_'+i);for(let j=1;j<J;j++)edge.push('v'+j+'_'+(N-1));for(let i=N-2;i>=0;i--)edge.push('v'+(J-1)+'_'+i);for(let j=J-2;j>0;j--)edge.push('v'+j+'_0');
for(let i=0;i<edge.length;i++){const a=edge[i],b=edge[(i+1)%edge.length],ks=[b,a,a+'b',b+'b'];faces['f'+Object.keys(faces).length]=new MeshFace(m,{vertices:ks,texture:tex.uuid,uv:Object.fromEntries(ks.map(k=>[k,[uv[k][0],k.endsWith('b')?63:56]]))});}
Undo.initEdit({elements:[m]});m.vertices=vertices;m.faces=faces;Canvas.updateAll();Undo.finishEdit('胸腹连接：首节腹甲前伸并嵌入胸底甲，平滑遮住断口');
report.vertices=Object.keys(vertices).length;report.faces=Object.keys(faces).length;return report;
})()`})));await fs.writeFile(d+'/work/junction_report_v6.json',JSON.stringify(result,null,2));console.log(JSON.stringify(result));
await call('export_model',{codec_id:'project',path:d+'/work/v6_stage3.bbmodel',overwrite:true,max_content_length:0});
for(const[n,p,t,z]of[['junction_bottom',[0,-20,-.2],[0,1.7,-.2],6],['junction_oblique',[-8,-16,10],[0,1.7,-.1],5]]){const r=await call('set_camera_angle',{view:'beetle_v6',position:p,target:t,zoom:z,projection:'orthographic'});await fs.writeFile(d+'/work/v6_after_'+n+'.png',Buffer.from(r.content.find(c=>c.type==='image').data,'base64'));}
