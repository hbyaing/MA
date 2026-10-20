import json, urllib.request, pathlib, sys, base64
BASE = pathlib.Path(__file__).resolve().parent
URL = 'http://localhost:3000/bb-mcp'
SESSION = BASE / 'session.txt'
def request(body, session=None):
    headers={'Content-Type':'application/json','Accept':'application/json, text/event-stream'}
    if session: headers['mcp-session-id']=session
    req=urllib.request.Request(URL,json.dumps(body).encode(),headers)
    with urllib.request.urlopen(req,timeout=90) as r:
        raw=r.read().decode()
        return (json.loads(raw) if raw else {}),r.headers
def connect():
    result,headers=request({'jsonrpc':'2.0','id':1,'method':'initialize','params':{'protocolVersion':'2024-11-05','capabilities':{},'clientInfo':{'name':'Codex-Xuanshui','version':'1.0'}}})
    session=headers['mcp-session-id'];SESSION.write_text(session)
    request({'jsonrpc':'2.0','method':'notifications/initialized'},session)
    return session
def rpc(method,params):
    session=SESSION.read_text().strip() if SESSION.exists() else connect()
    result,_=request({'jsonrpc':'2.0','id':10,'method':method,'params':params},session)
    if 'error' in result: raise RuntimeError(result)
    return result.get('result',result)
def call(name,args={}):
    result=rpc('tools/call',{'name':name,'arguments':args})
    if result.get('isError'): raise RuntimeError(result)
    return result
def evaluate(code):
    result=call('risky_eval',{'code':code})
    texts=[c['text'] for c in result.get('content',[]) if c['type']=='text']
    try:return json.loads(texts[0])
    except:return texts
if __name__=='__main__':
    mode=sys.argv[1]
    if mode=='eval': result=evaluate(sys.argv[2])
    elif mode=='script': result=evaluate(pathlib.Path(sys.argv[2]).read_text(encoding='utf-8'))
    elif mode=='tools': result=rpc('tools/list',{})
    elif mode=='call': result=call(sys.argv[2],json.loads(sys.argv[3]) if len(sys.argv)>3 else {})
    elif mode=='screenshot':
        result=call('capture_app_screenshot',{'max_size':1600,'format':'png'})
        for c in result['content']:
            if c['type']=='image':pathlib.Path(sys.argv[2]).write_bytes(base64.b64decode(c['data']))
        result={'saved':sys.argv[2]}
    print(json.dumps(result,ensure_ascii=False))
