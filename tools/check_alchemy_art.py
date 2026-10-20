"""Check sprite contracts and the recipe-specific model without running Minecraft."""
import json
from pathlib import Path
from PIL import Image

assets=Path(__file__).resolve().parents[1]/'src/main/resources/assets/wuxingxiantu'
names=['alchemy_'+n for n in ('introduction','nourishing','guarding','body','breakthrough','aptitude','complete')]
names.append('aura_pill_formula')
sizes = {name: 128 for name in names}
# Preserve the previously approved introduction and recipe-specific icon sizes.
sizes.update(alchemy_introduction=64, aura_pill_formula=32)
pixels=set()
for name in names:
    model=json.loads((assets/f'models/item/{name}.json').read_text(encoding='utf-8'))
    assert model['textures']['layer0']==f'wuxingxiantu:item/{name}',name
    with Image.open(assets/f'textures/item/{name}.png') as im:
        assert im.size==(sizes[name],sizes[name]) and im.mode=='RGBA',name
        assert im.getextrema()[3]==(0,255),name
        alpha = im.getchannel('A')
        w, h = im.size
        assert all(alpha.getpixel((x, y)) == 0 for x in range(w) for y in (0, h-1)), name
        assert all(alpha.getpixel((x, y)) == 0 for y in range(h) for x in (0, w-1)), name
        pixels.add(im.tobytes())
assert len(pixels)==8,'Sprites must differ'
formula=json.loads((assets/'models/item/alchemy_formula.json').read_text(encoding='utf-8'))
assert formula['textures']['layer0']=='minecraft:item/paper'
assert formula['overrides']==[{'predicate':{'wuxingxiantu:aura_formula':1},'model':'wuxingxiantu:item/aura_pill_formula'}]
print('PASS: 8 distinct RGBA sprites with explicit 32/64/128 sizes and transparent borders; model links and recipe override preserved')
