import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
const base=JSON.parse(await fs.readFile(dir+'/before_tail_fit.bbmodel','utf8'));
const fronts=base.elements.filter(e=>e.name.includes('前胸盾片')).map(({name,faces})=>({name,faces}));
console.log(getText(await call('risky_eval',{code:`(()=>{
 const eyes=Mesh.all.filter(m=>m.parent?.name.includes('_eye_'));const front=Mesh.all.filter(m=>m.name.includes('前胸盾片'));Undo.initEdit({elements:[...eyes,...front],outliner:true});
 for(const m of eyes)m.remove();
 const ds=${JSON.stringify(fronts)};for(const d of ds){const m=front.find(m=>m.name===d.name);for(const [k,f]of Object.entries(d.faces))m.faces[k].uv=f.uv;}
 Canvas.updateAll();Undo.finishEdit('移除外加眼片，保留原画的一对眼睛');return {removedExtraEyeMeshes:eyes.length,retainedReferenceEyes:2};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['detail_eye_side',[-12,5,-12],[-.4,2.3,-3.3],5],['detail_eyes',[0,3.5,-15],[0,2.2,-3.4],5],['detail_mouth',[0,3.9,-16],[0,1.95,-3.6],4.2],['perspective',[-13,9,-16],[0,3.3,0],1.3],['front',[0,6,-20],[0,3.3,0],1.3],['side',[-20,3.5,0],[0,3.3,0],1.3],['back',[0,8,20],[0,3.3,0],1.3],['bottom',[0,-20,0],[0,2,0],1.5],['bottom_oblique',[-12,-8,12],[0,2,0],1.5]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});let pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
await call('set_camera_angle',{position:[-13,9,-16],target:[0,3.3,0],projection:'orthographic',zoom:1.3});
