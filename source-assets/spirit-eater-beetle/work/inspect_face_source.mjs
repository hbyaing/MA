import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();
const r=await call('risky_eval',{code:`(()=>{const t=Texture.all.find(t=>t.name==='噬灵虫_三视图原画纹理.png'),c=document.createElement('canvas');c.width=500;c.height=460;c.getContext('2d').drawImage(t.img,140,630,125,115,0,0,500,460);return c.toDataURL()})()`});let s=JSON.parse(getText(r));await fs.writeFile('E:/五行仙途/source-assets/spirit-eater-beetle/work/reference_face.png',Buffer.from(s.split(',')[1],'base64'));

