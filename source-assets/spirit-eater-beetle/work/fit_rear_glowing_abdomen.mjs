import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
console.log(getText(await call('risky_eval',{code:`(()=>{
const ventral=Mesh.all.filter(m=>/^贴合腹甲_[567]$/.test(m.name)),belly=Mesh.all.find(m=>m.name.includes('腹部_青玉色薄膜')),glow=Texture.all.find(t=>t.name==='背面参考_灵光.png');Undo.initEdit({elements:[...ventral,belly]});
for(const m of ventral){const index=Number(m.name.slice(-1)),ps=Object.values(m.vertices),z0=Math.min(...ps.map(p=>p[2])),z1=Math.max(...ps.map(p=>p[2])),N=ps.length/2;for(let i=0;i<ps.length;i++){const p=ps[i],z=2.0+(index-5)*.38+(p[2]-z0)/(z1-z0)*.43,x=p[0]*.67,y=2.772-2.02*Math.sqrt(Math.max(.001,1-(x/2.02)**2-((z-1.89)/2.36)**2));p[0]=x;p[1]=y-.025+(i>=N?.06:0);p[2]=z;}}
const widths=[[218,0],[300,110],[400,173],[450,211],[500,237],[550,252],[600,259],[650,262],[700,253],[750,234],[800,199],[850,134],[895,9],[902,0]];
function width(v){for(let i=1;i<widths.length;i++)if(v<=widths[i][0]){const [a,b]=[widths[i-1],widths[i]],t=Math.max(0,(v-a[0])/(b[0]-a[0]));return a[1]+(b[1]-a[1])*t;}return 0;}
for(const f of Object.values(belly.faces)){if(f.texture!==glow.uuid)continue;for(const k of f.vertices){const p=belly.vertices[k],v=218+(4.792-p[1])/(4.04/682),r=2.02*Math.sqrt(Math.max(.0001,1-((p[1]-2.772)/2.02)**2));f.uv[k]=[768+p[0]/r*width(v)*.965,v];}}
Canvas.updateAll();Undo.finishEdit('后腹甲收回腹底，露出双叶灵光腹部');return {ventralPlatesFitted:ventral.length};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['rear_reference_straight',[0,2.772,20],[0,2.772,1.89],2.1],['rear_reference_oblique',[-12,7,15],[0,3,2.5],2.1]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});const pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
