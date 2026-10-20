import fs from 'node:fs/promises';
import {connect,call,getText} from '../../spirit-eater-beetle/work/mcp.mjs';
const dir='E:/五行仙途/source-assets/xuantie-spirit-anvil';
await connect();
await call('export_model',{codec_id:'project',path:dir+'/玄铁灵砧.bbmodel',overwrite:true,max_content_length:0});
for(const [name,pos,target,zoom] of [
 ['perspective',[30,28,-36],[0,8,0],.36],
 ['front',[0,8,-40],[0,8,0],.40],
 ['right',[40,8,0],[0,8,0],.48],
 ['top',[0,50,.001],[0,8,0],.40]
]){
 const r=await call('set_camera_angle',{position:pos,target,projection:'orthographic',zoom});
 const pic=r.content.find(c=>c.type==='image');await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));
}
await call('set_camera_angle',{position:[30,28,-36],target:[0,8,0],projection:'orthographic',zoom:.36});
console.log('Saved project and four views');
