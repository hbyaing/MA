import fs from 'node:fs/promises';import {connect,call} from './mcp.mjs';await connect();const d='E:/五行仙途/source-assets/spirit-eater-beetle/work';const prefix=process.argv[2]||'v5';
await call('create_offscreen_view',{id:'beetle_v5',width:960,height:800,copy_view:'active'});
for(const[n,p,t,z]of [['side',[-20,3.5,2],[0,3.7,.4],2.5],['rear',[0,2.772,20],[0,3.7,.7],2.5],['oblique',[-12,7,15],[0,3.7,.4],2.5],['front',[-13,9,-16],[0,3.5,-.4],2.2],['top',[0,23,1],[0,2,0],2.2],['underside',[-10,-12,12],[0,2,0],2.1]]){
 const r=await call('set_camera_angle',{view:'beetle_v5',position:p,target:t,zoom:z,projection:'orthographic'});const pic=r.content.find(c=>c.type==='image');if(!pic)throw Error('No capture '+n);await fs.writeFile(d+'/'+prefix+'_'+n+'.png',Buffer.from(pic.data,'base64'));
}
console.log('Six views captured:',prefix);

await call('delete_offscreen_view',{view:'beetle_v5'});

