import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
console.log(getText(await call('risky_eval',{code:`(()=>{
const tails=Mesh.all.filter(m=>m.name.startsWith('carapace_')&&/尾背甲|尾侧甲|尾端护片/.test(m.name));Undo.initEdit({elements:tails});const c=[0,2.772,1.89],r=[2.02,2.02,2.36];
function project(p,len){const v=p.map((x,i)=>(x-c[i])/r[i]),l=Math.hypot(...v);return v.map((x,i)=>c[i]+x/l*r[i]*len);}
for(const m of tails.filter(m=>m.name.includes('左_'))){
 const ps=Object.values(m.vertices),mn=Math.min(...ps.map(p=>Math.abs(p[0]))),mx=Math.max(...ps.map(p=>Math.abs(p[0]))),center=ps.reduce((a,p)=>a.map((v,i)=>v+p[i]/ps.length),[0,0,0]);
 for(const[k,old]of Object.entries(m.vertices)){
 let p=old.slice(),len=Math.hypot(...p.map((v,i)=>(v-c[i])/r[i]));
 if(/尾背甲第二片|尾背甲第三片/.test(m.name)){p[0]=-Math.max(0,(Math.abs(p[0])-mn)/(mx-mn)*mx);}
 if(/尾侧甲第四片|尾端护片/.test(m.name)){const growth=m.name.includes('尾端')?1.18:1.13;p=p.map((v,i)=>center[i]+(v-center[i])*growth);if(m.name.includes('尾端'))p[0]-=.14*Math.max(0,Math.min(1,(p[1]-2.15)/.7));}
 m.vertices[k]=project(p,len);
 }
 const right=tails.find(t=>t.name===m.name.replace('左_','右_'));right.vertices=Object.fromEntries(Object.entries(m.vertices).map(([k,p])=>[k,[-p[0],p[1],p[2]]]));right.faces={};for(const f of Object.values(m.faces))right.addFaces(new MeshFace(right,{vertices:f.vertices.slice().reverse(),uv:JSON.parse(JSON.stringify(f.uv)),texture:f.texture}));
}
Canvas.updateAll();Undo.finishEdit('延伸原尾甲边缘封闭顶部和侧面接缝');return {tailPlates:tails.length};})()`})));
for(const[name,position]of [['tail_closed_left',[-18,6,8]],['tail_closed_right',[18,6,8]],['tail_closed_back',[0,6,20]],['tail_closed_top',[-12,11,15]]]){const r=await call('set_camera_angle',{position,target:[0,3,2.8],zoom:3.3,projection:'orthographic'});const pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
