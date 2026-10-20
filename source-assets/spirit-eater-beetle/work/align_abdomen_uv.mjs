import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
console.log(getText(await call('risky_eval',{code:`(()=>{const m=Mesh.all.find(m=>m.name.includes('腹部_青玉色薄膜'));Undo.initEdit({elements:[m]});let n=0;
for(const f of Object.values(m.faces)){const vs=f.vertices.map(k=>m.vertices[k]),cy=vs.reduce((a,p)=>a+p[1]/vs.length,0),cz=vs.reduce((a,p)=>a+p[2]/vs.length,0);if(cy<1.4||cz<2.35)continue;const back=(cz-1.89)/2.36>.58;
for(const k of f.vertices){const p=m.vertices[k];f.uv[k]=back?[1257-Math.abs(p[0])/2.02*121,602-(p[1]-2.772)*73]:[Math.min(1040,Math.max(773,760+p[2]/.014)),Math.min(689,Math.max(479,790-p[1]/.014))];}n++;}
Canvas.updateAll();Undo.finishEdit('按实际顶点对齐尾腹原画UV');return {faces:n}})()`})));
for(const[name,position]of [['tail_fixed_left',[-18,6,8]],['tail_fixed_right',[18,6,8]],['tail_fixed_back',[0,6,20]]]){const r=await call('set_camera_angle',{position,target:[0,2.6,2.5],zoom:2.8,projection:'orthographic'});const pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
