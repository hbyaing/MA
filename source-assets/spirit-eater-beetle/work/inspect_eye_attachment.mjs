import{connect,call,getText}from'./mcp.mjs';await connect();console.log(getText(await call('risky_eval',{code:`(()=>{
const out=[];for(const m of Mesh.all.filter(m=>m.name.includes('前胸盾片'))){for(const [key,f]of Object.entries(m.faces)){
const ks=f.vertices;for(let i=1;i<ks.length-1;i++){const vs=[ks[0],ks[i],ks[i+1]],uv=vs.map(k=>f.uv[k]);const [a,b,c]=uv;
const den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1]);if(Math.abs(den)<.001)continue;
for(const p of [[523,630],[520,631],[525,628],[523,626],[518,630]]){const u=((b[1]-c[1])*(p[0]-c[0])+(c[0]-b[0])*(p[1]-c[1]))/den,v=((c[1]-a[1])*(p[0]-c[0])+(a[0]-c[0])*(p[1]-c[1]))/den,w=1-u-v;if(Math.min(u,v,w)>=0)out.push({mesh:m.name,face:key,uv:p,xyz:[0,1,2].map(d=>u*m.vertices[vs[0]][d]+v*m.vertices[vs[1]][d]+w*m.vertices[vs[2]][d])});}
}}}return {points:out,existingEyeMeshes:Mesh.all.filter(m=>m.name.includes('eye')).map(m=>m.name),groups:Group.all.filter(g=>g.name.includes('_eye_')||g.name.includes('_antenna_')).map(g=>({name:g.name,origin:g.origin}))};})()`})));
