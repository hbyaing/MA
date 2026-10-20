import fs from'node:fs/promises';import{connect,call,getText}from'./mcp.mjs';await connect();const d='E:/五行仙途/source-assets/spirit-eater-beetle';const base=JSON.parse(await fs.readFile(d+'/before_thorax_junction_v6_20261007.bbmodel','utf8'));const old=base.elements.find(m=>m.name==='贴合腹甲_1');
const report=JSON.parse(getText(await call('risky_eval',{code:`(()=>{
const m=Mesh.all.find(m=>m.name==='贴合腹甲_1'),chest=Mesh.all.find(m=>m.name.includes('胸部连续底甲')),sources=Mesh.all.filter(m=>/腹部_青玉色薄膜|腹部_透明保护膜/.test(m.name)),old=${JSON.stringify(old)},tex=Texture.all.find(t=>t.name==='腹甲_弧缘分节UV_v6.png');
function tris(ms){const ts=[];for(const s of ms)for(const f of Object.values(s.faces)){const k=f.getSortedVertices();for(let i=1;i<k.length-1;i++)ts.push([k[0],k[i],k[i+1]].map(q=>new THREE.Vector3(...s.vertices[q])));}return ts;}
const ct=tris([chest]),at=tris(sources);
function hit(ts,x,z){let y=Infinity;const ray=new THREE.Ray(new THREE.Vector3(x,-10,z),new THREE.Vector3(0,1,0));for(const t of ts){const p=ray.intersectTriangle(...t,false,new THREE.Vector3());if(p)y=Math.min(y,p.y);}return y;}
const vs=JSON.parse(JSON.stringify(old.vertices)),fs={},report={changedMeshCount:1,frontExtension:.18,rootEmbeddedVertices:0};
for(let j=0;j<6;j++)for(let i=0;i<17;i++){
 const r=j/5,t=-1+2*i/16,k='v'+j+'_'+i,p=vs[k],fade=(1-r)**3;
 const x=p[0]+.025*t*fade,z=p[2]-.18*(1-.7*Math.abs(t)**1.4)*fade,cy=hit(ct,x,z),ay=hit(at,x,z);
 let sy=Math.min(cy,ay);if(!Number.isFinite(sy))throw Error('Missing attachment surface');
 let y=sy-.048-.058*r*r;
 if(j===0&&Number.isFinite(cy)){y=Math.min(cy+.018,ay-.025);if(y>cy)report.rootEmbeddedVertices++;}
 if(j>=3)y=p[1];
 const thickness=old.vertices[k+'b'][1]-old.vertices[k][1];vs[k]=[x,y,z];vs[k+'b']=[x,y+thickness,z];
}
for(const[k,f]of Object.entries(old.faces))fs[k]=new MeshFace(m,{vertices:f.vertices,uv:f.uv,texture:tex.uuid});
Undo.initEdit({elements:[m]});m.vertices=vs;m.faces=fs;Canvas.updateAll();Undo.finishEdit('精简胸腹过渡：首节腹甲根部局部前伸，消除中央透缝');report.vertices=Object.keys(vs).length;report.faces=Object.keys(fs).length;return report;
})()`})));await fs.writeFile(d+'/work/junction_report_v6.json',JSON.stringify(report,null,2));console.log(JSON.stringify(report));await call('export_model',{codec_id:'project',path:d+'/work/v6_stage3.bbmodel',overwrite:true,max_content_length:0});
for(const[n,p,t,z]of[['junction_bottom',[0,-20,-.2],[0,1.7,-.2],6],['junction_oblique',[-8,-16,10],[0,1.7,-.1],5]]){const r=await call('set_camera_angle',{view:'beetle_v6',position:p,target:t,zoom:z,projection:'orthographic'});await fs.writeFile(d+'/work/v6_after_'+n+'.png',Buffer.from(r.content.find(c=>c.type==='image').data,'base64'));}
