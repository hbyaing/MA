import fs from 'node:fs/promises';
import {connect,call,getText} from './mcp.mjs';
await connect();
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
const baseline=JSON.parse(await fs.readFile(dir+'/before_optimization_v4_20261007.bbmodel','utf8'));
const source=baseline.elements.filter(e=>/腹部_青玉|透明保护膜/.test(e.name));
const code=`(()=>{
if(!Project.name.includes('噬灵虫'))throw Error('Wrong project');
const tails=Mesh.all.filter(m=>m.name.startsWith('carapace_')&&/尾背甲|尾侧甲|尾端护片/.test(m.name));
const bellies=Mesh.all.filter(m=>/腹部_青玉|透明保护膜/.test(m.name));
const armor=Texture.all.find(t=>t.name==='背面参考_甲壳.png');
if(tails.length!==10||bellies.length!==2||!armor)throw Error('Missing expected parts');
const previous=${JSON.stringify(source.map(e=>({uuid:e.uuid,vertices:e.vertices})))};
Undo.initEdit({elements:[...tails,...bellies]});
const sx=.00715,sy=4.04/682,center=new THREE.Vector3(0,2.772,1.89);
const defs=[
['尾背甲第一片',[[768,219],[686,248],[588,299],[533,373],[484,459],[454,553],[510,501],[572,439],[630,359],[700,330],[768,301]]],
['尾背甲第二片',[[768,312],[701,338],[636,383],[660,454],[716,430],[768,412]]],
['尾背甲第三片',[[768,408],[681,438],[666,536],[719,518],[768,506]]],
['尾侧甲第四片',[[768,502],[702,524],[715,627],[746,615],[768,607]]],
['尾端护片',[[768,602],[729,626],[740,675],[768,734]]]];
function position(uv,row){const v=uv[1]-(row===1?14*Math.max(0,(390-uv[1])/78):0);const x=(uv[0]-768)*sx,y=4.792-(v-218)*sy;const z=row===0?1.66+2.56*Math.sqrt(Math.max(.003,1-(x/2.30)**2-((y-2.72)/2.20)**2)):1.89+2.36*Math.sqrt(Math.max(.003,1-(x/2.02)**2-((y-2.772)/2.02)**2));return[x,y,z+.045+.045*Math.exp(-Math.abs(x)*5)];}
const leading=[[0,4.945,1.54],[-.58,4.54,1.49],[-1.17,4.055,1.41],[-1.6,3.56,1.24],[-1.86,3.245,1.09],[-2.06,2.42,.48]];
const report=[];
const frontUV=defs[0][1].slice(0,6),backUV=[defs[0][1][10],defs[0][1][9],defs[0][1][8],defs[0][1][7],defs[0][1][6],defs[0][1][5]];
function stripUV(section,q,r){const a=frontUV[section].map((v,k)=>v+(frontUV[section+1][k]-v)*q),b=backUV[section].map((v,k)=>v+(backUV[section+1][k]-v)*q);return a.map((v,k)=>v+(b[k]-v)*r);}
function remapFirst(uv){let best=null;for(let s=0;s<5;s++){let q=.5,r=.5;for(let it=0;it<12;it++){const p=stripUV(s,q,r),dq=stripUV(s,q+.0001,r).map((v,k)=>(v-p[k])/.0001),dr=stripUV(s,q,r+.0001).map((v,k)=>(v-p[k])/.0001);const a=uv[0]-p[0],b=uv[1]-p[1],det=dq[0]*dr[1]-dq[1]*dr[0];if(Math.abs(det)<1e-7)break;q+=(a*dr[1]-b*dr[0])/det;r+=(dq[0]*b-dq[1]*a)/det;}const qc=Math.max(0,Math.min(1,q)),rc=Math.max(0,Math.min(1,r)),p=stripUV(s,qc,rc),err=Math.hypot(p[0]-uv[0],p[1]-uv[1]);if(!best||err<best.err)best={s,q:qc,r:rc,err};}return stripUV(best.s,best.q,.4+.59*best.r);}

for(let row=0;row<defs.length;row++)for(const side of [1,-1]){
 const [suffix,poly]=defs[row],m=tails.find(m=>m.name==='carapace_层叠背甲__'+(side===1?'左_':'右_')+suffix);
 const vertices={},faces={},lookup=new Map(),outer=[];
 function add(p,uv){const sig=p.map(v=>v.toFixed(7)).join(',');if(lookup.has(sig))return lookup.get(sig);const k='v'+lookup.size;vertices[k]=[p[0]*side,p[1],p[2]];lookup.set(sig,k);return k;}
 function triangle(ps,uvs){let ks=ps.map((p,i)=>add(p,uvs[i]));const v=ks.map(k=>new THREE.Vector3(...vertices[k]));const n=v[1].clone().sub(v[0]).cross(v[2].clone().sub(v[0]));const c=v.reduce((a,p)=>a.add(p),new THREE.Vector3()).multiplyScalar(1/3).sub(center);if(n.dot(c)<0)ks.reverse();outer.push({vertices:ks,uv:Object.fromEntries(ps.map((p,i)=>[add(p,uvs[i]),uvs[i]]))});}
 let tris=THREE.ShapeUtils.triangulateShape(poly.map(p=>new THREE.Vector2(...p)),[]).map(t=>t.map(i=>poly[i]));
 for(let pass=0;pass<2;pass++){const next=[];for(const[a,b,c]of tris){const ab=a.map((v,i)=>(v+b[i])/2),bc=b.map((v,i)=>(v+c[i])/2),ca=c.map((v,i)=>(v+a[i])/2);next.push([a,ab,ca],[ab,b,bc],[ca,bc,c],[ab,bc,ca]);}tris=next;}
 for(const tri of tris)triangle(tri.map(p=>position(p,row)),row===0?tri.map(remapFirst):tri);
 if(row===0){
  const grid=[];for(let i=0;i<=20;i++){const section=Math.min(4,Math.floor(i/4)),q=(i-section*4)/4;const uv=poly[section].map((v,k)=>v+(poly[section+1][k]-v)*q);const a=leading[section].map((v,k)=>v+(leading[section+1][k]-v)*q),b=position(uv,0);const r=[];
   for(let j=0;j<=4;j++){const t=j/4,p=a.map((v,k)=>v+(b[k]-v)*t);p[1]+=.10*Math.sin(t*Math.PI);const inside=stripUV(section,q,.01+.39*t);r.push({p,uv:inside});}grid.push(r);}
  for(let i=0;i<20;i++)for(let j=0;j<4;j++){const a=grid[i][j],b=grid[i+1][j],c=grid[i+1][j+1],d=grid[i][j+1];triangle([a.p,b.p,c.p],[a.uv,b.uv,c.uv]);triangle([a.p,c.p,d.p],[a.uv,c.uv,d.uv]);}
 }
 const keys=Object.keys(vertices);for(const k of keys){const p=new THREE.Vector3(...vertices[k]);const n=p.clone().sub(center);n.x/=2.02**2;n.y/=2.02**2;n.z/=2.36**2;n.normalize();vertices[k+'b']=p.addScaledVector(n,-.060).toArray();}
 const edges=new Map();for(const f of outer){faces['f'+Object.keys(faces).length]={...f,texture:armor.uuid};const ks=f.vertices.map(k=>k+'b').reverse();faces['f'+Object.keys(faces).length]={vertices:ks,uv:Object.fromEntries(f.vertices.map(k=>[k+'b',f.uv[k]])),texture:armor.uuid};for(let i=0;i<3;i++){const a=f.vertices[i],b=f.vertices[(i+1)%3],sig=[a,b].sort().join('/');if(edges.has(sig))edges.get(sig).count++;else edges.set(sig,{a,b,count:1});}}
 for(const{a,b,count}of edges.values())if(count===1){const ks=[b,a,a+'b',b+'b'];faces['f'+Object.keys(faces).length]={vertices:ks,uv:Object.fromEntries(ks.map(k=>[k,[740,285]])),texture:armor.uuid};}
 m.vertices=vertices;m.faces={};for(const[k,f]of Object.entries(faces))m.faces[k]=new MeshFace(m,f);
 report.push({name:m.name,vertices:Object.keys(vertices).length,faces:Object.keys(faces).length});
}
// Tuck only the forward, upper abdomen beneath the existing armor envelope.
// The head, legs, antennae, jaws and original six front armor meshes are untouched.
const shells=Mesh.all.filter(m=>m.name.startsWith('carapace_')&&/主鞘甲|尾背甲第一片/.test(m.name));
const triangles=[];for(const m of shells)for(const f of Object.values(m.faces)){const ks=f.getSortedVertices();for(let i=1;i<ks.length-1;i++)triangles.push([ks[0],ks[i],ks[i+1]].map(k=>new THREE.Vector3(...m.vertices[k])));}
let tucked=0;
for(const m of bellies){const old=previous.find(e=>e.uuid===m.uuid);m.vertices=JSON.parse(JSON.stringify(old.vertices));for(const p of Object.values(m.vertices)){
 if(p[1]<2.95||p[2]>2.8)continue;
 const vec=new THREE.Vector3(...p).sub(center),len=vec.length(),direction=vec.clone().normalize(),ray=new THREE.Ray(center,direction);let best=Infinity;
 for(const tri of triangles){const hit=ray.intersectTriangle(...tri,false,new THREE.Vector3());if(hit){const d=hit.distanceTo(center);if(d>1.10&&d<best)best=d;}}
 if(best<len+.035){const q=center.clone().addScaledVector(direction,best-(m.name.includes('透明保护')?.023:.036));p.splice(0,3,...q.toArray());tucked++;}
}}
Canvas.updateAll();Undo.finishEdit('补齐主鞘甲与肩甲的曲面衔接，薄片贴体与尾甲拓扑优化');return {plates:report,tuckedVertices:tucked};})()`;
console.log(getText(await call('risky_eval',{code:code.replace(/^\s*\/\/.*$/gm,'')})));
await call('export_model',{codec_id:'project',path:dir+'/work/v4_geometry.bbmodel',overwrite:true,max_content_length:0});

for(const[name,position,target,zoom]of [['v4_side',[-20,3.8,2],[0,3.5,0],1.05],['v4_rear',[0,2.772,20],[0,3.4,1],1.05],['v4_oblique',[-12,7,15],[0,3.5,0],1.05],['v4_front',[-13,9,-16],[0,3.5,0],1.05],['v4_top',[0,23,1],[0,2,0],1.05]]){const r=await call('set_camera_angle',{view:'beetle_v4',position,target,zoom,projection:'orthographic'});const pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/work/'+name+'.png',Buffer.from(pic.data,'base64'));}


