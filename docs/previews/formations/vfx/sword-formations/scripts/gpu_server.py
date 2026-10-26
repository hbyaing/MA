"""Loopback-only, single-context GPU preview service; accepts bounded numeric scene parameters."""
from pathlib import Path
from http.server import BaseHTTPRequestHandler,HTTPServer
import argparse,io,json,math,time,traceback,subprocess,os
import glfw,moderngl,numpy as np
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
FLOATS={'time':(0,10,4),'yaw':(-100000,100000,.55),'pitch':(-.1,1.52,.42),'distance':(5,110,44),'targetY':(0,20,5.8),'tail':(.3,1.8,1),'rotation':(.2,3,1),'glow':(0,1.5,.65)}
INTS={'width':(240,1280,800),'height':(200,960,600),'seq':(0,2147483647,0),'density':(0,2,1),'bloom':(0,1,1),'particles':(0,1,1),'trails':(0,1,1),'sockets':(0,1,0),'loop':(0,1,1),'autoView':(0,1,1)}
def validate(data):
 if not isinstance(data,dict) or set(data)-set(FLOATS)-set(INTS)-{'mode'}:raise ValueError('Unknown parameters')
 out={'mode':data.get('mode','longyuan')}
 if out['mode'] not in ('longyuan','wanjian'):raise ValueError('Invalid mode')
 for k,(lo,hi,default) in {**FLOATS,**INTS}.items():
  v=data.get(k,default)
  if isinstance(v,bool) or not isinstance(v,(int,float)) or not math.isfinite(v) or not lo<=v<=hi or (k in INTS and int(v)!=v):raise ValueError('Invalid '+k)
  out[k]=int(v) if k in INTS else float(v)
 if out['width']*out['height']>1000000:raise ValueError('Frame too large')
 return out
