import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
const baseline=JSON.parse(await fs.readFile(dir+'/before_abdomen_v6_20261007.bbmodel','utf8'));
const originals=baseline.elements.filter(e=>/腹部_青玉|腹部_透明保护/.test(e.name)).map(e=>({uuid:e.uuid,vertices:e.vertices,faces:e.faces}));
const state=JSON.parse(getText(await call('risky_eval',{code:`(()=>({name:Project.name,textures:Texture.all.map(t=>t.name)}))()`})));
if(!state.name.includes('噬灵虫'))throw Error('Unexpected project');
for(const[name,w,h,mode]of [['腹甲_原图分节UV_v6.png',512,512,'default'],['腹部_柔和灵光UV_v6.png',1536,768,'emissive']])if(!state.textures.includes(name))await call('create_texture',{name,data:dir+'/'+name,uv_width:w,uv_height:h,render_mode:mode,render_sides:'front'});
const code=`(()=>{
if(!Project.name.includes('噬灵虫'))throw Error('Wrong project');
const originals=${JSON.stringify(originals)};
const body=Mesh.all.find(m=>m.name.includes('腹部_青玉色薄膜')),membrane=Mesh.all.find(m=>m.name.includes('腹部_透明保护膜')),plates=Mesh.all.filter(m=>/^贴合腹甲_[1-7]$/.test(m.name)),seam=Mesh.all.find(m=>m.name==='尾腹_贴体中央细缝');
if(!body||!membrane||plates.length!==7||!seam)throw Error('Missing required abdomen parts');
const skin=Texture.all.find(t=>t.name==='腹部_柔和灵光UV_v6.png'),chitin=Texture.all.find(t=>t.name==='腹甲_原图分节UV_v6.png'),armor=Texture.all.find(t=>t.name==='背面参考_甲壳.png');
Undo.initEdit({elements:[body,membrane,...plates,seam]});
const report={body:[],plates:[],rayMisses:0};
function deform(p){const t=Math.max(0,Math.min(1,(2.772-p[1])/2.02));return[p[0]*(1-.12*t**1.2),p[1]+.16*t*t,1.89+(p[2]-1.89)*(1-.035*t)];}
for(const m of[body,membrane]){
 const old=originals.find(o=>o.uuid===m.uuid),verts={},map={},lookup=new Map();
 for(const[k,p]of Object.entries(old.vertices)){const v=deform(p),sig=v.map(x=>x.toFixed(5)).join(',');if(!lookup.has(sig)){lookup.set(sig,k);verts[k]=v;}map[k]=lookup.get(sig);}
 const faces={};for(const[k,f]of Object.entries(old.faces)){
  const keys=[...new Set(f.vertices.map(v=>map[v]))];if(keys.length<3)continue;
  const uv={};for(const id of keys){const oldKeys=f.vertices.filter(v=>map[v]===id);uv[id]=oldKeys.reduce((a,v)=>[a[0]+f.uv[v][0]/oldKeys.length,a[1]+f.uv[v][1]/oldKeys.length],[0,0]);}
  faces[k]=new MeshFace(m,{vertices:keys,uv,texture:m===body?skin.uuid:Texture.all.find(t=>t.name==='腹部_透明表膜.png').uuid});
 }
 m.vertices=verts;m.faces=faces;report.body.push({name:m.name,oldVertices:Object.keys(old.vertices).length,newVertices:Object.keys(verts).length,faces:Object.keys(faces).length});
}
const triangles=[];for(const f of Object.values(body.faces)){const keys=f.getSortedVertices();for(let i=1;i<keys.length-1;i++)triangles.push([keys[0],keys[i],keys[i+1]].map(k=>new THREE.Vector3(...body.vertices[k])));}
function underside(x,z){const ray=new THREE.Ray(new THREE.Vector3(x,-10,z),new THREE.Vector3(0,1,0));let best=Infinity;for(const tri of triangles){const h=ray.intersectTriangle(...tri,false,new THREE.Vector3());if(h&&h.y<best)best=h.y;}if(!Number.isFinite(best)){report.rayMisses++;throw Error('No ventral surface at '+x+','+z);}return best;}
const intervals=[[-.30,.27],[.24,.79],[.76,1.31],[1.28,1.84],[1.81,2.37],[2.34,2.89],[2.86,3.39]];
for(let row=0;row<7;row++){
 const m=plates.find(m=>m.name==='贴合腹甲_'+(row+1)),[z0,z1]=intervals[row],N=11,J=4,vertices={},faces={},uv={};
 for(let j=0;j<J;j++){const r=j/(J-1);for(let i=0;i<N;i++){
  const t=-1+2*i/(N-1),z=z0+(z1-z0)*r-.065*Math.abs(t)**1.6*r;
  const radial=2.02*Math.sqrt(Math.max(.01,1-((z-1.89)/2.36)**2));
  const fraction=.57-.095*Math.max(0,(z-.3)/3.09),terminal=row===6?1-.48*r*r:1;
  const x=radial*fraction*t*terminal;
  const y=underside(x,z)-.034-.014*Math.sin(Math.PI*r),k='v'+j+'_'+i;
  vertices[k]=[x,y,z];vertices[k+'b']=[x,y+.026,z];
  uv[k]=[8+(t+1)*.5*496,row*64+5+r*54];uv[k+'b']=uv[k].slice();
 }}
 const add=(keys,flip=false)=>{if(flip)keys.reverse();faces['f'+Object.keys(faces).length]=new MeshFace(m,{vertices:keys,texture:chitin.uuid,uv:Object.fromEntries(keys.map(k=>[k,uv[k]]))});};
 for(let j=0;j<J-1;j++)for(let i=0;i<N-1;i++){const ks=['v'+j+'_'+i,'v'+j+'_'+(i+1),'v'+(j+1)+'_'+(i+1),'v'+(j+1)+'_'+i];add(ks);add(ks.map(k=>k+'b'),true);}
 const edge=[];for(let i=0;i<N;i++)edge.push('v0_'+i);for(let j=1;j<J;j++)edge.push('v'+j+'_'+(N-1));for(let i=N-2;i>=0;i--)edge.push('v'+(J-1)+'_'+i);for(let j=J-2;j>0;j--)edge.push('v'+j+'_0');
 for(let i=0;i<edge.length;i++){const a=edge[i],b=edge[(i+1)%edge.length];add([b,a,a+'b',b+'b']);}
 m.vertices=vertices;m.faces=faces;report.plates.push({name:m.name,vertices:Object.keys(vertices).length,faces:Object.keys(faces).length,maxHalfWidth:Math.max(...Object.values(vertices).map(p=>Math.abs(p[0])))});
}

function back(x,y){const ray=new THREE.Ray(new THREE.Vector3(x,y,20),new THREE.Vector3(0,0,-1));let best=-Infinity;for(const tri of triangles){const h=ray.intersectTriangle(...tri,false,new THREE.Vector3());if(h&&h.z>best)best=h.z;}return best;}
const v={},f={},N=24;
for(let i=0;i<=N;i++){const y=1.03+(3.78-1.03)*i/N,w=.007+.008*Math.sin(Math.PI*i/N);for(const[s,x]of[['l',-w],['r',w]]){const z=back(x,y);if(!Number.isFinite(z))throw Error('Missing rear seam surface');v[s+i]=[x,y,z+.025];}}
for(let i=0;i<N;i++){const ks=['l'+i,'r'+i,'r'+(i+1),'l'+(i+1)];f['f'+i]=new MeshFace(seam,{vertices:ks,texture:armor.uuid,uv:Object.fromEntries(ks.map(k=>[k,[768,805]]))});}
seam.vertices=v;seam.faces=f;
Canvas.updateAll();Undo.finishEdit('v6 腹部下缘收束、七节窄腹甲重建、贴体接缝与原图微光');return report;
})()`;
const report=JSON.parse(getText(await call('risky_eval',{code})));await fs.writeFile(dir+'/work/abdomen_geometry_report_v6.json',JSON.stringify(report,null,2));console.log(JSON.stringify(report));
await call('export_model',{codec_id:'project',path:dir+'/work/v6_stage1.bbmodel',overwrite:true,max_content_length:0});
