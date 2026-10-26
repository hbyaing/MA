// Shared orbit controls: viewport-normalized drag, bounded zoom, no release inertia.
export function bindOrbit(surface,{read,write,prepare=()=>{},minPitch=.12,maxPitch=1.52,minDistance=7,maxDistance=70}){
 surface.style.touchAction='none';surface.style.cursor='grab';surface.style.userSelect='none';
 if(surface.tagName==='IMG')surface.draggable=false;
 for(const img of surface.querySelectorAll('img'))img.draggable=false;
 let drag=null,target=null,animation=0,last=0;
 const initial={...read()};
 function apply(value){write({yaw:value.yaw,pitch:Math.max(minPitch,Math.min(maxPitch,value.pitch)),distance:Math.max(minDistance,Math.min(maxDistance,value.distance))});}
 function animate(now){const dt=last?Math.min(.05,(now-last)/1000):1/60;last=now;const current=read(),blend=1-Math.exp(-24*dt);let value={yaw:current.yaw+(target.yaw-current.yaw)*blend,pitch:current.pitch+(target.pitch-current.pitch)*blend,distance:current.distance+(target.distance-current.distance)*blend};apply(value);if(Math.abs(value.yaw-target.yaw)+Math.abs(value.pitch-target.pitch)+Math.abs(value.distance-target.distance)>.0001)animation=requestAnimationFrame(animate);else{apply(target);animation=0;last=0;}}
 function schedule(){if(!animation){last=0;animation=requestAnimationFrame(animate);}}
 function end(e){if(!drag||(e&&e.pointerId!==undefined&&e.pointerId!==drag.id))return;const id=drag.id;drag=null;surface.style.cursor='grab';if(target)apply(target);cancelAnimationFrame(animation);animation=0;last=0;if(surface.hasPointerCapture(id))surface.releasePointerCapture(id);}
 surface.ondragstart=e=>e.preventDefault();
 surface.onpointerdown=e=>{if(e.button!==0||!e.isPrimary||e.target.closest('button,input,select,a,label'))return;e.preventDefault();prepare();target={...read()};drag={id:e.pointerId,x:e.clientX,y:e.clientY};surface.setPointerCapture(e.pointerId);surface.style.cursor='grabbing';};
 surface.onpointermove=e=>{if(!drag||e.pointerId!==drag.id)return;if(e.pointerType==='mouse'&&!(e.buttons&1)){end(e);return;}e.preventDefault();const unit=Math.PI*.85/Math.max(240,Math.min(surface.clientWidth,surface.clientHeight));target.yaw-=(e.clientX-drag.x)*unit;target.pitch=Math.max(minPitch,Math.min(maxPitch,target.pitch+(e.clientY-drag.y)*unit));drag.x=e.clientX;drag.y=e.clientY;schedule();};
 surface.onpointerup=surface.onpointercancel=surface.onlostpointercapture=end;
 window.addEventListener('blur',()=>end());document.addEventListener('visibilitychange',()=>{if(document.hidden)end();});
 surface.addEventListener('wheel',e=>{if(e.target.closest('input,select'))return;e.preventDefault();prepare();target={...read()};const pixels=e.deltaY*(e.deltaMode===1?16:e.deltaMode===2?surface.clientHeight:1);target.distance=Math.max(minDistance,Math.min(maxDistance,target.distance*Math.exp(Math.max(-200,Math.min(200,pixels))*.001)));schedule();},{passive:false});
 surface.ondblclick=e=>{if(e.target.closest('button,input,select,a,label'))return;end();cancelAnimationFrame(animation);animation=0;target={...initial};apply(target);};
 return ()=>{end();cancelAnimationFrame(animation);};
}
