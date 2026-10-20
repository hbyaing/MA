import {connect,call,getText} from '../../spirit-eater-beetle/work/mcp.mjs';
const path='E:/五行仙途/source-assets/xuantie-spirit-anvil/reference-textured/玄铁灵砧_原图贴图.bbmodel';
await connect();
console.log(getText(await call('risky_eval',{code:`(()=>{const p='${path}';const data=JSON.parse(requireNativeModule('fs').readFileSync(p,'utf8'));Codecs.project.load(data,{path:p});return {loaded:Project.name,cubes:Cube.all.length,textures:Texture.all.length};})()`})));
await new Promise(r=>setTimeout(r,400));
await call('set_camera_angle',{position:[30,28,-36],target:[0,8,0],projection:'orthographic',zoom:.43});
