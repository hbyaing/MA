import fs from 'node:fs';
import readline from 'node:readline';
const file=new URL('../../sword-formations/formation.js',import.meta.url);
const {fieldPosition,direction,count}=await import('data:text/javascript;base64,'+fs.readFileSync(file).toString('base64'));
const cross=(a,b)=>[a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]],norm=a=>{let l=Math.hypot(...a)||1;return a.map(v=>v/l)};
for await(const line of readline.createInterface({input:process.stdin})){try{
 const {time:t,radius}=JSON.parse(line),n=count('longyuan',0),positions=[],directions=[];
 for(let i=0;i<n;i++){
 const fn=v=>fieldPosition(i,n,v,v),p=fn(t),f=direction(fn,t),side=norm(cross(f,[0,1,0])),tier=Math.min(2,Math.floor(i/(n/3)));
 for(let j=0;j<5;j++){const row=j===0?0:j<3?1:2,lateral=j===0?0:(j%2?-.34:.34)*(row===2?1.6:1),along=-row*.65,up=j===0?0:(row===1?.12:-.13)+Math.sin(t*1.8+i+j)*.045;
 const q=p.map((v,k)=>(v+side[k]*lateral+f[k]*along+(k===1?up:0))*(k===1?.45:radius*1.85/23));
 const d=norm(f.map((v,k)=>v*(k===1?.45:radius*1.85/23)));
 positions.push(...q,(.70+tier*.05)*(j===0?1:.88)*.45);directions.push(...d,1);
 }}process.stdout.write(JSON.stringify([...positions,...directions])+'\n');
 }catch(e){process.stderr.write(String(e)+'\n');process.stdout.write('[]\n');}}
