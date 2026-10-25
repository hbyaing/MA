"""Unified local formation gallery with same-origin, lazy native GPU rendering."""
from pathlib import Path
from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler
from functools import partial
from urllib.parse import urlsplit
import importlib.util,json,sys,os
from concurrent.futures import ThreadPoolExecutor
GPU=ThreadPoolExecutor(max_workers=1,thread_name_prefix="formation_gpu")
ROOT=Path(__file__).resolve().parent
RENDERERS={}
SERVICES={'spirit-gathering':'xiantu-spirit-gpu','protective-array':'xiantu-spirit-gpu','spell-sealing':'xiantu-sealing-gpu','immortal-sword-array':'xiantu-sword-array-gpu','deployment':'xiantu-sword-array-gpu'}

def renderer(name):
    if name in RENDERERS:return RENDERERS[name]
    deps=os.environ.get('XIANTU_GPU_DEPS')
    local=ROOT.parents[3]/'gl-check-deps'
    if deps:sys.path.insert(0,deps)
    elif local.is_dir():sys.path.insert(0,str(local))
    folder='immortal-sword-array' if name=='deployment' else name
    scripts=ROOT/'vfx'/folder/'scripts';sys.path.insert(0,str(scripts))
    file=scripts/('deployment_server.py' if name=='deployment' else 'gpu_server.py')
    spec=importlib.util.spec_from_file_location('formation_gpu_'+name.replace('-','_'),file)
    module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
    instance=module.Renderer();RENDERERS[name]=(module,instance)
    return module,instance

def render_frame(name,data):
    module,r=renderer(name);module.glfw.make_context_current(r.window)
    data=module.validate(data);image,ms=r.render(data)
    return image,ms,data['seq']

class Handler(SimpleHTTPRequestHandler):
    def log_message(self,*args):pass
    def end_headers(self):
        self.send_header('Cache-Control','no-store');super().end_headers()
    def answer(self,status,data,kind='application/json',extra=None):
        if not isinstance(data,bytes):data=json.dumps(data).encode()
        self.send_response(status);self.send_header('Content-Type',kind);self.send_header('Content-Length',str(len(data)))
        for k,v in (extra or {}).items():self.send_header(k,str(v))
        self.end_headers();self.wfile.write(data)
    def trusted(self):
        return self.headers.get('Host')==f'127.0.0.1:{self.server.server_port}' and self.headers.get('Origin','') in ('',f'http://127.0.0.1:{self.server.server_port}')
    def do_GET(self):
        if not self.trusted():return self.answer(403,{'error':'Local origin required'})
        parts=urlsplit(self.path).path.strip('/').split('/')
        if len(parts)==3 and parts[0]=='gpu':
            if parts[1] not in SERVICES or parts[2]!='status':return self.answer(404,{})
            return self.answer(200,{'service':SERVICES[parts[1]],'live':True})
        super().do_GET()
    def do_POST(self):
        if not self.trusted() or self.headers.get('X-Workbench')!='local':return self.answer(403,{})
        parts=urlsplit(self.path).path.strip('/').split('/')
        if len(parts)!=3 or parts[0]!='gpu' or parts[1] not in SERVICES or parts[2]!='render':return self.answer(404,{})
        try:
            n=int(self.headers.get('Content-Length','0'))
            if not 0<n<=16384:raise ValueError('Invalid request size')
            data=json.loads(self.rfile.read(n));image,ms,seq=GPU.submit(render_frame,parts[1],data).result(timeout=30)
            self.answer(200,image,'image/jpeg',{'X-Frame-Id':seq,'X-Render-MS':f'{ms:.2f}'})
        except (ValueError,TypeError) as error:self.answer(400,{'error':str(error)})
        except (BrokenPipeError,ConnectionResetError,ConnectionAbortedError):pass
        except Exception as error:self.answer(500,{'error':str(error)})

def create_server():
    server=ThreadingHTTPServer(('127.0.0.1',int(os.environ.get('XIANTU_FORMATION_PORT','0'))),partial(Handler,directory=str(ROOT.parent)))
    return server,f'http://127.0.0.1:{server.server_port}/formations/index.html'
