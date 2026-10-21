import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
await call('export_model',{codec_id:'project',path:dir+'/before_underside_revision.bbmodel',overwrite:true,max_content_length:0});
const parts=[];
function plate(name,z0,z1){
 const vertices=[],uv=[],faces=[],N=11,J=5;
 for(let j=0;j<J;j++){const z=z0+(z1-z0)*j/(J-1),k=Math.sqrt(Math.max(.008,1-((z-1.89)/2.36)**2));for(let i=0;i<N;i++){let t=-1+2*i/(N-1),x=2.02*k*.85*t,y=2.772-2.02*Math.sqrt(Math.max(.004,1-(x/2.02)**2-((z-1.89)/2.36)**2))-.04;vertices.push([x,y,z]);uv.push([758+i/(N-1)*44,507+j/(J-1)*38]);}}
 const outer=vertices.length;vertices.push(...vertices.map(v=>[v[0]*.996,v[1]+.075,v[2]]));uv.push(...uv.map(p=>p.slice()));
 for(let j=0;j<J-1;j++)for(let i=0;i<N-1;i++){let a=j*N+i;faces.push([a,a+1,a+N+1,a+N]);faces.push([a+outer,a+N+outer,a+N+1+outer,a+1+outer]);}
 let boundary=[];for(let i=0;i<N;i++)boundary.push(i);for(let j=1;j<J;j++)boundary.push(j*N+N-1);for(let i=N-2;i>=0;i--)boundary.push((J-1)*N+i);for(let j=J-2;j>0;j--)boundary.push(j*N);
 for(let i=0;i<boundary.length;i++){let a=boundary[i],b=boundary[(i+1)%boundary.length];faces.push([a,a+outer,b+outer,b]);}
 parts.push({name,group:'ventral_腹甲',vertices,faces,uvs:faces.map(f=>f.map(i=>uv[i]))});
}
for(let i=0;i<7;i++)plate('贴合腹甲_'+(i+1),-.28+i*.60,.36+i*.60);
const vertices=[],faces=[];
for(let j=0;j<15;j++){let y=1.13+j/14*3.35,z=1.89+2.36*Math.sqrt(Math.max(0,1-((y-2.772)/2.02)**2))+.012;vertices.push([-.023,y,z],[.023,y,z]);if(j)faces.push([2*j-2,2*j,2*j+1,2*j-1]);}
parts.push({name:'尾腹_贴体中央细缝',group:'rear_suture_尾部中央接缝',vertices,faces,uvs:faces.map(f=>f.map((_,i)=>[[781,518],[781,521],[784,521],[784,518]][i]))});
console.log(getText(await call('risky_eval',{code:`(()=>{
const targets=Mesh.all.filter(m=>m.name.includes('腹面节甲')||m.name.includes('尾腹中央细脊')||m.name.includes('腹部_青玉色薄膜')||m.name.includes('胸部连续底甲'));
Undo.initEdit({elements:targets,outliner:true});
for(const m of targets){
 if(m.name.includes('腹面节甲')||m.name.includes('尾腹中央细脊')){m.remove();continue;}
 if(m.name.includes('青玉色')){
 for(const f of Object.values(m.faces)){const c=f.vertices.map(k=>m.vertices[k]).reduce((a,v)=>a.map((x,i)=>x+v[i]/f.vertices.length),[0,0,0]);for(const k of f.vertices){const p=m.vertices[k];const u=Math.abs(c[0])>.85?(p[2]-1.89)/2.36:p[0]/2.02;f.uv[k]=[925+u*54,611-(p[1]-2.772)/2.02*34];}}
 }else{for(const f of Object.values(m.faces))f.vertices.forEach((k,i)=>f.uv[k]=[[762,510],[792,510],[792,534],[762,534]][i]);}
}
Canvas.updateAll();Undo.finishEdit('移除悬空腹甲，修正腹膜透明穿洞');return {removedFloatingPlates:6};})()`})));
for(const p of parts)await call('place_mesh',{group:p.group,texture:'噬灵虫_三视图原画纹理.png',elements:[{name:p.name,vertices:p.vertices,faces:p.faces}]});
console.log(getText(await call('risky_eval',{code:`(()=>{const ds=${JSON.stringify(parts.map(({name,uvs})=>({name,uvs})))};const ms=ds.map(d=>Mesh.all.find(m=>m.name===d.name));Undo.initEdit({elements:ms});ds.forEach((d,i)=>Object.values(ms[i].faces).forEach((f,j)=>f.vertices.forEach((k,v)=>f.uv[k]=d.uvs[j][v])));Canvas.updateAll();Undo.finishEdit('贴体腹甲搭接');return {meshes:Mesh.all.length,eyes:Mesh.all.filter(m=>m.name.includes('微光复眼')).length,legs:Group.all.filter(g=>/^(left|right)_(front|middle|hind)_/.test(g.name)&&!g.name.includes('segment')).map(g=>g.name)};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['bottom',[0,-20,0],[0,2,0],1.5],['bottom_oblique',[-12,-8,12],[0,2,0],1.5],['perspective',[-13,9,-16],[0,3.3,0],1.3],['front',[0,6,-20],[0,3.3,0],1.3],['side',[-20,3.5,0],[0,3.3,0],1.3],['back',[0,8,20],[0,3.3,0],1.3],['detail_rear',[0,8,20],[0,3.5,2.5],2.5]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});let pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
await call('set_camera_angle',{position:[-13,9,-16],target:[0,3.3,0],projection:'orthographic',zoom:1.3});
