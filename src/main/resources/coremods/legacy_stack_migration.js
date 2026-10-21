function initializeCoreMod() {
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    return {
        'legacy_stack_before_registry_lookup': {
            'target': { 'type': 'CLASS', 'name': 'net.minecraft.world.item.ItemStack' },
            'transformer': function (node) {
                var patched = 0;
                for (var i = 0; i < node.methods.size(); i++) {
                    var method = node.methods.get(i);
                    if (method.name != '<init>' || method.desc != '(Lnet/minecraft/nbt/CompoundTag;)V') continue;
                    var hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            'cn/blockforge/generated/generatedmod/alchemy/LegacyStackMigration', 'migrate',
                            '(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;', false));
                    hook.add(new VarInsnNode(Opcodes.ASTORE, 1));
                    method.instructions.insert(hook);
                    method.maxStack = Math.max(method.maxStack, 1);
                    patched++;
                }
                if (patched != 1) throw new Error('Legacy pill migration: expected one ItemStack NBT constructor, found ' + patched);
                return node;
            }
        }
    };
}
