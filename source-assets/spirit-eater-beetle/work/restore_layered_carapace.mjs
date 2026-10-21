import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
await call('export_model',{codec_id:'project',path:dir+'/before_restore_layered_carapace.bbmodel',overwrite:true,max_content_length:0});
const original=JSON.parse(await fs.readFile(dir+'/before_shell_antenna_revision.bbmodel','utf8'));
const plates=original.elements.filter(e=>e.name.startsWith('carapace_层叠背甲__')&&!e.name.includes('护缘'));
const changes=[];
for(const e of plates)for(const [key,f]of Object.entries(e.faces)){
 if(Object.values(f.uv).some(uv=>uv[0]<400)){
  for(const k of f.vertices){const p=e.vertices[k];f.uv[k]=[760+p[2]/.014,790-p[1]/.014];}
  changes.push({mesh:e.name,face:key});
 }
}
console.log(getText(await call('risky_eval',{code:`(()=>{
 const ds=${JSON.stringify(plates)};const group=Group.all.find(g=>g.name==='carapace_层叠背甲');if(!group)throw Error('Missing carapace group');
 const old=Mesh.all.filter(m=>m.parent===group);const elements=old.slice();Undo.initEdit({elements,outliner:true});
 for(const m of old)m.remove();const tx=Texture.all.find(t=>t.name==='噬灵虫_三视图原画纹理.png');
 for(const d of ds){for(const f of Object.values(d.faces))f.texture=tx.uuid;const m=new Mesh(d).addTo(group).init();elements.push(m);}
 Canvas.updateAll();Undo.finishEdit('恢复原叠层甲片，仅修正误含触须的 UV');return {restoredPlates:ds.length,meshCount:Mesh.all.length};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
const before=JSON.parse(await fs.readFile(dir+'/before_restore_layered_carapace.bbmodel','utf8'));
const after=JSON.parse(await fs.readFile(dir+'/噬灵虫.bbmodel','utf8'));
const result={restored_plates:plates.length,repaired_uv_faces:changes.length,original_geometry_matches:plates.every(e=>{let a=after.elements.find(x=>x.uuid===e.uuid);return a&&JSON.stringify(a.vertices)===JSON.stringify(e.vertices)&&JSON.stringify(a.origin)===JSON.stringify(e.origin)&&JSON.stringify(a.rotation)===JSON.stringify(e.rotation);}),unrelated_elements_unchanged:before.elements.filter(e=>!['前胸_连续弧面盾甲','中胸_覆叠肩甲','主鞘_隆起背甲','尾背_第一覆甲','尾背_第二覆甲','尾背_第三覆甲','尾背_闭合中央护片'].includes(e.name)).every(e=>JSON.stringify(e)===JSON.stringify(after.elements.find(a=>a.uuid===e.uuid))),uv_changes:changes};
await fs.writeFile(dir+'/carapace_restore_validation.json',JSON.stringify(result,null,2));
console.log(JSON.stringify({...result,uv_changes:undefined}));
for(const[name,position,target,zoom]of [['perspective',[-13,9,-16],[0,3.3,0],1.3],['front',[0,6,-20],[0,3.3,0],1.3],['side',[-20,3.5,0],[0,3.3,0],1.3],['back',[0,8,20],[0,3.3,0],1.3],['detail_carapace',[-12,10,-12],[0,3.5,-.5],2.3],['detail_rear',[0,8,20],[0,3.5,2.5],2.5]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});let pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
await call('set_camera_angle',{position:[-13,9,-16],target:[0,3.3,0],projection:'orthographic',zoom:1.3});
