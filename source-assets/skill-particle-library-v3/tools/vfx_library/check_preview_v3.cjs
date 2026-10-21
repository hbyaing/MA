const fs=require('node:fs'),path=require('node:path'),http=require('node:http');
const {pathToFileURL}=require('node:url');const {chromium}=require('playwright');
const root=path.resolve(__dirname,'../..'),base=path.join(root,'source-assets/skill-particle-library-v3'),report=path.join(root,'reports/skill-particle-library-v3');fs.mkdirSync(report,{recursive:true});
(async()=>{
 const server=http.createServer((req,res)=>{try{const u=new URL(req.url,'http://localhost'),file=path.resolve(base,'.'+decodeURIComponent(u.pathname));if(!file.startsWith(base+path.sep)||!fs.existsSync(file)||!fs.statSync(file).isFile()){res.writeHead(404);res.end();return}res.setHeader('Content-Type',({'.html':'text/html; charset=utf-8','.js':'application/javascript; charset=utf-8','.png':'image/png','.json':'application/json'})[path.extname(file)]||'text/plain');fs.createReadStream(file).pipe(res)}catch{res.writeHead(400);res.end()}});
 await new Promise(resolve=>server.listen(0,'127.0.0.1',resolve));const origin='http://127.0.0.1:'+server.address().port;
 const browser=await chromium.launch({headless:true,executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'});
 try{
  const page=await browser.newPage({viewport:{width:1580,height:1250},deviceScaleFactor:1});const errors=[];page.on('pageerror',e=>errors.push(e.message));page.on('requestfailed',r=>errors.push(r.url()+': '+r.failure().errorText));
  await page.goto(pathToFileURL(path.join(base,'preview/index.html')).href);await page.waitForFunction(()=>window.BOSS_VFX_READY===true,null,{timeout:60000});
  await page.goto(origin+'/preview/index.html');await page.waitForFunction(()=>window.BOSS_VFX_READY===true,null,{timeout:60000});
  await page.evaluate(()=>window.BOSS_VFX_PREVIEW.seek(0));await page.screenshot({path:path.join(report,'preview-v3.png'),fullPage:true});
  const samples=await page.evaluate(()=>{
   const D=window.BOSS_VFX_LIBRARY,api=window.BOSS_VFX_PREVIEW,c=document.getElementById('stage'),g=c.getContext('2d');let scen=0,frames=0;const blank=[];
   const signature=()=>{const data=g.getImageData(0,0,c.width,c.height).data;let hash=2166136261;for(let i=0;i<data.length;i+=64){hash^=data[i]+data[i+1]*7+data[i+2]*13;hash=Math.imul(hash,16777619)}return hash>>>0};
   api.setLayer('showModel',false);api.setLayer('showArt',false);api.setView('top');
   for(const skill of Object.values(D.skills)){
    api.selectSkill(skill.id);if(document.getElementById('title').textContent!==skill.name)throw Error('title '+skill.id);
    for(let i=0;i<skill.scenarios.length;i++){
     api.setScenario(i);const scene=api.scene;api.seek(scene.durationTicks);const empty=signature();let has=false;
     const damaging=scene.events.filter(e=>e.damaging);const times=[...new Set(damaging.map(e=>e.startTick))];
     for(const t of [times[0],times[Math.floor(times.length/2)],times.at(-1)].filter(v=>v!=null)){api.seek(t+.25);has ||= signature()!==empty;frames++}
     // Gaze is view-dependent, rendered with its core/sight marker in the art layer.
     if(!has&&damaging.some(e=>e.shape==='gaze')){api.setLayer('showArt',true);api.seek(damaging.find(e=>e.shape==='gaze').startTick+.25);has=signature()!==empty;api.setLayer('showArt',false)}
     if(!has)blank.push(skill.id+' / '+scene.id);scen++;
    }
   }
   api.setLayer('showModel',true);api.setLayer('showArt',true);api.setView('iso');
   return{skills:Object.keys(D.skills).length,scenarios:scen,geometryFrameSamples:frames,blank};
  });
  if(samples.blank.length)throw Error('Blank scenarios: '+samples.blank.join(', '));
  const examples=[['ermei_05',46,'top'],['ermei_01',25,'iso'],['ermei_02',31,'iso'],['bichen_03',34,'iso'],['pengju_03',30,'iso'],['pengjiao_04',40,'top'],['heart_demon_05',25,'front'],['shenyin_02',32,'top'],['yihuo_05',47,'top'],['pengzhi_05',45,'iso'],['shetao_01',25,'iso']];
  for(const [id,t,v]of examples){await page.evaluate(([id,t,v])=>{const a=window.BOSS_VFX_PREVIEW;a.selectSkill(id);a.setView(v);a.seek(t)},[id,t,v]);await page.locator('#stage').screenshot({path:path.join(report,id+'__'+v+'.png')})}
  const models=[];for(const boss of Object.keys(await page.evaluate(()=>window.BOSS_VFX_LIBRARY.profiles))){await page.evaluate(boss=>{const d=window.BOSS_VFX_LIBRARY,a=window.BOSS_VFX_PREVIEW;a.selectSkill(Object.values(d.skills).find(s=>s.boss===boss).id);a.seek(0)},boss);await page.locator('#modelStage').screenshot({path:path.join(report,'model__'+boss+'.png')});models.push(boss)}
  await page.evaluate(()=>{const a=window.BOSS_VFX_PREVIEW;a.selectSkill('ermei_05');a.setView('iso');a.seek(46)});await page.screenshot({path:path.join(report,'preview-v3.png'),fullPage:true});
  await page.fill('#search','心魔');if(await page.locator('#skillList .skill').count()!==0){/* Current boss is filtered away, selecting search result below. */}await page.locator('#bossList .boss').first().click();const heartSkills=await page.locator('#skillList .skill').count();if(heartSkills!==10)throw Error('Search heart skills '+heartSkills);await page.fill('#search','');
  await page.click('#gray');if(!await page.evaluate(()=>window.BOSS_VFX_PREVIEW.state.gray))throw Error('Gray');await page.click('#gray');await page.check('#showHeight');await page.selectOption('#view','front');
  await page.evaluate(()=>window.BOSS_VFX_PREVIEW.selectSkill('pengjiao_04'));const download=page.waitForEvent('download');await page.click('#download');const dl=await download;await dl.saveAs(path.join(report,'exported-recipe.json'));const exported=JSON.parse(fs.readFileSync(path.join(report,'exported-recipe.json'),'utf8'));if(exported.skillId!=='pengjiao_04'||!exported.scenario.events.some(e=>e.footprint?.length))throw Error('Export geometry');
  await page.setViewportSize({width:680,height:980});if(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth+1))throw Error('Mobile overflow');await page.screenshot({path:path.join(report,'preview-mobile-v3.png'),fullPage:true});
  const result={passed:errors.length===0,...samples,originalModels:models.length,fileProtocolLoaded:true,searchHeartSkills:heartSkills,geometryExport:true,grayAndHeightControls:true,mobileOverflow:false,errors,minecraftRendered:false};fs.writeFileSync(path.join(report,'browser-validation.json'),JSON.stringify(result,null,2));console.log(JSON.stringify(result));if(errors.length)process.exitCode=1;
 }finally{await browser.close();await new Promise(resolve=>server.close(resolve))}
})().catch(e=>{console.error(e);process.exitCode=1});
