import fs from 'node:fs/promises';
import {connect,call,getText} from './mcp.mjs';
await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
const current=JSON.parse(getText(await call('risky_eval',{code:`(()=>({name:Project.name,tex:Texture.all.some(t=>t.name==='腹部_连续环绕UV_v3.png')}))()`})));
if(!current.name.includes('噬灵虫'))throw Error('Wrong project');
if(!current.tex)await call('create_texture',{name:'腹部_连续环绕UV_v3.png',data:dir+'/腹部_连续环绕UV_v3.png',uv_width:1024,uv_height:512,render_mode:'emissive',render_sides:'front'});
console.log(getText(await call('risky_eval',{code:`(()=>{
const m=Mesh.all.find(m=>m.name.includes('腹部_青玉色薄膜')),tex=Texture.all.find(t=>t.name==='腹部_连续环绕UV_v3.png');
Undo.initEdit({elements:[m]});
for(const f of Object.values(m.faces)){
 f.texture=tex.uuid;
 const coords=f.vertices.map(k=>{const p=m.vertices[k];return [Math.atan2(p[0]/2.02,(p[2]-1.89)/2.36)/(2*Math.PI)+.5,Math.acos(Math.max(-1,Math.min(1,(p[1]-2.772)/2.02)))/Math.PI]});
 const nonpolar=coords.filter(p=>p[1]>1e-6&&p[1]<1-1e-6);
 if(Math.max(...nonpolar.map(p=>p[0]))-Math.min(...nonpolar.map(p=>p[0]))>.5)for(const p of coords)if(p[0]<.5)p[0]+=1;
 const u=coords.filter(p=>p[1]>1e-6&&p[1]<1-1e-6).reduce((s,p)=>s+p[0],0)/nonpolar.length;
 for(let i=0;i<coords.length;i++){if(coords[i][1]<1e-6||coords[i][1]>1-1e-6)coords[i][0]=u;f.uv[f.vertices[i]]=[Math.max(0,Math.min(1024,coords[i][0]*1024)),coords[i][1]*512];}
}
Canvas.updateAll();Undo.finishEdit('连续环绕腹部UV，剔除背甲与黑带的误投射');return {faces:Object.keys(m.faces).length};})()`})));
for(const [name,position,target,zoom]of [['stage1_side',[-20,3.8,2],[0,2.8,0],1.5],['stage1_back',[0,2.772,20],[0,2.772,1.89],1.6],['stage1_oblique',[-12,7,15],[0,3,1],1.65]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});const pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/work/'+name+'.png',Buffer.from(pic.data,'base64'));}
