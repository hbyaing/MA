const canvas=document.getElementById('scene');
if(canvas.getContext('webgl2',{antialias:true,alpha:false})){import('./app.js');}else{import('./native.js?v=2').catch(e=>{document.getElementById('error').textContent=e.message})}