class Renderer:
 def __init__(self):
  import re,base64
  self.re=re;self.base64=base64
  if not glfw.init():raise RuntimeError('GLFW unavailable')
  glfw.window_hint(glfw.VISIBLE,False);glfw.window_hint(glfw.CONTEXT_VERSION_MAJOR,3);glfw.window_hint(glfw.CONTEXT_VERSION_MINOR,3);glfw.window_hint(glfw.OPENGL_PROFILE,glfw.OPENGL_CORE_PROFILE)
  self.window=glfw.create_window(64,64,'Sword preview GPU',None,None);glfw.make_context_current(self.window);self.ctx=moderngl.create_context(require=330)
  self.worker=subprocess.Popen([str(Path.home()/'.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin/node.exe'),str(ROOT/'scripts/source-bridge.mjs')],stdin=subprocess.PIPE,stdout=subprocess.PIPE,text=True,encoding='utf-8',creationflags=subprocess.CREATE_NO_WINDOW)
  init=json.loads(self.worker.stdout.readline());self.programs={};self.attributes={};self.shaders={};self.textures={}
  for item in init['programs']:
   shaders={s['type']:s['code'].replace('#version 300 es','#version 330').replace('precision highp float;','') for s in item['shaders']}
   p=self.ctx.program(vertex_shader=shaders['VERTEX_SHADER'],fragment_shader=shaders['FRAGMENT_SHADER']);self.programs[item['id']]=p;self.shaders[item['id']]=shaders
   self.attributes[item['id']]={int(i):name for i,name in re.findall(r'layout\(location=(\d+)\)in\s+\w+\s+(\w+)',shaders['VERTEX_SHADER'])}
  self.sky=next(p for p in self.programs.values() if 'VP'not in p and 'Source'not in p and 'Strength'not in p);self.blur=next(p for p in self.programs.values() if 'Source'in p);self.comp=next(p for p in self.programs.values() if 'Strength'in p)
  self.skyvao=self.ctx.vertex_array(self.sky,[]);self.blurvao=self.ctx.vertex_array(self.blur,[]);self.compvao=self.ctx.vertex_array(self.comp,[]);self.size=None;self.resources=[];self.frames=0
 def resize(self,w,h):
  if self.size==(w,h):return
  self.ctx.screen.use()
  for r in self.resources:r.release()
  self.resources=[];self.size=(w,h)
  def tx(size,dtype='f2'):
   t=self.ctx.texture(size,4,dtype=dtype);t.filter=(moderngl.LINEAR,moderngl.LINEAR);t.repeat_x=t.repeat_y=False;self.resources.append(t);return t
  self.scene=tx((w,h));self.bright=tx((w,h));self.a=tx((max(1,w//2),max(1,h//2)));self.b=tx(self.a.size);self.final=tx((w,h),'f1');self.depth=self.ctx.depth_texture((w,h))
  self.fb=self.ctx.framebuffer([self.scene,self.bright],self.depth);self.clearbright=self.ctx.framebuffer([self.bright]);self.fa=self.ctx.framebuffer([self.a]);self.fbb=self.ctx.framebuffer([self.b]);self.ff=self.ctx.framebuffer([self.final]);self.resources.extend([self.fb,self.clearbright,self.fa,self.fbb,self.ff,self.depth])
 def render(self,d):
  start=time.perf_counter();w,h=d['width'],d['height'];self.resize(w,h);self.worker.stdin.write(json.dumps(d)+'\n');self.worker.stdin.flush();frame=json.loads(self.worker.stdout.readline())
  if 'error'in frame:raise RuntimeError(frame['error'])
  self.fb.use();self.ctx.viewport=(0,0,w,h);self.fb.clear(.045,.09,.12,1,depth=1);self.clearbright.clear(0,0,0,0);self.fb.use();self.ctx.disable(moderngl.DEPTH_TEST|moderngl.BLEND);self.skyvao.render(vertices=3);self.count=frame.get('count',0);self.ctx.enable(moderngl.PROGRAM_POINT_SIZE)
  for call in frame['draws']:
   p=self.programs[call['program']];self.ctx.enable(moderngl.DEPTH_TEST) if call['depth'] else self.ctx.disable(moderngl.DEPTH_TEST);self.ctx.depth_mask=call['mask']
   if call['blend']:self.ctx.enable(moderngl.BLEND);self.ctx.blend_func=(moderngl.SRC_ALPHA,moderngl.ONE)
   else:self.ctx.disable(moderngl.BLEND)
   for key,val in call['uniforms'].items():
    if key not in p:continue
    if key=='VP':p[key].write(np.asarray(val,dtype='f4').tobytes())
    else:p[key].value=tuple(val) if isinstance(val,list) else val
   for slot,file in call['textures']:
    if file not in self.textures:
     im=Image.open(ROOT/file).convert('RGBA');tex=self.ctx.texture(im.size,4,im.tobytes());tex.filter=(moderngl.NEAREST,moderngl.NEAREST);self.textures[file]=tex
    self.textures[file].use(slot-1000)
   contents=[];buffers=[]
   for attr in call['attrs']:
    name=self.attributes[call['program']].get(attr['i'])
    if name not in p:continue
    raw=self.base64.b64decode(attr['buffer']);stride=attr['stride']//4;off=attr['offset']//4;n=attr['n']
    data=np.frombuffer(raw,dtype='f4').reshape(-1,stride)[:,off:off+n].copy();buf=self.ctx.buffer(data.tobytes());buffers.append(buf);contents.append((buf,str(n)+'f'+(' /i' if attr['divisor'] else ''),name))
   vao=self.ctx.vertex_array(p,contents);vao.render(mode={'TRIANGLES':moderngl.TRIANGLES,'LINES':moderngl.LINES,'POINTS':moderngl.POINTS}[call['mode']],vertices=call['n'],first=call['first'],instances=call['instances']);vao.release()
   for buf in buffers:buf.release()
  self.ctx.depth_mask=True;self.ctx.disable(moderngl.DEPTH_TEST|moderngl.BLEND)
  for fb,tex,offset in [(self.fa,self.bright,(2.4/w,0)),(self.fbb,self.a,(0,2.4/h))]:
   fb.use();self.ctx.viewport=(0,0,*self.a.size);tex.use(0);self.depth.use(1);self.blur['Source']=0;self.blur['Depth']=1;self.blur['Step']=offset;self.blurvao.render(vertices=3)
  self.ff.use();self.ctx.viewport=(0,0,w,h);self.scene.use(0);self.b.use(1)
  for k,v in {'Scene':0,'Glow':1,'Strength':d['glow']*.95*d['bloom']}.items():self.comp[k]=v
  self.compvao.render(vertices=3);pixels=self.ff.read(components=3,alignment=1);err=self.ctx.error
  if err!='GL_NO_ERROR':raise RuntimeError(err)
  im=Image.frombytes('RGB',(w,h),pixels).transpose(Image.Transpose.FLIP_TOP_BOTTOM);out=io.BytesIO();im.save(out,format='JPEG',quality=90);self.frames+=1;return out.getvalue(),(time.perf_counter()-start)*1000
 def close(self):self.worker.terminate();glfw.destroy_window(self.window);glfw.terminate()

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--port',type=int,default=0);a=ap.parse_args();renderer=Renderer()
 class Handler(BaseHTTPRequestHandler):
  def log_message(self,*args):pass
  def end_headers(self):
   origin=self.headers.get('Origin','')
   if origin in ('http://127.0.0.1:18731','http://localhost:18731'):
    self.send_header('Access-Control-Allow-Origin',origin);self.send_header('Vary','Origin');self.send_header('Access-Control-Expose-Headers','X-Frame-Id,X-Render-MS,X-Sword-Count')
   super().end_headers()
  def do_OPTIONS(self):
   if self.headers.get('Origin','') not in ('http://127.0.0.1:18731','http://localhost:18731'):return self.answer(403,b'{}')
   self.send_response(204);self.send_header('Access-Control-Allow-Methods','GET,POST,OPTIONS');self.send_header('Access-Control-Allow-Headers','Content-Type,X-Workbench');self.send_header('Access-Control-Max-Age','600');self.send_header('Content-Length','0');self.end_headers()

  def answer(self,status,body,content='application/json'):
   self.send_response(status);self.send_header('Content-Type',content);self.send_header('Content-Length',str(len(body)));self.send_header('Cache-Control','no-store');self.end_headers();self.wfile.write(body)
  def do_GET(self):
   if self.path!='/status':return self.answer(404,b'{}')
   self.answer(200,json.dumps({'service':'xiantu-formation-gpu','renderer':renderer.ctx.info['GL_RENDERER'],'frames':renderer.frames,'live':True}).encode())
  def do_POST(self):
   if self.path!='/render':return self.answer(404,b'{}')
   if self.headers.get('X-Workbench')!='local' or self.headers.get('Origin','') not in ('','http://127.0.0.1:18731','http://localhost:18731'):return self.answer(403,b'{}')
   try:
    length=int(self.headers.get('Content-Length',0))
    if not 0<length<=16384:raise ValueError('Request size')
    d=validate(json.loads(self.rfile.read(length)));image,ms=renderer.render(d);self.send_response(200);self.send_header('Content-Type','image/jpeg');self.send_header('Content-Length',str(len(image)));self.send_header('Cache-Control','no-store');self.send_header('X-Sword-Count',str(renderer.count));self.send_header('X-Frame-Id',str(d['seq']));self.send_header('X-Render-MS',f'{ms:.2f}');self.end_headers();self.wfile.write(image)
   except (ValueError,TypeError,json.JSONDecodeError) as e:self.answer(400,json.dumps({'error':str(e)}).encode())
   except (BrokenPipeError,ConnectionResetError):pass
   except Exception as e:
    traceback.print_exc();self.answer(500,json.dumps({'error':str(e)}).encode())
 server=HTTPServer(('127.0.0.1',a.port),Handler);print(json.dumps({'port':server.server_port,'renderer':renderer.ctx.info['GL_RENDERER']}),flush=True)
 try:server.serve_forever()
 except KeyboardInterrupt:pass
 finally:server.server_close();renderer.close()
if __name__=='__main__':main()
