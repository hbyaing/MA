import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
const before=JSON.parse(await fs.readFile(dir+'/before_curved_mandibles.bbmodel','utf8'));
const ds=before.elements.filter(e=>!e.name.includes('mandible')).map(({name,faces})=>({name,faces}));
console.log(getText(await call('risky_eval',{code:`(()=>{const ds=${JSON.stringify(ds)};const ms=ds.map(d=>Mesh.all.find(m=>m.name===d.name)).filter(Boolean);Undo.initEdit({elements:ms});for(const d of ds){const m=Mesh.all.find(m=>m.name===d.name);if(!m)continue;for(const [k,f]of Object.entries(d.faces)){if(m.faces[k])m.faces[k].uv=f.uv;}}Canvas.updateAll();Undo.finishEdit('恢复非目标 UV，定位重复眼点');return Mesh.all.filter(m=>m.parent?.name==='head_头部').map(m=>({name:m.name,uuid:m.uuid}));})()`})));
await call('set_camera_angle',{position:[-12,5,-12],target:[-.4,2.3,-3.3],zoom:5,projection:'orthographic'});
for(const test of ['额甲','头部底壳','前胸盾片','胸部连续底甲']){
 await call('risky_eval',{code:`(()=>{for(const m of Mesh.all){if(m.name.includes(${JSON.stringify(test)}))m.visibility=false;}Canvas.updateAll();return true;})()`});
 const r=await call('capture_screenshot');const p=r.content.find(c=>c.type==='image');if(p)await fs.writeFile(dir+'/work/hide_'+test+'.png',Buffer.from(p.data,'base64'));
 await call('risky_eval',{code:`(()=>{for(const m of Mesh.all){if(m.name.includes(${JSON.stringify(test)}))m.visibility=true;}Canvas.updateAll();return true;})()`});
}
