import fs from 'node:fs/promises';
import {connect,call,getText} from './mcp.mjs';
await connect();
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
for(const [name,position,target,zoom] of [
 ['audit_full',[-13,9,-16],[0,3.3,0],1.35],
 ['audit_rear',[0,2.772,20],[0,2.772,1.89],1.6],
 ['audit_side',[-20,3.8,2],[0,2.8,0],1.5],
 ['audit_top',[0,23,1],[0,2,0],1.35],
 ['audit_rear_oblique',[-12,7,15],[0,3,1],1.65]
]){
 const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});
 const pic=r.content.find(c=>c.type==='image');
 if(pic)await fs.writeFile(`${dir}/work/${name}.png`,Buffer.from(pic.data,'base64'));
}
const stat=await call('risky_eval',{code:`(()=>({parts:Mesh.all.filter(m=>m.name.startsWith('carapace_')&&m.name.includes('左_')&&!m.name.includes('尾')).map(m=>({name:m.name,vertices:m.vertices})),renderer:{format:Format.id,preview_shading:settings.shading.value}}))()`});
await fs.writeFile(dir+'/work/anatomy_audit.json',getText(stat));
console.log(getText(stat));
