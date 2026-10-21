import fs from 'node:fs/promises';
const endpoint = 'http://localhost:3000/bb-mcp';
let session;
let counter = 0;
export async function rpc(method, params = {}) {
  const headers = {'Content-Type':'application/json', Accept:'application/json, text/event-stream'};
  if (session) headers['mcp-session-id'] = session;
  const response = await fetch(endpoint, {method:'POST', headers, body:JSON.stringify({jsonrpc:'2.0',id:++counter,method,params})});
  const sid = response.headers.get('mcp-session-id');
  if (sid) session = sid;
  const body = await response.text();
  const data = body.startsWith('event:') ? JSON.parse(body.split('\n').find(x=>x.startsWith('data:')).slice(5)) : JSON.parse(body);
  if (data.error) throw Error(JSON.stringify(data.error));
  return data.result;
}
export async function connect() {
  return rpc('initialize',{protocolVersion:'2024-11-05',capabilities:{},clientInfo:{name:'Codex Muyan Model Designer',version:'1.0.0'}});
}
export async function call(name, args = {}) {
  const result = await rpc('tools/call',{name,arguments:args});
  if (result.isError) throw Error(JSON.stringify(result));
  return result;
}
export function getText(result) {
  return result.content.filter(x=>x.type==='text').map(x=>x.text).join('\n');
}
if (process.argv[1].endsWith('mcp.mjs')) {
  await connect();
  const [command, arg, extra] = process.argv.slice(2);
  let result;
  if (command === 'eval') result = await call('risky_eval',{code:await fs.readFile(arg,'utf8')});
  else if (command === 'shot') {
    result = await call(extra === 'app' ? 'capture_app_screenshot' : 'capture_screenshot');
    const pic = result.content.find(x=>x.type==='image');
    if (!pic) throw Error(getText(result));
    await fs.writeFile(arg, Buffer.from(pic.data,'base64'));
    result = {saved:arg};
  } else if (command === 'rpc') result = await rpc(arg, JSON.parse(extra || '{}'));
  else result = await call(command, JSON.parse(arg || '{}'));
  console.log(JSON.stringify(result,(key,value)=>key==='data'&&typeof value==='string'&&value.length>1000?`[${value.length} chars]`:value));
}
