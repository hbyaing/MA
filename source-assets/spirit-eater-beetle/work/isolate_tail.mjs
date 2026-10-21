import fs from 'node:fs/promises';import{connect,call}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle/work';
await call('set_camera_angle',{position:[-18,6,8],target:[0,2.6,2.5],zoom:2.8,projection:'orthographic'});
try{
await call('risky_eval',{code:`(()=>{for(const m of Mesh.all)if(m.name.includes('腹部_青玉色薄膜')||m.name.includes('腹部_透明保护膜'))m.mesh.visible=false;return true})()`});
const r=await call('capture_screenshot');const p=r.content.find(c=>c.type==='image');await fs.writeFile(dir+'/tail_without_abdomen.png',Buffer.from(p.data,'base64'));
}finally{await call('risky_eval',{code:'(()=>{Canvas.updateAll();return true})()'});}
