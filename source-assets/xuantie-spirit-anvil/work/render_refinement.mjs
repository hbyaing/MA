import fs from 'node:fs/promises';
import {connect,call,getText} from '../../spirit-eater-beetle/work/mcp.mjs';
const dir='E:/五行仙途/source-assets/xuantie-spirit-anvil';
await connect();
await call('risky_eval',{code:`(()=>{const r=new THREE.WebGLRenderer({alpha:true,antialias:true,preserveDrawingBuffer:true});r.setPixelRatio(1);r.setSize(1400,1050);globalThis.anvilRefinementRenderer=r;return {ready:true};})()`});
try{
for(const [name,pos,span] of [['perspective',[30,28,-36],35],['front',[0,8,-40],29],['right',[40,8,0],24],['top',[0,50,.001],29],['back',[-30,24,36],35]]){
 const result=JSON.parse(getText(await call('risky_eval',{code:`(()=>{
const camera=new THREE.OrthographicCamera(-${span}/2,${span}/2,${span}*.375,-${span}*.375,.01,1000);
camera.position.fromArray(${JSON.stringify(pos)});camera.lookAt(0,8,0);camera.layers.mask=Preview.selected.camera.layers.mask;camera.updateMatrixWorld();
const old=Canvas.scene.background;let data;
try{Canvas.scene.background=new THREE.Color('#202630');Canvas.withoutGizmos(()=>{globalThis.anvilRefinementRenderer.render(Canvas.scene,camera);data=globalThis.anvilRefinementRenderer.domElement.toDataURL('image/png');});}
finally{Canvas.scene.background=old;}
return {data};})()`})));
 await fs.writeFile(dir+'/'+name+'.png',Buffer.from(result.data.split(',')[1],'base64'));
 console.log('Rendered '+name);await new Promise(r=>setTimeout(r,200));
}
}finally{
await call('risky_eval',{code:`(()=>{globalThis.anvilRefinementRenderer.dispose();globalThis.anvilRefinementRenderer.forceContextLoss();delete globalThis.anvilRefinementRenderer;return {disposed:true};})()`});
}
await call('set_camera_angle',{position:[30,28,-36],target:[0,8,0],projection:'orthographic',zoom:.45});
await call('export_model',{codec_id:'project',path:dir+'/玄铁灵砧.bbmodel',overwrite:true,max_content_length:0});
await call('risky_eval',{code:`(()=>{Project.save_path='${dir}/玄铁灵砧.bbmodel';Project.saved=true;return true;})()`});
