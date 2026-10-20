import fs from 'node:fs/promises';
import {connect,call,getText} from './mcp.mjs';
await connect();
const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
await call('export_model',{codec_id:'project',path:dir+'/before_shell_antenna_revision.bbmodel',overwrite:true,max_content_length:0});
const add=(a,b)=>a.map((v,i)=>v+b[i]),sub=(a,b)=>a.map((v,i)=>v-b[i]),mul=(a,s)=>a.map(v=>v*s),cross=(a,b)=>[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]],norm=a=>mul(a,1/Math.hypot(...a));
const pieces=[];
function shell(name,rings){
 const vs=[],uv=[],faces=[],angles=[-78,-60,-40,-20,0,20,40,60,78],n=angles.length;
 for(let j=0;j<rings.length;j++){let [z,y,w,d]=rings[j];for(let i=0;i<n;i++){let a=angles[i]*Math.PI/180,u=i/(n-1),v=j/(rings.length-1),edge=Math.abs(Math.sin(a));vs.push([w*Math.sin(a),y-d*(1-Math.cos(a)),z+(j===rings.length-1?.19*(1-edge):0)]);let left=[744+v*20,500+v*56],right=[829+v*8,470+v*59];uv.push([left[0]*(1-u)+right[0]*u,left[1]*(1-u)+right[1]*u]);}}
 const outer=vs.length;
 for(let i=0;i<outer;i++){vs.push(add(vs[i],[0,-.07,0]));uv.push(uv[i]);}
 for(let j=0;j<rings.length-1;j++)for(let i=0;i<n-1;i++){let a=j*n+i,b=a+1,c=a+n+1,d=a+n;faces.push([a,d,c,b]);faces.push([a+outer,b+outer,c+outer,d+outer]);}
 const boundary=[];for(let i=0;i<n;i++)boundary.push(i);for(let j=1;j<rings.length;j++)boundary.push(j*n+n-1);for(let i=n-2;i>=0;i--)boundary.push((rings.length-1)*n+i);for(let j=rings.length-2;j>0;j--)boundary.push(j*n);
 for(let j=0;j<boundary.length;j++){let a=boundary[j],b=boundary[(j+1)%boundary.length];faces.push([a,b,b+outer,a+outer]);}
 pieces.push({name,group:'carapace_层叠背甲',vertices:vs,faces,uvs:faces.map(f=>f.map(i=>uv[i])),texture:'噬灵虫_三视图原画纹理.png'});
}
shell('前胸_连续弧面盾甲',[[-3.50,2.81,.55,.53],[-3.03,3.40,.97,.90],[-2.36,3.99,1.37,1.22],[-1.96,3.87,1.43,1.48]]);
shell('中胸_覆叠肩甲',[[-2.10,3.96,1.34,1.44],[-1.62,4.43,1.45,1.40],[-1.13,4.53,1.55,1.68],[-.62,4.17,1.82,1.97]]);
shell('主鞘_隆起背甲',[[-1.22,4.42,1.48,1.59],[-.52,4.83,1.71,1.87],[.54,5.03,1.87,2.11],[1.62,4.99,1.95,2.28],[1.84,4.82,1.96,2.45]]);
shell('尾背_第一覆甲',[[1.63,4.93,1.66,1.64],[2.05,4.91,1.64,1.57],[2.41,4.66,1.63,1.67]]);
shell('尾背_第二覆甲',[[2.18,4.77,1.50,1.4],[2.56,4.62,1.50,1.49],[2.92,4.38,1.42,1.60]]);
shell('尾背_第三覆甲',[[2.69,4.48,1.26,1.20],[3.05,4.25,1.24,1.21],[3.34,4.02,1.13,1.31]]);
shell('尾背_闭合中央护片',[[3.13,4.15,1.0,1.0],[3.43,3.86,.91,.97],[3.62,3.61,.67,.8],[3.81,3.15,.20,.34]]);
function tube(name,group,pts,rs,texture='噬灵虫_三视图原画纹理.png',glow=false){
 const vs=[],uv=[],f=[],N=8;
 for(let j=0;j<pts.length;j++){let t=norm(sub(pts[Math.min(j+1,pts.length-1)],pts[Math.max(j-1,0)])),a=norm(cross(t,[0,0,1])),b=norm(cross(t,a));for(let i=0;i<N;i++){let angle=i/N*Math.PI*2;vs.push(add(pts[j],add(mul(a,rs[j]*Math.cos(angle)),mul(b,rs[j]*Math.sin(angle)))));uv.push(glow?[525+i/N*8,401+j/(pts.length-1)*8]:[778+i/N*9,518+j/(pts.length-1)*10]);}}
 for(let j=0;j<pts.length-1;j++)for(let i=0;i<N;i++)f.push([j*N+i,j*N+(i+1)%N,(j+1)*N+(i+1)%N,(j+1)*N+i]);
 for(let i=1;i<N-1;i++){f.push([0,i+1,i]);let k=(pts.length-1)*N;f.push([k,k+i,k+i+1]);}
 pieces.push({name,group,vertices:vs,faces:f,uvs:f.map(f=>f.map(i=>uv[i])),texture});
}
for(const s of [-1,1]){
 const side=s<0?'left':'right',group=side+'_antenna_弯曲触角';
 const pts=[[.55,2.52,-3.58],[.66,3.05,-3.68],[.73,3.61,-3.75],[.81,4.18,-3.78],[.94,4.72,-3.78],[1.10,5.20,-3.76],[1.34,5.65,-3.7],[1.64,6.01,-3.6],[1.99,6.26,-3.48]].map(([x,y,z])=>[x*s,y,z]);
 tube(side+'_触须_弯曲细长主干',group,pts,[.071,.065,.059,.054,.049,.044,.039,.034,.025]);
 for(let i=1;i<pts.length-1;i++){const t=norm(sub(pts[i+1],pts[i-1]));tube(side+'_触须_窄节环_'+i,group,[add(pts[i],mul(t,-.025)),add(pts[i],mul(t,.025))],[.077-i*.005,.074-i*.005]);}
 const end=pts.at(-1),t=norm(sub(end,pts.at(-2)));
 tube(side+'_触须_青光水滴末梢',group,[add(end,mul(t,-.08)),add(end,mul(t,.045)),add(end,mul(t,.18)),add(end,mul(t,.28))],[.028,.11,.085,.006],'腹部_灵光原画.png',true);
}
console.log(getText(await call('risky_eval',{code:`(()=>{const ms=Mesh.all.filter(m=>m.parent?.name==='carapace_层叠背甲'||m.parent?.name.includes('_antenna_'));Undo.initEdit({elements:ms,outliner:true});for(const m of ms)m.remove();Canvas.updateAll();Undo.finishEdit('替换错误背甲投射和触须造型');return {removed:ms.length};})()`})));
for(const m of pieces)await call('place_mesh',{group:m.group,texture:m.texture,elements:[{name:m.name,vertices:m.vertices,faces:m.faces}]});
console.log(getText(await call('risky_eval',{code:`(()=>{const ds=${JSON.stringify(pieces.map(({name,uvs})=>({name,uvs})))};const ms=ds.map(d=>Mesh.all.find(m=>m.name===d.name));Undo.initEdit({elements:ms});ds.forEach((d,i)=>Object.values(ms[i].faces).forEach((f,j)=>f.vertices.forEach((k,v)=>f.uv[k]=d.uvs[j][v])));Canvas.updateAll();Undo.finishEdit('干净甲壳区域连续 UV');return {meshes:Mesh.all.length,eyes:Mesh.all.filter(m=>m.name.includes('微光复眼')).length};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const [name,position,target,zoom]of [['perspective',[-13,9,-16],[0,3.3,0],1.3],['front',[0,6,-20],[0,3.3,0],1.3],['side',[-20,3.5,0],[0,3.3,0],1.3],['back',[0,8,20],[0,3.3,0],1.3],['detail_rear',[0,8,20],[0,3.5,2.5],2.5],['detail_mouth',[0,3.9,-16],[0,1.95,-3.6],4.2]]){let r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});let pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
await call('set_camera_angle',{position:[-13,9,-16],target:[0,3.3,0],projection:'orthographic',zoom:1.3});
