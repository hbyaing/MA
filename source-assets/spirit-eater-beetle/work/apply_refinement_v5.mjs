import fs from 'node:fs/promises';import {connect,call,getText} from './mcp.mjs';await connect();
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
const baseline=JSON.parse(await fs.readFile(dir+'/before_optimization_v5_20261007.bbmodel','utf8'));
const names=baseline.elements.filter(e=>e.name.includes('主鞘甲')).map(e=>({uuid:e.uuid,vertices:e.vertices}));
const state=JSON.parse(getText(await call('risky_eval',{code:`(()=>({name:Project.name,hasTexture:Texture.all.some(t=>t.name==='腹部_连续环绕UV_v5.png')}))()`})));
if(!state.name.includes('噬灵虫'))throw Error('Wrong project');
if(!state.hasTexture)await call('create_texture',{name:'腹部_连续环绕UV_v5.png',data:dir+'/腹部_连续环绕UV_v5.png',uv_width:1536,uv_height:768,render_mode:'emissive',render_sides:'front'});
console.log(getText(await call('risky_eval',{code:`(()=>{
const originals=${JSON.stringify(names)},wings=originals.map(o=>Mesh.all.find(m=>m.uuid===o.uuid));
const belly=Mesh.all.find(m=>m.name.includes('腹部_青玉色薄膜')),tex=Texture.all.find(t=>t.name==='腹部_连续环绕UV_v5.png');
Undo.initEdit({elements:[...wings,belly]});let lifted=0;
for(const m of wings){m.vertices=JSON.parse(JSON.stringify(originals.find(o=>o.uuid===m.uuid).vertices));for(const p of Object.values(m.vertices)){const w=Math.max(0,Math.min(1,(-.05-p[2])/1.0));const lift=.60*w*Math.exp(-.5*p[0]*p[0]);p[1]+=lift;if(lift>.001)lifted++;}}
for(const f of Object.values(belly.faces))f.texture=tex.uuid;
Canvas.updateAll();Undo.finishEdit('v5 校准腹部侧面原画投射并收拢主鞘甲前缘折角');return {lifted,abdomenFaces:Object.keys(belly.faces).length};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/work/v5_stage1.bbmodel',overwrite:true,max_content_length:0});
