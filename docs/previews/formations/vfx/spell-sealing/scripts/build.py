from pathlib import Path
root=Path(__file__).resolve().parents[1];core=(root/'shader/formation.glsl').read_text(encoding='utf-8-sig')
(root/'shader/formation.vsh').write_text('#version 150\nin vec2 position;out vec2 uv;void main(){uv=position*.5+.5;gl_Position=vec4(position,0,1);}',encoding='utf-8',newline='\n')
(root/'shader/formation.fsh').write_text('#version 150\nin vec2 uv;out vec4 FragColor;out vec4 BrightColor;\n'+core+'\nvoid main(){vec3 c,e;formation(uv,c,e);FragColor=vec4(c,1.);BrightColor=vec4(max(e-vec3(.09),vec3(0.)),1.);}',encoding='utf-8',newline='\n')
print('Generated GLSL150 from formation.glsl')
