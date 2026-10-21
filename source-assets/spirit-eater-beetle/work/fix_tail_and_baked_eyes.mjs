import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
await call('export_model',{codec_id:'project',path:dir+'/before_tail_fit.bbmodel',overwrite:true,max_content_length:0});
console.log(getText(await call('risky_eval',{code:`(()=>{
const front=Mesh.all.filter(m=>m.name.includes('前胸盾片'));
const tails=Mesh.all.filter(m=>m.name.startsWith('carapace_')&&/尾背甲|尾侧甲|尾端护片/.test(m.name));
Undo.initEdit({elements:[...front,...tails]});let fixed=0;
for(const m of front)for(const f of Object.values(m.faces)){
 const u=Object.values(f.uv);const minX=Math.min(...u.map(v=>v[0])),maxX=Math.max(...u.map(v=>v[0])),minY=Math.min(...u.map(v=>v[1])),maxY=Math.max(...u.map(v=>v[1]));
 if(minX<544&&maxX>502&&minY<652&&maxY>618){f.vertices.forEach((k,i)=>f.uv[k]=[[538,563],[557,558],[564,577],[541,583]][i]);fixed++;}
}
for(const m of tails){
 const keys=Object.keys(m.vertices),N=(keys.length-1)/3,c=[0,2.772,1.89],r=[2.02,2.02,2.36];
 for(let i=0;i<keys.length;i++){
  const p=m.vertices[keys[i]],v=p.map((x,a)=>(x-c[a])/r[a]),len=Math.hypot(...v);
  const isBack=i>2*N;
  const scale=isBack?1.009:(i===2*N?1.039:1.024);
  for(let a=0;a<3;a++)p[a]=c[a]+v[a]/len*r[a]*scale;
 }
 const mx=Math.max(...Object.values(m.vertices).map(v=>Math.abs(v[0]))),mn=Math.min(...Object.values(m.vertices).map(v=>Math.abs(v[0]))),ys=Object.values(m.vertices).map(v=>v[1]),y0=Math.min(...ys),y1=Math.max(...ys);
 for(const f of Object.values(m.faces))for(const k of f.vertices){const v=m.vertices[k];f.uv[k]=[759+(Math.abs(v[0])-mn)/Math.max(.01,mx-mn)*44,500+(y1-v[1])/Math.max(.01,y1-y0)*40];}
}
Canvas.updateAll();Undo.finishEdit('尾甲贴合腹部并移除前盾重复眼点');return {repairedBakedEyeFaces:fixed,tailPlates:tails.length};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['detail_eye_side',[-12,5,-12],[-.4,2.3,-3.3],5],['detail_eyes',[0,3.5,-15],[0,2.2,-3.4],5],['detail_tail',[-12,7,15],[0,3.2,2.7],2.8],['detail_rear',[0,8,20],[0,3.5,2.5],2.5],['perspective',[-13,9,-16],[0,3.3,0],1.3],['front',[0,6,-20],[0,3.3,0],1.3],['side',[-20,3.5,0],[0,3.3,0],1.3],['back',[0,8,20],[0,3.3,0],1.3]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});let pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
await call('set_camera_angle',{position:[-12,7,15],target:[0,3.2,2],zoom:1.8,projection:'orthographic'});
