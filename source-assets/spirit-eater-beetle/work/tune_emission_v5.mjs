import fs from 'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const d='E:/五行仙途/source-assets/spirit-eater-beetle';const data='data:image/png;base64,'+(await fs.readFile(d+'/腹部_连续环绕UV_v5.png')).toString('base64');
console.log(getText(await call('risky_eval',{code:`(()=>{const t=Texture.all.find(t=>t.name==='腹部_连续环绕UV_v5.png');if(!t)throw Error('Missing v5');Undo.initEdit({textures:[t],bitmap:true});t.updateSource(${JSON.stringify(data)});t.saved=false;Canvas.updateAll();Undo.finishEdit('v5 腹部原图光点分级微发光（逆Alpha发光遮罩）');return {name:t.name,mode:t.render_mode};})()`})));
await call('create_offscreen_view',{id:'diagnose_v5',width:850,height:800});
const membrane=JSON.parse(getText(await call('risky_eval',{code:`(()=>{const m=Mesh.all.find(m=>m.name.includes('腹部_透明保护膜'));return {uuid:m.uuid,visibility:m.visibility}})()`})));
try{
 for(const visible of [true,false]){
  await call('risky_eval',{code:`(()=>{const m=Mesh.all.find(m=>m.uuid==='${membrane.uuid}');m.visibility=${visible};Canvas.updateAll();return {visibility:m.visibility};})()`});
  const r=await call('set_camera_angle',{view:'diagnose_v5',position:[0,2.772,20],target:[0,3.7,.7],zoom:2.5,projection:'orthographic'});await fs.writeFile(d+'/work/v5_membrane_'+visible+'.png',Buffer.from(r.content.find(c=>c.type==='image').data,'base64'));
 }
}finally{await call('risky_eval',{code:`(()=>{const m=Mesh.all.find(m=>m.uuid==='${membrane.uuid}');m.visibility=${membrane.visibility};Canvas.updateAll();return true;})()`});await call('delete_offscreen_view',{view:'diagnose_v5'}).catch(e=>console.warn('Offscreen dispose plugin error:',e.message.slice(0,150)));}
