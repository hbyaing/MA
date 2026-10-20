import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
const original=JSON.parse(await fs.readFile(dir+'/before_shell_antenna_revision.bbmodel','utf8'));
const d=original.elements.find(e=>e.name.includes('腹部_青玉色薄膜'));
console.log(getText(await call('risky_eval',{code:`(()=>{
 const d=${JSON.stringify(d)};const m=Mesh.all.find(m=>m.name===d.name);const tx=Texture.all.find(t=>t.name==='噬灵虫_三视图原画纹理.png');const canvas=document.createElement('canvas');canvas.width=tx.width;canvas.height=tx.height;const ctx=canvas.getContext('2d');ctx.drawImage(tx.img,0,0);const pixels=ctx.getImageData(0,0,canvas.width,canvas.height).data;
 Undo.initEdit({elements:[m]});let restored=0,repaired=0;
 for(const [key,f]of Object.entries(d.faces)){
  const cy=f.vertices.reduce((a,k)=>a+d.vertices[k][1]/f.vertices.length,0);if(cy<1.4)continue;
  const uv=f.vertices.map(k=>f.uv[k]);let bad=false;
  for(let j=1;j<uv.length-1;j++){const [a,b,c]=[uv[0],uv[j],uv[j+1]];for(let v=0;v<=10;v++)for(let w=0;w<=10-v;w++){let x=Math.round(a[0]*(1-v/10-w/10)+b[0]*v/10+c[0]*w/10),y=Math.round(a[1]*(1-v/10-w/10)+b[1]*v/10+c[1]*w/10);x=Math.max(0,Math.min(canvas.width-1,x));y=Math.max(0,Math.min(canvas.height-1,y));if(pixels[(y*canvas.width+x)*4+3]<200)bad=true;}}
  if(bad){let rect=cy>3.7?[[771,508],[790,508],[790,527],[771,527]]:[[900,597],[932,597],[932,621],[900,621]];f.vertices.forEach((k,i)=>f.uv[k]=rect[i]);repaired++;}
  if(m.faces[key])m.faces[key].uv=f.uv;restored++;
 }
 Canvas.updateAll();Undo.finishEdit('恢复背部原画局部纹理并消除透明孔洞');return {restored,repaired};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['perspective',[-13,9,-16],[0,3.3,0],1.3],['front',[0,6,-20],[0,3.3,0],1.3],['side',[-20,3.5,0],[0,3.3,0],1.3],['back',[0,8,20],[0,3.3,0],1.3],['detail_carapace',[-12,10,-12],[0,3.5,-.5],2.3],['detail_rear',[0,8,20],[0,3.5,2.5],2.5],['bottom',[0,-20,0],[0,2,0],1.5],['bottom_oblique',[-12,-8,12],[0,2,0],1.5]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});let pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
await call('set_camera_angle',{position:[-13,9,-16],target:[0,3.3,0],projection:'orthographic',zoom:1.3});
