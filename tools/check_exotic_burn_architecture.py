"""Structural independence checks only: these do not replace Forge/runtime tests."""
from pathlib import Path
import json
import unittest

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/main/java/cn/blockforge/generated/generatedmod'
RES=ROOT/'src/main/resources'

def read(path):
    return path.read_text(encoding='utf-8-sig')

class ExoticBurnArchitecture(unittest.TestCase):
    def test_no_vanilla_fire_replacement(self):
        core=json.loads(read(RES/'META-INF/coremods.json'))
        self.assertNotIn('coremods/exotic_burn.js',core.values())
        self.assertNotIn('coremods/ignition_source.js',core.values())
        for path in (RES/'coremods').glob('*.js'):
            self.assertNotIn('renderFlame',read(path))
            self.assertNotIn('ScreenEffectRenderer',read(path))

    def test_timer_does_not_use_vanilla_fire(self):
        source=read(JAVA/'fire/ExoticBurnManager.java').split('private static void migrate(')[0]
        for name in ('setSecondsOnFire','setRemainingFireTicks','getRemainingFireTicks','isOnFire','setHealth'):
            self.assertNotIn(name,source)
        self.assertIn('entity.clearFire()',source)
        self.assertIn('entity.setSharedFlagOnFire(false)',source)
        block=read(JAVA/'ExoticFireBlock.java')
        self.assertNotIn('super.entityInside',block)

    def test_renderers_only_use_independent_state(self):
        for name in ('ExoticBurnClientState','ExoticBurnRenderer','ExoticBurnOverlay'):
            source=read(JAVA/f'client/{name}.java')
            for dependency in ('isOnFire','getRemainingFireTicks','ScreenEffectRenderer','renderFlame','getEquippedFire',
                               'minecraft:block/fire','minecraft:block/soul_fire','worldFireSprite'):
                self.assertNotIn(dependency,source)

    def test_world_burn_uses_shared_colored_sprites_without_weapon_palette(self):
        source=read(JAVA/'client/ExoticBurnRenderer.java')
        self.assertIn('sprite(burn.sourceFireId(),false)',source)
        self.assertIn('sprite(burn.sourceFireId(),true)',source)
        self.assertIn('burn.sourceFireId()',source)
        self.assertNotIn('WeaponFlameResources',source)
        self.assertNotIn('ModelBakery.FIRE_',source)
        self.assertIn('layer<32',source)
        self.assertNotIn('PixelFlameMesh',source)
        self.assertIn('renderAtEntity(',source)
        hook=read(RES/'coremods/exotic_burn_anchor.js')
        self.assertIn('EntityRenderDispatcher',hook)
        self.assertIn('method.instructions.insert(call,hook)',hook)
        self.assertIn('hooks!=1',hook)
        self.assertNotIn('instructions.set(',hook)
        self.assertIn('finally {pose.popPose();}',source)
        for obsolete in ('AFTER_PARTICLES','entity.xOld','camera.x','camera.y','camera.z'):
            self.assertNotIn(obsolete,source)

    def test_no_obsolete_calls_in_any_source_set(self):
        for path in (ROOT/'src').rglob('*.java'):
            for obsolete in ('ExoticBurnSourceSync','worldFireSprite','sendExoticBurnEffect','exoticFireContact'):
                self.assertNotIn(obsolete,read(path),str(path))

    def test_damage_types_keep_ordinary_tags_additive(self):
        for name in ('exotic_fire','exotic_fire_contact','exotic_fire_burn'):
            self.assertEqual(json.loads(read(RES/f'data/wuxingxiantu/damage_type/{name}.json'))['scaling'],'never')
        for path in (RES/'data/minecraft/tags/damage_type').glob('*.json'):
            self.assertFalse(json.loads(read(path)).get('replace',False))

    def test_all_existing_flame_sprites_present(self):
        for tier,fire in enumerate(('red_flow','molten_gold','cold_bone','azure_wood','cobalt_sea','violet_thunder','sun_saint','nether_void','nirvana'),1):
            for frame in (0,1):
                sprite=RES/f'assets/wuxingxiantu/textures/block/exotic_fire_{fire}_{frame}.png'
                self.assertTrue(sprite.is_file())
                metadata=json.loads(read(Path(str(sprite)+'.mcmeta')))
                self.assertEqual(metadata['forge']['loader'],f'wuxingxiantu:spirit_fire_{tier}')
                self.assertIn('animation',metadata)

if __name__=='__main__':
    unittest.main(verbosity=2)
