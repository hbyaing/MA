function initializeCoreMod() {
    var O = Java.type('org.objectweb.asm.Opcodes');
    var List = Java.type('org.objectweb.asm.tree.InsnList');
    var Var = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var Insn = Java.type('org.objectweb.asm.tree.InsnNode');
    var Call = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var API = Java.type('net.minecraftforge.coremod.api.ASMAPI');
    return {'jei_search_set_value_bridge': {
        target: {type: 'CLASS', name: 'mezz.jei.gui.input.GuiTextFieldFilter'},
        transformer: function(node) {
            var required = [
                ['setValue', '(Ljava/lang/String;)V'],
                ['setFocused', '(Z)V'],
                ['updateBounds', '(Lmezz/jei/common/util/ImmutableRect2i;)V']
            ];
            var Method = Java.type('org.objectweb.asm.tree.MethodNode');
            for (var r = 0; r < required.length; r++) {
                var publicName = required[r][0], desc = required[r][1], present = false, targetName = null;
                for (var i = 0; i < node.methods.size(); i++) {
                    var candidate = node.methods.get(i);
                    if (candidate.name == publicName && candidate.desc == desc) present = true;
                    if (candidate.name != publicName && candidate.desc == desc && targetName == null) targetName = candidate.name;
                }
                if (present) continue;
                if (targetName == null) throw new Error('JEI search bridge: underlying '+publicName+' method missing');
                var body = new List();
                body.add(new Var(O.ALOAD, 0));
                body.add(new Var(desc == '(Z)V' ? O.ILOAD : O.ALOAD, 1));
                body.add(new Call(O.INVOKEVIRTUAL, 'mezz/jei/gui/input/GuiTextFieldFilter', targetName, desc, false));
                body.add(new Insn(O.RETURN));
                var bridge = new Method(O.ACC_PUBLIC, publicName, desc, null, null);
                bridge.instructions = body;
                bridge.maxStack = 2;
                bridge.maxLocals = 2;
                node.methods.add(bridge);
            }
            return node;
        }
    }};
}
