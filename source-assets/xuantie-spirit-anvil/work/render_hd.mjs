import fs from 'node:fs/promises';
import {connect,call,getText} from '../../spirit-eater-beetle/work/mcp.mjs';
const dir='E:/五行仙途/source-assets/xuantie-spirit-anvil';
await connect();
for(const [name,pos,span] of [
 ['perspective',[30,28,-36],33],
 ['front',[0,8,-40],29],
 ['right',[40,8,0],24],
 ['top',[0,50,.001],29],
 ['back',[-30,24,36],33]
]){
const result=JSON.parse(getText(await call('risky_eval',{code:`(()=>{
const renderer=new THREE.WebGLRenderer({alpha:true,antialias:true,preserveDrawingBuffer:true});
renderer.setPixelRatio(1);renderer.setSize(1600,1200);
const camera=new THREE.OrthographicCamera(-${span}/2,${span}/2,${span}*.375,-${span}*.375,.01,1000);
camera.position.fromArray(${JSON.stringify(pos)});camera.lookAt(0,8,0);camera.layers.mask=Preview.selected.camera.layers.mask;camera.updateMatrixWorld();
let data;const old=Canvas.scene.background;
try{Canvas.scene.background=new THREE.Color('#202630');Canvas.withoutGizmos(()=>{renderer.render(Canvas.scene,camera);data=renderer.domElement.toDataURL('image/png')});}
finally{Canvas.scene.background=old;renderer.dispose();renderer.forceContextLoss();}
return {data};})()`})));
await fs.writeFile(dir+'/'+name+'.png',Buffer.from(result.data.split(',')[1],'base64'));
console.log('Rendered '+name);
}
await call('set_camera_angle',{position:[30,28,-36],target:[0,8,0],projection:'orthographic',zoom:.47});
await call('export_model',{codec_id:'project',path:dir+'/玄铁灵砧.bbmodel',overwrite:true,max_content_length:0});
console.log(getText(await call('get_project_info')));
