"""Loopback-only, single-context GPU preview service; accepts bounded numeric scene parameters."""
from pathlib import Path
from http.server import BaseHTTPRequestHandler,HTTPServer
import argparse,io,json,math,time,traceback
import glfw,moderngl,numpy as np
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
FLOATS={'time':(0,1e9,5.2),'power':(0,1.5,1),'speed':(0,1.5,1),'radius':(3,7,5.2),'density':(.1,2,1),'yaw':(-100000,100000,.52),'pitch':(.12,1.52,.73),'distance':(7,28,16),'reveal':(0,1,1),'glow':(0,2,.55)}
INTS={'width':(240,1280,800),'height':(200,960,550),'seq':(0,2147483647,0),'palette':(0,2,0),'steps':(24,64,40),'bloom':(0,1,1)}
def validate(data):
 if not isinstance(data,dict) or set(data)-set(FLOATS)-set(INTS):raise ValueError('Unknown frame parameters')
 out={}
 for name,(lo,hi,default) in {**FLOATS,**INTS}.items():
  value=data.get(name,default)
  if isinstance(value,bool) or not isinstance(value,(int,float)) or not math.isfinite(value) or not lo<=value<=hi:raise ValueError('Invalid '+name)
  if name in INTS and int(value)!=value:raise ValueError('Integer required: '+name)
  out[name]=int(value) if name in INTS else float(value)
 if out['steps'] not in (24,40,64):raise ValueError('Invalid quality')
 if out['width']*out['height']>1000000:raise ValueError('Frame too large')
 return out
