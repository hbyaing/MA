import fs from 'node:fs/promises';
import {connect,call,getText} from './mcp.mjs';
await connect();
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
await call('risky_eval',{code:"(()=>{unselectAll();if(typeof Dialog!=='undefined'&&Dialog.open)Dialog.open.hide();Canvas.updateAll();return {textures:Texture.all.map(t=>({name:t.name,width:t.width,height:t.height,uv_width:t.uv_width,uv_height:t.uv_height})),bounds:Mesh.all.length};})()"}).then(r=>console.log(getText(r)));
for(const [name,pos,target] of [
 ['perspective',[13,10,-16],[0,2.6,0]],
 ['side',[-20,3.5,0],[0,2.8,0]],
 ['front',[0,12,-20],[0,2.8,0]],
 ['back',[0,10,20],[0,2.8,0]]
]){
 const r=await call('set_camera_angle',{position:pos,target,projection:'orthographic',zoom:1.3,max_size:1600});
 const pic=r.content.find(c=>c.type==='image');
 if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));
 console.log(name,getText(r).slice(0,300));
}


