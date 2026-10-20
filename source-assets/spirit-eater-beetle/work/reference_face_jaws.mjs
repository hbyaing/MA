import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const dir='E:/五行仙途/source-assets/spirit-eater-beetle';
console.log(getText(await call('risky_eval',{code:`(()=>{
const jaws=Mesh.all.filter(m=>m.name.includes('mandible_弧形内收钳')),head=Mesh.all.find(m=>m.name.includes('头部底壳')),tx=Texture.all.find(t=>t.name==='噬灵虫_三视图原画纹理.png');
if(jaws.length!==2||!head)throw Error('Missing facial meshes');Undo.initEdit({elements:[...jaws,head]});
const scale=.675/39;
function make(m,outline,side,isHead){
 const vertices={},N=outline.length,contour=outline.map(([u,v])=>new THREE.Vector2((u-202)*scale*side,2.305-(v-654)*.014));
 for(let i=0;i<N;i++){const p=contour[i],uv=outline[i],tip=!isHead&&i>=7&&i<=10;
 const z=isHead?-4.02+Math.abs(p.x)*.55:-3.75-Math.max(0,uv[1]-676)*.006;
 vertices['f'+i]=[p.x,p.y,z-(isHead?0:tip?.018:.075)];vertices['b'+i]=[p.x,p.y,isHead?-3.15:z+(tip?.018:.11)];}
 m.vertices=vertices;m.faces={};
 const tris=THREE.ShapeUtils.triangulateShape(contour,[]);
 for(const tri of tris)for(const layer of ['f','b']){
 let ids=tri.map(i=>layer+i);const p=ids.map(k=>new THREE.Vector3(...vertices[k])),nz=p[1].clone().sub(p[0]).cross(p[2].clone().sub(p[0])).z;if((layer==='f'&&nz>0)||(layer==='b'&&nz<0))ids.reverse();
 m.addFaces(new MeshFace(m,{vertices:ids,uv:Object.fromEntries(ids.map(k=>[k,outline[Number(k.slice(1))].slice()])),texture:tx.uuid}));}
 for(let i=0;i<N;i++){const j=(i+1)%N;let ids=['f'+i,'b'+i,'b'+j,'f'+j];const a=contour[i],b=contour[j],cc=contour.reduce((a,p)=>a.add(p.clone()),new THREE.Vector2()).multiplyScalar(1/N),v=ids.map(k=>new THREE.Vector3(...vertices[k])),normal=v[1].clone().sub(v[0]).cross(v[2].clone().sub(v[0]));if(normal.x*((a.x+b.x)/2-cc.x)+normal.y*((a.y+b.y)/2-cc.y)<0)ids.reverse();
 m.addFaces(new MeshFace(m,{vertices:ids,uv:Object.fromEntries(ids.map(k=>[k,outline[Number(k.slice(1))].slice()])),texture:tx.uuid}));}
}
const jaw=[[227,676],[235,679],[248,687],[250,703],[246,718],[237,729],[225,735],[216.5,733],[222,726],[228,718],[228,713],[224,706],[221,703],[224,700],[231,695],[232,692],[224,689],[220,683]];
for(const m of jaws)make(m,jaw,m.name.startsWith('left')?-1:1,false);
make(head,[[176,627],[202,622],[228,627],[234,642],[229,659],[221,673],[212,694],[192,694],[183,673],[175,659],[170,642]],1,true);
Canvas.updateAll();Undo.finishEdit('按三视图重塑收窄面甲与眼下短钳');return {jawRootBelowEyes:true,jawTipGap:2*(216.5-202)*scale,editedMeshes:3};})()`})));
await call('export_model',{codec_id:'project',path:dir+'/噬灵虫.bbmodel',overwrite:true,max_content_length:0});
for(const[name,position,target,zoom]of [['face_reference_front',[0,2.3,-16],[0,2.1,-3.4],5],['face_reference_side',[-12,5,-12],[-.4,2.15,-3.5],5],['perspective',[-13,9,-16],[0,3.3,0],1.3]]){const r=await call('set_camera_angle',{position,target,zoom,projection:'orthographic'});const pic=r.content.find(c=>c.type==='image');if(pic)await fs.writeFile(dir+'/'+name+'.png',Buffer.from(pic.data,'base64'));}