class Renderer:
 def __init__(self):
  if not glfw.init():raise RuntimeError('GLFW unavailable')
  glfw.window_hint(glfw.VISIBLE,False);glfw.window_hint(glfw.CONTEXT_VERSION_MAJOR,3);glfw.window_hint(glfw.CONTEXT_VERSION_MINOR,3);glfw.window_hint(glfw.OPENGL_PROFILE,glfw.OPENGL_CORE_PROFILE)
  self.window=glfw.create_window(64,64,'Xiantu GPU worker',None,None)
  if not self.window:raise RuntimeError('OpenGL context unavailable')
  glfw.make_context_current(self.window);self.ctx=moderngl.create_context(require=330)
  vertex='#version 330\nin vec2 position;out vec2 uv;void main(){uv=position*.5+.5;gl_Position=vec4(position,0,1);}'
  core=(ROOT/'shader/formation.glsl').read_text(encoding='utf-8-sig')
  fragment='#version 330\nin vec2 uv;layout(location=0)out vec4 FragColor;layout(location=1)out vec4 BrightColor;\n'+core+'\nvoid main(){vec3 c,e;formation(uv,c,e);FragColor=vec4(c,1.);BrightColor=vec4(max(e-vec3(.09),vec3(0.)),1.);}'
  self.p=self.ctx.program(vertex_shader=vertex,fragment_shader=fragment);header='#version 330\nin vec2 uv;out vec4 FragColor;\n'
  self.blur=self.ctx.program(vertex_shader=vertex,fragment_shader=header+(ROOT/'shader/blur.glsl').read_text())
  composite=(ROOT/'shader/composite.glsl').read_text().replace('uniform float Bloom;','uniform float Bloom;uniform float Glow;').replace('*.55*Bloom','*Glow*Bloom')
  self.composite=self.ctx.program(vertex_shader=vertex,fragment_shader=header+composite)
  self.buffer=self.ctx.buffer(np.array([-1,-1,3,-1,-1,3],dtype='f4').tobytes());self.vaos={p:self.ctx.vertex_array(p,[(self.buffer,'2f','position')]) for p in [self.p,self.blur,self.composite]}
  image=Image.open(ROOT/'textures/flow-noise.png').convert('RGBA');self.noise=self.ctx.texture(image.size,4,image.tobytes());self.noise.repeat_x=self.noise.repeat_y=True;self.noise.filter=(moderngl.LINEAR,moderngl.LINEAR)
  self.size=None;self.resources=[];self.frames=0
 def resize(self,w,h):
  if self.size==(w,h):return
  for r in self.resources:r.release()
  self.resources=[];self.size=(w,h)
  def tx(size,dtype='f2'):
   t=self.ctx.texture(size,4,dtype=dtype);t.filter=(moderngl.LINEAR,moderngl.LINEAR);t.repeat_x=t.repeat_y=False;self.resources.append(t);return t
  self.main=tx((w,h));self.bright=tx((w,h));self.a=tx((max(1,w//4),max(1,h//4)));self.b=tx(self.a.size);self.final=tx((w,h),'f1')
  self.fb=self.ctx.framebuffer([self.main,self.bright]);self.fa=self.ctx.framebuffer([self.a]);self.fc=self.ctx.framebuffer([self.b]);self.ff=self.ctx.framebuffer([self.final]);self.resources.extend([self.fb,self.fa,self.fc,self.ff])
 @staticmethod
 def put(p,key,value):
  if key in p:p[key].value=value
 def render(self,d):
  t=time.perf_counter();w,h=d['width'],d['height'];self.resize(w,h);self.fb.use();self.ctx.viewport=(0,0,w,h);self.noise.use(0)
  values={'Resolution':(w,h),'FlowNoise':0,'Time':d['time'],'Power':d['power'],'Speed':d['speed'],'Radius':d['radius'],'Density':d['density'],'Yaw':d['yaw'],'Pitch':d['pitch'],'Distance':d['distance'],'Reveal':d['reveal'],'Palette':d['palette'],'Steps':d['steps']}
  for key,val in values.items():self.put(self.p,key,val)
  self.vaos[self.p].render(vertices=3)
  if d['bloom']:
   for fb,tex,offset in [(self.fa,self.bright,(3/w,0)),(self.fc,self.a,(0,4/h)),(self.fa,self.b,(4/w,0)),(self.fc,self.a,(0,4/h))]:
    fb.use();self.ctx.viewport=(0,0,*self.a.size);tex.use(0);self.put(self.blur,'InputColor',0);self.put(self.blur,'Direction',offset);self.vaos[self.blur].render(vertices=3)
  self.ff.use();self.ctx.viewport=(0,0,w,h);self.main.use(0);self.b.use(1)
  for key,val in {'MainColor':0,'BloomColor':1,'Bloom':float(d['bloom']),'Glow':d['glow']}.items():self.put(self.composite,key,val)
  self.vaos[self.composite].render(vertices=3);self.ctx.finish();pixels=self.ff.read(components=3,alignment=1)
  error=self.ctx.error
  if error!='GL_NO_ERROR':raise RuntimeError(error)
  image=Image.frombytes('RGB',(w,h),pixels).transpose(Image.Transpose.FLIP_TOP_BOTTOM);stream=io.BytesIO();image.save(stream,format='JPEG',quality=89,subsampling=0)
  self.frames+=1;return stream.getvalue(),(time.perf_counter()-t)*1000
 def close(self):
  for r in self.resources:r.release()
  glfw.destroy_window(self.window);glfw.terminate()
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--port',type=int,default=0);a=ap.parse_args();renderer=Renderer()
 class Handler(BaseHTTPRequestHandler):
  def log_message(self,*args):pass
  def end_headers(self):
   origin=self.headers.get('Origin','')
   if origin in ('http://127.0.0.1:18731','http://localhost:18731'):
    self.send_header('Access-Control-Allow-Origin',origin);self.send_header('Vary','Origin');self.send_header('Access-Control-Expose-Headers','X-Frame-Id,X-Render-MS')
   super().end_headers()
  def do_OPTIONS(self):
   if self.headers.get('Origin','') not in ('http://127.0.0.1:18731','http://localhost:18731'):return self.answer(403,b'{}')
   self.send_response(204);self.send_header('Access-Control-Allow-Methods','GET,POST,OPTIONS');self.send_header('Access-Control-Allow-Headers','Content-Type,X-Workbench');self.send_header('Access-Control-Max-Age','600');self.send_header('Content-Length','0');self.end_headers()

  def answer(self,status,body,content='application/json'):
   self.send_response(status);self.send_header('Content-Type',content);self.send_header('Content-Length',str(len(body)));self.send_header('Cache-Control','no-store');self.end_headers();self.wfile.write(body)
  def do_GET(self):
   if self.path!='/status':return self.answer(404,b'{}')
   self.answer(200,json.dumps({'service':'xiantu-spirit-gpu','renderer':renderer.ctx.info['GL_RENDERER'],'frames':renderer.frames,'live':True}).encode())
  def do_POST(self):
   if self.path!='/render':return self.answer(404,b'{}')
   if self.headers.get('X-Workbench')!='local' or self.headers.get('Origin','') not in ('','http://127.0.0.1:18731','http://localhost:18731'):return self.answer(403,b'{}')
   try:
    length=int(self.headers.get('Content-Length',0))
    if not 0<length<=16384:raise ValueError('Request size')
    d=validate(json.loads(self.rfile.read(length)));image,ms=renderer.render(d);self.send_response(200);self.send_header('Content-Type','image/jpeg');self.send_header('Content-Length',str(len(image)));self.send_header('Cache-Control','no-store');self.send_header('X-Frame-Id',str(d['seq']));self.send_header('X-Render-MS',f'{ms:.2f}');self.end_headers();self.wfile.write(image)
   except (ValueError,TypeError,json.JSONDecodeError) as e:self.answer(400,json.dumps({'error':str(e)}).encode())
   except (BrokenPipeError,ConnectionResetError):pass
   except Exception as e:
    traceback.print_exc();self.answer(500,json.dumps({'error':str(e)}).encode())
 server=HTTPServer(('127.0.0.1',a.port),Handler);print(json.dumps({'port':server.server_port,'renderer':renderer.ctx.info['GL_RENDERER']}),flush=True)
 try:server.serve_forever()
 except KeyboardInterrupt:pass
 finally:server.server_close();renderer.close()
if __name__=='__main__':main()
