// Research recipe v1. No game balances, saved item tiers or random quality rolls are imported.
export const herbs=[
 ['spirit_sprout_grass','灵芽草','灵气',34,7,3,5,2,3,8,1],
 ['dew_drop_flower','凝露花','水润',16,28,2,6,-8,28,16,0],
 ['clear_heart_lotus','清心莲','清净',12,18,1,28,-5,20,38,0],
 ['spirit_ginseng','灵参','滋养',24,38,12,4,5,4,8,1],
 ['red_sun_grass','赤阳草','温阳',18,9,30,3,35,0,2,4],
 ['blood_spirit_flower','血灵花','气血',5,40,26,0,12,2,0,12],
 ['gale_grass','疾风草','轻灵',20,7,12,10,4,2,9,1],
 ['thunder_vein_grass','雷纹草','刚烈',10,6,38,8,30,0,0,10],
 ['herb_juling','聚灵草','聚气',42,5,3,5,1,2,10,1],
 ['herb_huichun','回春草','生机',7,46,3,2,1,12,9,0],
 ['herb_jingang','金刚草','炼体',4,8,47,0,18,0,8,3],
 ['herb_hanbing','寒冰草','寒性',6,10,5,12,-36,35,12,2],
 ['herb_lieyang','烈阳花','烈性',14,3,28,3,44,0,0,9],
 ['herb_tianji','天机花','神魂',20,7,1,42,-2,3,15,1],
 ['herb_duzhen','毒鸩草','毒性',4,12,15,2,10,2,0,48],
 ['herb_huanling','幻灵草','幻性',10,4,3,44,-4,7,7,12]
].map(([id,name,nature,qi,life,body,soul,heat,water,clean,toxin])=>({id,name,nature,qi,life,body,soul,heat,water,clean,toxin}));
export const types=[{key:'qi',name:'回灵丹',icon:'aura_pill',unit:'灵力'},{key:'life',name:'回春丹',icon:'rejuvenation_pill',unit:'生命'},{key:'body',name:'金刚丹',icon:'diamond_body_pill',unit:'护体'},{key:'soul',name:'养神丹',icon:'calming_pill',unit:'神识'}];
export const defaults=()=>({schema:'myriad-alchemy-rework/1',target:'qi',slots:[{id:'herb_juling',years:100,qty:2},{id:'dew_drop_flower',years:80,qty:1},{id:'clear_heart_lotus',years:120,qty:1},null,null,null,null,null],env:{qi:55,temp:24,humidity:60,moon:50,heat:62,time:90,fire:'balanced'}});
const bounded=(v,a,b)=>Math.max(a,Math.min(b,v));
export function validate(raw){
 if(!raw||raw.schema!=='myriad-alchemy-rework/1'||!types.some(t=>t.key===raw.target)||!Array.isArray(raw.slots)||raw.slots.length!==8)throw Error('不是兼容的八槽炼丹推演配方');
 const limits={qi:[0,100],temp:[-10,50],humidity:[0,100],moon:[0,100],heat:[0,100],time:[20,240]};
 if(!raw.env||!['balanced','wood','water','sun'].includes(raw.env.fire))throw Error('异火设置无效');
 const env={fire:raw.env.fire};for(const [key,[a,b]] of Object.entries(limits)){const v=raw.env[key];if(typeof v!=='number'||!Number.isFinite(v)||v<a||v>b)throw Error('环境参数超出范围：'+key);env[key]=v;}
 const slots=raw.slots.map(s=>{if(s===null)return null;if(!s||!herbs.some(h=>h.id===s.id)||!Number.isInteger(s.years)||s.years<1||s.years>10000||!Number.isInteger(s.qty)||s.qty<1||s.qty>8)throw Error('药材、年份或数量无效');return {id:s.id,years:s.years,qty:s.qty};});
 return {schema:raw.schema,target:raw.target,slots,env};
}
export function compute(input){
 const state=validate(input),e=state.env,slots=state.slots.filter(Boolean),sum=slots.reduce((a,s)=>a+s.qty,0),empty=sum===0;
 const vector={qi:0,life:0,body:0,soul:0,heat:0,water:0,clean:0,toxin:0};let power=0,ageSum=0;const inputs=[];
 for(const s of slots){const herb=herbs.find(h=>h.id===s.id),age=1+.45*Math.log10(1+s.years/10),dose=s.qty;ageSum+=s.years*dose;power+=age*dose;
  for(const k of Object.keys(vector))vector[k]+=herb[k]*age*dose;
  inputs.push({...s,name:herb.name,multiplier:age,contribution:(herb[state.target]*age*dose)});
 }
 const avg=sum?power/sum:0,avgAge=sum?ageSum/sum:0;
 const avgHeat=sum?vector.heat/sum:0,ideal=bounded(52+avgHeat*.4,30,85),idealTime=bounded(65+avg*.9+avgAge*.03,40,180);
 const extraction=bounded(1-Math.abs(e.heat-ideal)/100-Math.abs(e.time-idealTime)/400,.12,1);
 const reactions=[];let harmony=0;
 // Consume finite raw pools exactly once. Reactions transform existing components, never multiply all pairs.
 const neutral=Math.min(Math.max(0,vector.heat),vector.water*.65);
 if(neutral>1){vector.heat-=neutral;vector.water-=neutral/.65;vector.clean+=neutral*.2;harmony+=neutral/Math.max(1,sum)*.4;reactions.push({name:'水火调和',detail:'寒润与温阳相制，消耗相应药性，少量转为清净。',value:neutral});}
 const detox=Math.min(vector.toxin,vector.clean*.65);
 if(detox>1){vector.toxin-=detox;vector.clean-=detox/.65;reactions.push({name:'清净制毒',detail:'清净药性被消耗，用于削减毒性；不白送有效药力。',value:detox});}
 // Ordered source modulation: only neighbouring doses share one bounded mixing budget.
 let sequence=0;for(let i=1;i<slots.length;i++){const a=herbs.find(h=>h.id===slots[i-1].id),b=herbs.find(h=>h.id===slots[i].id);if(a.water>15&&b.heat>20)sequence+=2;if(a.heat>20&&b.water>15)sequence-=2;}
 if(sequence)reactions.push({name:'投药先后',detail:sequence>0?'先润后温，融合更缓和。':'先烈后寒，融合额外承压。',value:sequence});
 const moisture=1-Math.abs(e.humidity-55)/220,temperature=1-Math.abs(e.temp-24)/180;
 const fireMod=e.fire==='wood'?{life:1.12,body:.94}:e.fire==='water'?{soul:1.12,qi:1.04,body:.90}:e.fire==='sun'?{body:1.14,life:.92}:{};
 const final={};for(const t of types)final[t.key]=empty?0:vector[t.key]/sum*extraction*moisture*temperature*(1+e.qi/300)*(1+(t.key==='soul'?e.moon/500:0))*(fireMod[t.key]||1);
 const ordered=types.slice().sort((a,b)=>final[b.key]-final[a.key]),kind=ordered[0],match=empty?0:bounded(final[state.target]/Math.max(1,final[kind.key]),0,1);
 const toxin=empty?0:bounded(vector.toxin/sum*1.6+(e.heat>85?(e.heat-85)*.8:0),0,100);
 const purity=empty?0:bounded(68+vector.clean/sum*.65-toxin*.6-Math.abs(e.heat-ideal)*.32,0,100);
 const stability=empty?0:bounded(89-Math.abs(e.heat-ideal)*.85-Math.abs(e.time-idealTime)*.14+harmony+sequence-(e.fire==='sun'?7:0),0,100);
 const potency=empty?0:bounded(avg*25*extraction*(.85+e.qi/250),0,100);
 const score=empty?0:bounded(.48*potency+.28*purity+.24*stability-toxin*.22,0,100);
 const failed=empty||stability<25||purity<25||score<10;
 const rank=failed?0:Math.min(9,Math.max(1,Math.floor(score/10))),bottles=failed?0:Math.max(1,Math.min(8,Math.floor(sum/3)));
 const effective=failed?0:final[kind.key]*(.5+purity/200)*(1-toxin/200),duration=failed?0:Math.round(15+stability*.45);
 return {empty,inputs,vector,final,avgAge,avg,power:potency,purity,stability,toxin,score,rank,failed,kind,match,bottles,effective,duration,ideal,idealTime,extraction,reactions};
}
