import fs from 'node:fs/promises';
import {connect,call,getText} from '../../spirit-eater-beetle/work/mcp.mjs';
const dir='E:/五行仙途/source-assets/xuantie-spirit-anvil/reference-textured';
await connect();
await call('risky_eval',{code:`(()=>{if(Project.name!=='玄铁灵砧_原图贴图')throw Error('Wrong project');const r=new THREE.WebGLRenderer({alpha:true,antialias:true,preserveDrawingBuffer:true});r.setPixelRatio(1);r.setSize(1400,1050);globalThis.anvilReferenceRenderer=r;return true;})()`});
try{
for(const [name,pos,span] of [['perspective',[30,28,-36],35],['front',[0,8,-40],29],['right',[40,8,0],24],['top',[0,50,.001],29]]){
const result=JSON.parse(getText(await call('risky_eval',{code:`(()=>{
const c=new THREE.OrthographicCamera(-${span}/2,${span}/2,${span}*.375,-${span}*.375,.01,1000);c.position.fromArray(${JSON.stringify(pos)});c.lookAt(0,8,0);c.layers.mask=Preview.selected.camera.layers.mask;c.updateMatrixWorld();const old=Canvas.scene.background;let data;try{Canvas.scene.background=new THREE.Color('#202630');Canvas.withoutGizmos(()=>{globalThis.anvilReferenceRenderer.render(Canvas.scene,c);data=globalThis.anvilReferenceRenderer.domElement.toDataURL('image/png');});}finally{Canvas.scene.background=old;}return {data};})()`})));
await fs.writeFile(dir+'/'+name+'.png',Buffer.from(result.data.split(',')[1],'base64'));console.log('Rendered '+name);await new Promise(r=>setTimeout(r,180));
}
}finally{await call('risky_eval',{code:`(()=>{globalThis.anvilReferenceRenderer.dispose();globalThis.anvilReferenceRenderer.forceContextLoss();delete globalThis.anvilReferenceRenderer;return true;})()`});}
await call('export_model',{codec_id:'project',path:dir+'/玄铁灵砧_原图贴图.bbmodel',overwrite:true,max_content_length:0});
await call('risky_eval',{code:`(()=>{Project.save_path='${dir}/玄铁灵砧_原图贴图.bbmodel';Project.saved=true;return {project:Project.name,cubes:Cube.all.length};})()`});
