import {herbs} from './engine.mjs';
export const parameterSources=new Map();
for(const key of ['power','purity','stability','toxin','score'])parameterSources.set('result.'+key,(state,r)=>r[key]);
for(const key of ['qi','life','body','soul'])parameterSources.set('property.'+key,(state,r)=>r.final[key]);
for(const key of ['qi','heat','temp','humidity','moon','time'])parameterSources.set('environment.'+key,(state,r)=>state.env[key]);
const tagSources=new Map([
 ['material_observations',(s,r)=>s.slots.flatMap((slot,index)=>{if(!slot)return [];const h=herbs.find(h=>h.id===slot.id);return [{id:'material:'+h.id,label:h.name+' · '+h.nature,present:true,strength:null,unit:'',source:'药槽 '+(index+1)+' / '+slot.years+'年 × '+slot.qty+'株',budget:null}];})],
 ['process_observations',(s,r)=>r.reactions.map((x,index)=>({id:'process:prototype_'+index,label:x.name,present:true,strength:x.value,unit:'演示转化量',source:'网页原型反应记录：'+x.detail,budget:null}))]
]);
export function validateDisplay(raw){
 const categories=['material','process','structure','effect','evidence'];
 if(!raw||raw.schema!=='alchemy-display/1'||!Array.isArray(raw.parameters)||raw.parameters.length>32||!Array.isArray(raw.tags)||raw.tags.length>16)throw Error('展示定义版本或容量无效');
 const ids=new Set();for(const p of raw.parameters){if(!p||typeof p.id!=='string'||!/^[a-z0-9_.:-]+$/.test(p.id)||ids.has(p.id)||!parameterSources.has(p.source)||typeof p.label!=='string'||p.label.length>40||typeof p.unit!=='string'||p.unit.length>20||!['normal','danger','score'].includes(p.style))throw Error('参数重复、超长或数据源未注册');ids.add(p.id);}
 for(const t of raw.tags)if(!t||!tagSources.has(t.source)||!categories.includes(t.category)||typeof t.label!=='string'||t.label.length>40)throw Error('标签定义无效或来源未注册');
 return structuredClone(raw);
}
export function projectDisplay(definitions,state,result){return {parameters:definitions.parameters.map(p=>({...p,value:parameterSources.get(p.source)(state,result)})),tags:definitions.tags.flatMap(t=>tagSources.get(t.source)(state,result).map(tag=>({...tag,category:t.category,categoryLabel:t.label})))};}
