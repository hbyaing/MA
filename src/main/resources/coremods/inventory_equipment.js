function initializeCoreMod() {
    var O=Java.type('org.objectweb.asm.Opcodes');
    var List=Java.type('org.objectweb.asm.tree.InsnList');
    var Insn=Java.type('org.objectweb.asm.tree.InsnNode');
    var Var=Java.type('org.objectweb.asm.tree.VarInsnNode');
    var Call=Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var Label=Java.type('org.objectweb.asm.tree.LabelNode');
    var Jump=Java.type('org.objectweb.asm.tree.JumpInsnNode');
    var helper='cn/blockforge/generated/generatedmod/robe/EquipmentSlots';
    return {'inventory_equipment':{
        target:{type:'CLASS',name:'net.minecraft.world.inventory.InventoryMenu'},
        transformer:function(node){
            var constructors=0,transfers=0;
            for(var mi=0;mi<node.methods.size();mi++){
                var method=node.methods.get(mi);
                if(method.name=='<init>'&&method.desc=='(Lnet/minecraft/world/entity/player/Inventory;ZLnet/minecraft/world/entity/player/Player;)V'){
                    var instructions=method.instructions.toArray(),add=null,offhand=0;
                    // Vanilla offhand x=77 is three pixels left of the robe/sword column.
                    for(var i=0;i<instructions.length;i++)if(instructions[i].getOpcode()==O.BIPUSH&&instructions[i].operand==77) {
                        instructions[i].operand=80;offhand++;
                    }
                    if(offhand!=1)throw new Error('Inventory equipment: expected one offhand x coordinate');
                    for(var i=0;i<instructions.length;i++)if(instructions[i].getOpcode()==O.INVOKEVIRTUAL
                            &&instructions[i].desc=='(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;')add=instructions[i];
                    if(add==null)throw new Error('Inventory equipment: vanilla addSlot call missing');
                    for(var i=0;i<instructions.length;i++)if(instructions[i].getOpcode()==O.RETURN){
                        var hook=new List();
                        for(var kind=0;kind<2;kind++){
                            hook.add(new Var(O.ALOAD,0));hook.add(new Var(O.ALOAD,1));
                            hook.add(new Call(O.INVOKESTATIC,helper,kind==0?'inventoryRobe':'inventorySword',
                                '(Lnet/minecraft/world/entity/player/Inventory;)Lnet/minecraft/world/inventory/Slot;',false));
                            hook.add(new Call(O.INVOKEVIRTUAL,add.owner,add.name,add.desc,false));hook.add(new Insn(O.POP));
                        }
                        method.instructions.insertBefore(instructions[i],hook);constructors++;
                    }
                    method.maxStack+=2;
                }
                if(method.desc=='(Lnet/minecraft/world/entity/player/Player;I)Lnet/minecraft/world/item/ItemStack;'){
                    var hook=new List(),fallback=new Label();
                    hook.add(new Var(O.ALOAD,0));hook.add(new Var(O.ALOAD,1));hook.add(new Var(O.ILOAD,2));
                    hook.add(new Call(O.INVOKESTATIC,helper,'quickMove',
                        '(Lnet/minecraft/world/inventory/InventoryMenu;Lnet/minecraft/world/entity/player/Player;I)Lnet/minecraft/world/item/ItemStack;',false));
                    hook.add(new Insn(O.DUP));hook.add(new Jump(O.IFNULL,fallback));hook.add(new Insn(O.ARETURN));
                    hook.add(fallback);hook.add(new Insn(O.POP));method.instructions.insert(hook);method.maxStack+=3;transfers++;
                }
            }
            if(constructors!=1||transfers!=1)throw new Error('Inventory equipment: unexpected hook count '+constructors+'/'+transfers);
            return node;
        }
    }};
}
