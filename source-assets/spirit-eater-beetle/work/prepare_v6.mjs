import fs from 'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const d='E:/五行仙途/source-assets/spirit-eater-beetle';
const info=JSON.parse(getText(await call('risky_eval',{code:`(()=>({name:Project.name,uuid:Project.uuid,format:Format.id,groups:Group.all.map(g=>({name:g.name,uuid:g.uuid,origin:g.origin})),views:Preview.all.filter(p=>p.connected).map(p=>({id:p.id}))}))()`})));
if(!info.name.includes('噬灵虫')||info.format!=='free')throw Error('Unexpected project');
await fs.writeFile(d+'/work/v6_initial_state.json',JSON.stringify(info,null,2));
console.log(getText(await call('export_model',{codec_id:'project',path:d+'/before_abdomen_v6_20261007.bbmodel',overwrite:false,max_content_length:0})));
for(const[n,p,t,z]of [['underside',[-10,-12,12],[0,2,1],3.2],['side',[-20,3.4,2],[0,3.3,.6],2.5],['rear',[0,2.772,20],[0,3.7,.7],2.5]]){const r=await call('set_camera_angle',{view:'active',position:p,target:t,zoom:z,projection:'orthographic'});await fs.writeFile(d+'/work/v6_before_'+n+'.png',Buffer.from(r.content.find(c=>c.type==='image').data,'base64'));}
console.log('V6 backup and three diagnostic views saved');
