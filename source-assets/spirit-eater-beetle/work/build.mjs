import fs from 'node:fs/promises';
import {connect,call,getText} from './mcp.mjs';
const DIR='E:/五行仙途/source-assets/spirit-eater-beetle';
const REF='D:/QQ/存储/Tencent Files/3320018474/nt_qq/nt_data/Pic/2026-10/Ori/f0747f814c5c940ab5ccb3bb85d47f63.png';
await connect();
console.log('Connected');
const schema=await call('get_project_info');
console.log('Project inspected');
await fs.writeFile(DIR+'/work/project-before.json',JSON.stringify(schema,null,2));
await call('risky_eval',{code:"(()=>{if(Mesh.all.length||Group.all.length)throw Error('Build requires an empty project');return true;})()"});
console.log('Empty project confirmed');
const tex=await call('create_texture',{name:'噬灵虫_三视图原画纹理.png',data:REF,uv_width:1448,uv_height:1086,render_sides:'double'});
console.log('Main texture loaded');
await fs.copyFile(REF,DIR+'/abdomen_source.png');
await call('create_texture',{name:'腹部_灵光原画.png',data:DIR+'/abdomen_source.png',uv_width:1448,uv_height:1086,render_sides:'double',render_mode:'emissive'});
await call('create_texture',{name:'腹部_透明表膜.png',width:16,height:16,fill_color:[139,208,186,27],render_sides:'front'});
console.log('Reference texture loaded');
const data=[],groups=[];
const S=.014;
const P=(u,v,x=0)=>[x,(790-v)*S,(u-760)*S];
const add=(a,b)=>a.map((v,i)=>v+b[i]);
const sub=(a,b)=>a.map((v,i)=>v-b[i]);
const mul=(a,s)=>a.map(v=>v*s);
const dot=(a,b)=>a.reduce((s,v,i)=>s+v*b[i],0);
const cross=(a,b)=>[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]];
const norm=a=>mul(a,1/Math.max(1e-10,Math.hypot(...a)));
const mean=vs=>mul(vs.reduce(add,[0,0,0]),1/vs.length);
const sideUV=p=>[760+p[2]/S,790-p[1]/S];
function group(name,parent='root',origin=[0,0,0]){groups.push({name,parent,origin});return name;}
function mesh(name,vertices,faces,g,uvfn=sideUV,patch=null){
 const uvs=faces.map((f,fi)=>f.map(i=>uvfn(vertices[i],fi,i)));
 data.push({name:g+'__'+name,vertices,faces,group:g,uvs,patch});
}
function ellipsoid(name,c,r,g,uvfn=sideUV,nu=16,nv=8){
 const vs=[],faces=[];
 for(let j=0;j<=nv;j++){let ph=Math.PI*j/nv;for(let i=0;i<nu;i++){let t=2*Math.PI*i/nu;vs.push([c[0]+r[0]*Math.sin(ph)*Math.cos(t),c[1]+r[1]*Math.cos(ph),c[2]+r[2]*Math.sin(ph)*Math.sin(t)]);}}
 for(let j=0;j<nv;j++)for(let i=0;i<nu;i++){let a=j*nu+i,b=j*nu+(i+1)%nu,d=(j+1)*nu+i,e=(j+1)*nu+(i+1)%nu;let f=j===0?[a,e,d]:j===nv-1?[a,b,d]:[a,b,e,d];let n=cross(sub(vs[f[1]],vs[f[0]]),sub(vs[f[2]],vs[f[0]]));if(dot(n,sub(mean(f.map(k=>vs[k])),c))<0)f.reverse();faces.push(f);}
 mesh(name,vs,faces,g,uvfn);
}
function tube(name,pts,radii,g,uvfn=sideUV,sides=7){
 const vs=[],faces=[];
 for(let j=0;j<pts.length;j++){const tangent=norm(sub(pts[Math.min(j+1,pts.length-1)],pts[Math.max(j-1,0)]));const a=norm(cross(tangent,Math.abs(tangent[0])<.8?[1,0,0]:[0,1,0]));const b=norm(cross(tangent,a));for(let i=0;i<sides;i++){let t=2*Math.PI*i/sides;let r=Array.isArray(radii[j])?radii[j]:[radii[j],radii[j]];vs.push(add(pts[j],add(mul(a,r[0]*Math.cos(t)),mul(b,r[1]*Math.sin(t)))));}}
 for(let j=0;j<pts.length-1;j++)for(let i=0;i<sides;i++)faces.push([j*sides+i,j*sides+(i+1)%sides,(j+1)*sides+(i+1)%sides,(j+1)*sides+i]);
 for(let i=1;i<sides-1;i++){faces.push([0,i+1,i]);let b=(pts.length-1)*sides;faces.push([b,b+i,b+i+1]);}
 const center=mean(pts);
 for(const f of faces){let n=cross(sub(vs[f[1]],vs[f[0]]),sub(vs[f[2]],vs[f[0]]));if(dot(n,sub(mean(f.map(i=>vs[i])),center))<0)f.reverse();}
 mesh(name,vs,faces,g,uvfn);
}
function plate(name,outline,g,side=1,thick=.08){
 const outer=outline.map(([u,v,x])=>P(u,v,x*side));
 const center=mean(outer);center[1]+=.12;center[0]+=.02*side;
 const inner=outer.map(p=>add(center,mul(sub(p,center),.92)));
 const vs=[...outer,...inner,center,...outer.map(p=>add(p,[-.035*side,-thick,0]))];
 const N=outer.length,F=[];
 for(let i=0;i<N;i++){let j=(i+1)%N;F.push([i,j,N+j,N+i]);F.push([N+i,N+j,2*N]);F.push([i,2*N+1+i,2*N+1+j,j]);}
 for(let i=1;i<N-1;i++)F.push([2*N+1,2*N+1+i+1,2*N+1+i]);
 for(const f of F){const n=cross(sub(vs[f[1]],vs[f[0]]),sub(vs[f[2]],vs[f[0]]));const c=mean(f.map(i=>vs[i]));if(dot(n,sub(c,add(center,[0,-thick/2,0])))<0)f.reverse();}
 mesh(name,vs,F,g,sideUV);
}
const root=group('root_噬灵虫');
const body=group('body_躯体',root,P(750,615));
const belly=group('abdomen_半透明双叶腹部',body,P(896,596));
const bellyCenter=P(895,592),bellyRadius=[2.02,2.02,2.36];
function abdomenUV(p){let z=(p[2]-bellyCenter[2])/bellyRadius[2],x=p[0]/bellyRadius[0];if(z>.58)return [1257+x*121,602-(p[1]-bellyCenter[1])*73];let uv=sideUV(p);uv[0]=Math.min(1040,Math.max(773,uv[0]));uv[1]=Math.min(689,Math.max(479,uv[1]));return uv;}
ellipsoid('腹部_青玉色薄膜',bellyCenter,bellyRadius,belly,abdomenUV,24,14);
const bellyData=data.at(-1);
bellyData.texture='腹部_灵光原画.png';
bellyData.uvs=bellyData.faces.map(f=>{const c=mean(f.map(i=>bellyData.vertices[i]));let rear=(c[2]-bellyCenter[2])/bellyRadius[2]>.55;return f.map(i=>{let p=bellyData.vertices[i];return rear?[1257+p[0]/bellyRadius[0]*121,602-(p[1]-bellyCenter[1])*73]:[Math.min(1046,Math.max(777,760+p[2]/S)),Math.min(695,Math.max(478,790-p[1]/S))];});});
ellipsoid('腹部_透明保护膜',bellyCenter,bellyRadius.map(v=>v+.013),belly,p=>[8,8],24,14);
data.at(-1).texture='腹部_透明表膜.png';
ellipsoid('胸部连续底甲',P(664,601),[1.36,.74,1.72],body,p=>[769+p[0]*7,526+p[1]*3],16,8);
const under=group('ventral_腹甲',body);
for(let i=0;i<6;i++){let z=-.5+i*.64;ellipsoid('腹面节甲_'+(i+1),[0,.98+(i<2?.06:0),z],[1.27-i*.07,.24,.37],under,p=>[676+p[0]*15,638+p[2]*6],12,4);}
const armor=group('carapace_层叠背甲',body);
const outlines=[
 ['前胸盾片',[[501,597,0],[545,552,0],[604,507,0],[645,529,.35],[633,570,1.16],[590,614,1.39],[535,635,.97]]],
 ['中胸叠甲',[[613,507,0],[642,484,0],[684,471,0],[680,516,.67],[732,567,1.40],[694,608,1.82],[638,609,1.51],[654,564,1.0]]],
 ['主鞘甲',[[685,518,0],[742,468,0],[819,434,0],[878,435,0],[869,501,1.16],[845,558,1.85],[786,622,2.08],[703,596,1.89]]],
 ['尾背甲第一片',[[881,440,.04],[912,444,.04],[942,458,.5],[934,479,1.2],[890,482,1.05],[872,470,.58]]],
 ['尾背甲第二片',[[933,461,.20],[964,474,.30],[977,491,.95],[960,508,1.52],[929,490,1.34]]],
 ['尾背甲第三片',[[969,485,.52],[992,497,.56],[1009,518,1.37],[981,516,1.56],[959,505,1.28]]],
 ['尾侧甲第四片',[[1004,520,1.29],[1025,545,1.39],[1036,573,1.66],[1013,562,1.90],[990,533,1.67]]],
 ['尾端护片',[[1037,578,1.56],[1058,607,1.33],[1060,657,.87],[1040,687,.85],[1038,655,1.32],[1025,644,1.63]]]
];
for(const s of [-1,1])for(const [n,o]of outlines)plate((s<0?'左_':'右_')+n,o,armor,s);
for(const s of [-1,1]){
 tube((s<0?'左':'右')+'侧腹护缘',[P(716,628,s*1.7),P(771,664,s*1.85),P(841,697,s*1.83),P(934,713,s*1.36),P(1003,686,s*.8)],[.10,.12,.115,.08,.025],armor,p=>[725+p[2]*8,616+p[1]*4]);
}
const head=group('head_头部',body,P(526,632));
ellipsoid('头部底壳',P(516,648),[.94,.60,.59],head,p=>[199+p[0]*28,653-(p[1]-2)*23],12,6);
for(const s of [-1,1])plate((s<0?'左':'右')+'额甲',[[490,627,.15],[501,601,.25],[526,606,.66],[548,630,.94],[538,650,.86],[514,666,.60],[490,662,.18]],head,s,.06);
for(const s of [-1,1]){
 const eye=group((s<0?'left':'right')+'_eye_复眼',head,P(524,630,s*.85));
 ellipsoid('眼眶',P(524,630,s*.88),[.145,.158,.155],eye,p=>[516+(p[0]-s*.88)*12,635+(p[1]-2.24)*12],10,6);
 ellipsoid('微光复眼',P(522,630,s*1.005),[.065,.094,.1],eye,p=>[523+(p[2]-P(522,630)[2])*45,629-(p[1]-P(522,630)[1])*48],10,6);
 const jaw=group((s<0?'left':'right')+'_mandible_短弯口器',head,P(498,680,s*.43));
 tube('钳根',[P(503,675,s*.43),P(491,689,s*.57),P(482,708,s*.63)],[.18,.21,.17],jaw,p=>[178+Math.abs(p[0]) *22,690+(2-p[1])*23]);
 tube('内弯钳尖',[P(482,708,s*.63),P(484,725,s*.48),P(498,731,s*.26),P(504,719,s*.18)],[.17,.12,.075,.008],jaw,p=>[177+Math.abs(p[0])*23,692+(2-p[1])*24]);
 for(let i=0;i<2;i++)tube('口器内齿_'+i,[P(489+i*7,706-i*7,s*.39),P(494+i*7,708-i*7,s*.22)],[.061,.002],jaw,p=>[182,718],5);
 const ant=group((s<0?'left':'right')+'_antenna_弯曲触角',head,P(505,606,s*.56));
 const pts=[[505,606,.56],[487,577,.68],[473,544,.78],[467,512,.89],[469,481,1.04],[481,452,1.28],[501,428,1.56],[531,407,1.91]].map(([u,v,x])=>P(u,v,s*x));
 tube('触角主干',pts,[.071,.066,.060,.055,.051,.047,.043,.031],ant,p=>[148+(p[1]-2)*2,575-(p[1]-2)*55],7);
 for(let i=1;i<pts.length-1;i++){let p=pts[i],d=norm(sub(pts[i+1],pts[i-1]));tube('触角节环_'+i,[add(p,mul(d,-.023)),add(p,mul(d,.023))],[.077-i*.004,.077-i*.004],ant,p=>[140,454+i*16],7);}
 ellipsoid('触角末端青光',pts.at(-1),[.097,.112,.157],ant,p=>[530+(p[2]-pts.at(-1)[2])*37,405-(p[1]-pts.at(-1)[1])*40],12,8);
}
const legs=[
 {id:'front',cn:'前足',path:[[589,646,1.08],[617,671,1.87],[579,691,2.67],[554,735,2.98],[502,788,3.14]],r:[.18,.23,.28,.15,.008]},
 {id:'middle',cn:'中足',path:[[702,642,1.64],[714,676,2.43],[731,728,2.88],[765,790,3.35]],r:[.17,.23,.15,.009]},
 {id:'hind',cn:'后足',path:[[781,650,1.72],[820,673,2.4],[844,649,2.88],[868,716,3.04],[940,777,3.47]],r:[.17,.18,.20,.145,.008]}
];
for(const s of [-1,1])for(const l of legs){
 let side=s<0?'left':'right';let pts=l.path.map(([u,v,x])=>P(u,v,s*x));
 const limb=group(side+'_'+l.id+'_'+l.cn,body,pts[0]);
 ellipsoid('髋关节',pts[0],[.23,.22,.22],limb,p=>[693+(p[0]-pts[0][0])*18,640+(p[1]-pts[0][1])*18],10,6);
 for(let j=0;j<pts.length-1;j++){
 const bone=group(side+'_'+l.id+'_segment_'+j, j===0?limb:side+'_'+l.id+'_segment_'+(j-1),pts[j]);
 let a=pts[j],b=pts[j+1],mid=add(a,mul(sub(b,a),.32));let end=add(a,mul(sub(b,a),.91));
 tube(l.cn+'_肢节_'+j,[a,mid,end,b],[l.r[j]*.72,l.r[j],l.r[j+1]*.94,l.r[j+1]*.64],bone,sideUV,7);
 if(j<pts.length-2)ellipsoid(l.cn+'_关节_'+j,b,[l.r[j+1]*.8,l.r[j+1]*.78,l.r[j+1]*.8],bone,p=>[685+(p[0]-b[0])*12,651+(p[1]-b[1])*12],8,4);
 }
}
const seam=group('rear_suture_尾部中央接缝',belly);
tube('尾腹中央细脊',[P(950,466,0),P(1016,496,0),P(1054,552,0),P(1061,610,0),P(1040,664,0),P(992,704,0)],[.07,.065,.055,.047,.045,.06],seam,p=>[1257,Math.min(764,480+(p[2]-2)*95)],6);
for(const d of data){
 if(/触角主干|触角节环|侧腹护缘|髋关节|_关节_|腹面节甲|肢节|钳根|内弯钳尖|口器内齿|头部底壳|眼眶|尾腹中央/.test(d.name)){
   const warm=/钳|内齿/.test(d.name),ant=/触角/.test(d.name);
   const rect=warm?[515,665,534,686]:ant?[768,509,785,526]:[758,512,794,539];
   const mn=[0,1,2].map(a=>Math.min(...d.vertices.map(v=>v[a]))),mx=[0,1,2].map(a=>Math.max(...d.vertices.map(v=>v[a])));
   d.uvs=d.faces.map((f,fi)=>f.map(i=>{const p=d.vertices[i];return [rect[0]+(p[0]-mn[0])/Math.max(.001,mx[0]-mn[0])*(rect[2]-rect[0]),rect[1]+(p[1]-mn[1])/Math.max(.001,mx[1]-mn[1])*(rect[3]-rect[1])];}));
 }
 if(/微光复眼|触角末端青光/.test(d.name)){
   d.texture='腹部_灵光原画.png';
   const c=mean(d.vertices);
   d.uvs=d.faces.map(f=>f.map(i=>[530+(d.vertices[i][2]-c[2])*24,405-(d.vertices[i][1]-c[1])*27]));
 }
}
for(const g of groups){await call('add_group',g);}
for(const g of groups){for(const texture of ['噬灵虫_三视图原画纹理.png','腹部_灵光原画.png','腹部_透明表膜.png']){const items=data.filter(d=>d.group===g.name&&(d.texture||'噬灵虫_三视图原画纹理.png')===texture);if(items.length)await call('place_mesh',{group:g.name,texture,elements:items.map(({name,vertices,faces})=>({name,vertices,faces}))});}}
await fs.writeFile(DIR+'/work/geometry.json',JSON.stringify(data));
const uvPayload=data.map(({name,uvs})=>({name,uvs}));
const code=`(()=>{const payload=${JSON.stringify(uvPayload)};Undo.initEdit({elements:Mesh.all});for(const d of payload){const m=Mesh.all.find(m=>m.name===d.name);if(!m)throw Error(d.name);Object.values(m.faces).forEach((f,i)=>{f.vertices.forEach((v,j)=>f.uv[v]=d.uvs[i][j]);});}Canvas.updateAll();Undo.finishEdit('三视图原画 UV 投射');Project.name='噬灵虫 · 三视图精细模型';Project.credit='Model based on user supplied front / side / rear reference';return {meshes:Mesh.all.length,groups:Group.all.length,faces:Mesh.all.reduce((n,m)=>n+Object.keys(m.faces).length,0)};})()`;
console.log(getText(await call('risky_eval',{code})));
await fs.copyFile(REF,DIR+'/reference.png');
await fs.copyFile('C:/Users/Nostzephert/Desktop/噬灵虫.txt',DIR+'/reference-prompt.txt');
console.log('Model built');
