import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
console.log(getText(await call('risky_eval',{code:`(()=>{
const tails=Mesh.all.filter(m=>m.name.startsWith('carapace_')&&/尾背甲|尾侧甲|尾端护片/.test(m.name)),belly=Mesh.all.find(m=>m.name.includes('腹部_青玉色薄膜'));
const armor=Texture.all.find(t=>t.name==='背面参考_甲壳.png'),glow=Texture.all.find(t=>t.name==='背面参考_灵光.png');if(tails.length!==10||!armor||!glow)throw Error('Rear targets missing');
Undo.initEdit({elements:[...tails,belly],textures:[armor,glow]});for(const t of [armor,glow]){t.uv_width=t.width;t.uv_height=t.height;}
const sx=.00715,sy=4.04/682;
const defs=[
['尾背甲第一片',[[768,219],[686,248],[588,299],[533,373],[484,459],[454,553],[510,501],[572,439],[630,359],[700,330],[768,301]]],
['尾背甲第二片',[[768,312],[701,338],[636,383],[660,454],[716,430],[768,412]]],
['尾背甲第三片',[[768,408],[681,438],[666,536],[719,518],[768,506]]],
['尾侧甲第四片',[[768,502],[702,524],[715,627],[746,615],[768,607]]],
['尾端护片',[[768,602],[729,626],[740,675],[768,734]]]
];
function pos(uv,back){const x=(uv[0]-768)*sx,y=4.792-(uv[1]-218)*sy;
const z=1.89+2.36*Math.sqrt(Math.max(.003,1-(x/2.02)**2-((y-2.772)/2.02)**2));return[x,y,z+(back?-.025:.045)+(!back?.045*Math.exp(-Math.abs(x)*5):0)];}
function build(m,poly,s){const xy=poly.map(([u,v])=>new THREE.Vector2(u,v));let tris=THREE.ShapeUtils.triangulateShape(xy,[]).map(t=>t.map(i=>poly[i]));
for(let pass=0;pass<3;pass++){const next=[];for(const[a,b,c]of tris){const ab=a.map((v,i)=>(v+b[i])/2),bc=b.map((v,i)=>(v+c[i])/2),ca=c.map((v,i)=>(v+a[i])/2);next.push([a,ab,ca],[ab,b,bc],[ca,bc,c],[ab,bc,ca]);}tris=next;}
const vertices={},faces={},lookup=new Map();let next=0;
function vertex(uv,back){const sig=uv.map(v=>v.toFixed(7)).join(',')+'/'+back;let k=lookup.get(sig);if(!k){k='v'+next++;const p=pos(uv,back);p[0]*=s;vertices[k]=p;lookup.set(sig,k);}return k;}
function face(uvs,back,wall=false){let vs=uvs.map(uv=>vertex(uv,back)),mapping=Object.fromEntries(vs.map((k,i)=>[k,uvs[i]]));if(wall)return;const pts=vs.map(k=>new THREE.Vector3(...vertices[k]));const nz=pts[1].clone().sub(pts[0]).cross(pts[2].clone().sub(pts[0])).z;if((!back&&nz<0)||(back&&nz>0))vs.reverse();faces['f'+Object.keys(faces).length]={vertices:vs,uv:mapping,texture:armor.uuid};}
for(const tri of tris){face(tri,false);face(tri,true);}
for(let i=0;i<poly.length;i++){const a=poly[i],b=poly[(i+1)%poly.length];for(let j=0;j<8;j++){const u=a.map((v,k)=>v+(b[k]-v)*j/8),v=a.map((n,k)=>n+(b[k]-n)*(j+1)/8),ks=[vertex(u,false),vertex(v,false),vertex(v,true),vertex(u,true)];if(s<0)ks.reverse();faces['f'+Object.keys(faces).length]={vertices:ks,uv:Object.fromEntries(ks.map(k=>[k,[740,285]])),texture:armor.uuid};}}
m.vertices=vertices;m.faces={};for(const[k,f]of Object.entries(faces))m.faces[k]=new MeshFace(m,f);
}
for(const [suffix,poly]of defs){build(tails.find(m=>m.name==='carapace_层叠背甲__左_'+suffix),poly,1);build(tails.find(m=>m.name==='carapace_层叠背甲__右_'+suffix),poly,-1);}
let rearFaces=0;for(const f of Object.values(belly.faces)){const c=f.vertices.reduce((a,k)=>a+belly.vertices[k][2]/f.vertices.length,0);if(c<2.2)continue;f.texture=glow.uuid;for(const k of f.vertices){const p=belly.vertices[k];f.uv[k]=[768+p[0]/sx,218+(4.792-p[1])/sy];}rearFaces++;}
Canvas.updateAll();Undo.finishEdit('按新背视图重建肩甲与递减中脊甲片');return {rearPlates:tails.length,rearAbdomenFaces:rearFaces};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['rear_reference_straight',[0,2.772,20],[0,2.772,1.89],2.1],['rear_reference_oblique',[-12,7,15],[0,3,2.5],2.1],['rear_reference_side',[-20,4,2],[0,2.8,1.6],2.1]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});const pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
