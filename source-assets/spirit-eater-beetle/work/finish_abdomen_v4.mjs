import fs from 'node:fs/promises';import {connect,call,getText} from './mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
const old=JSON.parse(await fs.readFile(dir+'/before_optimization_v4_20261007.bbmodel','utf8')).elements.find(e=>e.name.includes('腹部_青玉色薄膜'));
const state=JSON.parse(getText(await call('risky_eval',{code:"Texture.all.some(t=>t.name==='腹部_连续环绕UV_v4.png')"})));
if(!state)await call('create_texture',{name:'腹部_连续环绕UV_v4.png',data:dir+'/腹部_连续环绕UV_v4.png',uv_width:1536,uv_height:768,render_mode:'emissive',render_sides:'front'});
console.log(getText(await call('risky_eval',{code:`(()=>{
const belly=Mesh.all.find(m=>m.name.includes('腹部_青玉色薄膜')),seam=Mesh.all.find(m=>m.name==='尾腹_贴体中央细缝'),tex=Texture.all.find(t=>t.name==='腹部_连续环绕UV_v4.png'),armor=Texture.all.find(t=>t.name==='背面参考_甲壳.png');
Undo.initEdit({elements:[belly,seam]});
const old=${JSON.stringify(old.faces)};
for(const [key,f]of Object.entries(belly.faces)){f.texture=tex.uuid;for(const k of f.vertices)f.uv[k]=old[key].uv[k].map(v=>v*1.5);}
const vertices={},faces={},N=32;
for(let i=0;i<=N;i++){const y=.81+(3.93-.81)*i/N,z=1.89+2.36*Math.sqrt(Math.max(0,1-((y-2.772)/2.02)**2))+.046,w=.009+.012*Math.sin(Math.PI*i/N);vertices['l'+i]=[-w,y,z];vertices['r'+i]=[w,y,z];}
for(let i=0;i<N;i++){const ks=['l'+i,'r'+i,'r'+(i+1),'l'+(i+1)];faces['f'+i]={vertices:ks,texture:armor.uuid,uv:Object.fromEntries(ks.map(k=>[k,[768,805]]))};}
seam.vertices=vertices;seam.faces={};for(const[k,f]of Object.entries(faces))seam.faces[k]=new MeshFace(seam,f);
Canvas.updateAll();Undo.finishEdit('腹部连续皮膜去除折返拉伸，重贴尾腹细缝');return {abdomenFaces:Object.keys(belly.faces).length,seamFaces:Object.keys(seam.faces).length};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/work/v4_textured.bbmodel',overwrite:true,max_content_length:0});
