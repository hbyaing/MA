import assert from 'node:assert/strict';
import {herbs,defaults,validate,compute} from './engine.mjs';
let checks=0;function check(v,m){checks++;assert.ok(v,m);}
const s=defaults(),r=compute(s);check(r.rank>=1&&r.rank<=9,'default yields valid rank');check(JSON.stringify(compute(s))===JSON.stringify(r),'deterministic');
const empty=defaults();empty.slots.fill(null);check(compute(empty).rank===0&&compute(empty).bottles===0,'empty cannot yield');
const twice=structuredClone(s);twice.slots.forEach(s=>{if(s)s.qty*=2;});const doubled=compute(twice);check(Math.abs(r.score-doubled.score)<1e-8,'uniform extra quantity cannot raise grade');check(doubled.bottles>=r.bottles,'quantity affects yield');
const toxic=defaults();toxic.slots=[{id:'herb_duzhen',years:900,qty:3},{id:'herb_lieyang',years:600,qty:2},null,null,null,null,null,null];toxic.env.heat=98;toxic.env.time=220;toxic.env.fire='sun';check(compute(toxic).failed,'failure preset must fail');
for(const h of herbs)for(const years of [1,10,100,1000,10000])for(const heat of [0,30,60,100])for(const fire of ['balanced','wood','water','sun']){
 const x=defaults();x.slots=[{id:h.id,years,qty:1},null,null,null,null,null,null,null];Object.assign(x.env,{heat,fire});const a=compute(x);
 for(const k of ['score','power','purity','stability','toxin'])check(Number.isFinite(a[k])&&a[k]>=0&&a[k]<=100,'bounded result '+k);
 check(a.rank>=0&&a.rank<=9,'rank bounds');check(a.bottles>=0&&a.bottles<=8,'yield bounds');check(Object.values(a.final).every(Number.isFinite),'finite effects');
}
for(const bad of [null,{}, {...s,schema:'bad'}, {...s,slots:[]},{...s,env:{...s.env,qi:NaN}},{...s,slots:[{id:'unknown',years:1,qty:1},...s.slots.slice(1)]}])assert.throws(()=>validate(bad));
const before=JSON.stringify(s);compute(s);check(before===JSON.stringify(s),'calculation never mutates recipe');
console.log(JSON.stringify({checks,defaultRank:r.rank,defaultScore:r.score,status:'passed'}));
