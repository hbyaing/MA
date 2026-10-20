"""Focused source contracts for first login and experimental confirmation routing."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/cn/blockforge/generated/generatedmod'

def source(path):
    return (JAVA / path).read_text(encoding='utf-8-sig')

handler = source('client/ExperimentalWarningHandler.java')
assert 'value = Dist.CLIENT' in handler
assert 'ScreenEvent.Opening' in handler
assert 'screen.getClass() == ConfirmScreen.class' in handler
assert 'screen.getClass() == ConfirmExperimentalFeaturesScreen.class' in handler
assert 'client.tell(' in handler and 'callback.accept(true)' in handler
assert 'client.execute(' not in handler, 'Save lock must be released before continuation'
assert 'client.screen == previous' in handler
keys = set(re.findall(r'"(selectWorld\.[^"]+)"', handler))
assert keys == {
    'selectWorld.warning.experimental.title', 'selectWorld.warning.experimental.question',
    'selectWorld.backupQuestion.experimental', 'selectWorld.backupWarning.experimental',
}
for forbidden in ('Lifecycle', 'level.dat', 'PrimaryLevelData', 'BackupConfirmScreen',
                  'java.lang.reflect', 'getDeclaredField', '.onPress(', 'Thread.sleep'):
    assert forbidden not in handler, forbidden
assert '.define("hideExperimentalWarning", true)' in source('HudConfig.java')
for path in JAVA.rglob('*.java'):
    if 'client' not in path.relative_to(JAVA).parts:
        assert 'ExperimentalWarningHandler' not in path.read_text(encoding='utf-8-sig'), path

at = (ROOT / 'src/main/resources/META-INF/accesstransformer.cfg').read_text()
for field in ('ConfirmScreen f_95649_', 'ConfirmScreen f_95651_',
              'ConfirmExperimentalFeaturesScreen f_244340_'):
    assert field in at, field

initialization = source('PlayerInitialization.java')
assert 'if (data.wuxingxiantu$isInitialized()) return;' in initialization
assert 'data.wuxingxiantu$getRootMask() == 0' in initialization
assert 'RootBalance.drawCount(random)' in initialization
assert 'setRootMask(mask)' in initialization
assert 'RootBalance.drawQuality(random)' in initialization
assert 'setInitialized(true)' in initialization
assert 'PlayerInitialization.initializeIfNeeded' in source('event/PlayerLifecycleEvents.java')
assert 'PlayerInitialization.initializeIfNeeded' in source('ModCommands.java')
assert 'setRoot' not in source('network/packet/cultivation/SelectRootPacket.java')
assert not (JAVA / 'client/RootSelectionScreen.java').exists()
assert 'RootSelectionScreen' not in source('client/WuXingClientPackets.java')
assert 'sendOpenRoot' not in source('Network.java')

hud = source('client/WuXingClient.java')
assert '"Lv." + player.experienceLevel, 196, 68, 42, 9, TEXT, 0.5F' in hud
assert 'number(needed), 190, 86, 54, 9, TEXT, 0.5F' in hud
assert '(width - font.width(text) * scale) * alignment' in hud
print('Entry UI contracts passed: weighted random roots, centered labels, client-only precise warning continuation')
