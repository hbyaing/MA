import fs from 'node:fs/promises';
import {connect,call,rpc,getText} from './mcp.mjs';
await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
await fs.writeFile(dir+'/work/mcp-tools-v5.json',JSON.stringify(await rpc('tools/list'),null,2));
const state=JSON.parse(getText(await call('risky_eval',{code:`(()=>({name:Project.name,format:Format.id,elements:Mesh.all.map(m=>({name:m.name,uuid:m.uuid,origin:m.origin,rotation:m.rotation,vertices:Object.keys(m.vertices).length,faces:Object.keys(m.faces).length,visible:m.visibility,render_order:m.render_order})),groups:Group.all.map(g=>({name:g.name,uuid:g.uuid,origin:g.origin})),textures:Texture.all.map(t=>({name:t.name,uuid:t.uuid,faces:Mesh.all.reduce((n,m)=>n+Object.values(m.faces).filter(f=>f.texture===t.uuid).length,0)}))}))()`})));
if(!state.name.includes('噬灵虫'))throw Error('Wrong project');
await fs.writeFile(dir+'/work/v5_initial_state.json',JSON.stringify(state,null,2));
await call('export_model',{codec_id:'project',path:dir+'/before_optimization_v5_20261007.bbmodel',overwrite:false,max_content_length:0});
console.log(JSON.stringify(state,null,2));
