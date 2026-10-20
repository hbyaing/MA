import pathlib,base64,sys,json
from bb_rpc import call
P=pathlib.Path(__file__).resolve().parent.parent
views={'perspective':([155,92,-150],[0,27,18],.215),'side':([200,40,22],[0,30,22],.215),'front':([0,38,-190],[0,29,-3],.22),'rear':([135,94,175],[0,30,22],.215),'top':([0,220,24],[0,0,24],.215)}
try:call('create_offscreen_view',{'id':'xuanshui_review','width':1400,'height':1000,'copy_view':'none'})
except:pass
for name in sys.argv[1:] or ['perspective','side','front','rear']:
    pos,target,zoom=views[name]
    r=call('set_camera_angle',{'view':'xuanshui_review','position':pos,'target':target,'projection':'orthographic','zoom':zoom,'format':'png'})
    for c in r.get('content',[]):
        if c['type']=='image':(P/(name+'.png')).write_bytes(base64.b64decode(c['data']))
    print(name)
