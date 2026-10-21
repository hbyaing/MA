function initializeCoreMod() {
    var O=Java.type('org.objectweb.asm.Opcodes');
    var List=Java.type('org.objectweb.asm.tree.InsnList');
    var Var=Java.type('org.objectweb.asm.tree.VarInsnNode');
    var Call=Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var helper='cn/blockforge/generated/generatedmod/client/weaponflame/WeaponFlameRenderer';
    var render='(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V';
    return {weapon_flame_local_space:{
        target:{type:'CLASS',name:'net.minecraft.client.renderer.entity.ItemRenderer'},
        transformer:function(node) {
            var captures=0,draws=0;
            for(var i=0;i<node.methods.size();i++) {
                var method=node.methods.get(i),nodes=method.instructions.toArray();
                if(method.desc=='(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V') {
                    for(var j=0;j<nodes.length;j++) {
                        var call=nodes[j];
                        if(call.getOpcode()!=O.INVOKEVIRTUAL||call.owner!=node.name||call.desc!=render)continue;
                        method.instructions.insertBefore(call,new Var(O.ALOAD,1));
                        method.instructions.set(call,new Call(O.INVOKESTATIC,helper,'renderHeld',
                            '(Lnet/minecraft/client/renderer/entity/ItemRenderer;'+render.substring(1,render.length-2)+'Lnet/minecraft/world/entity/LivingEntity;)V',false));
                        method.maxStack++;captures++;
                    }
                }
                if(method.desc==render) {
                    var lastPop=null;
                    for(var j=0;j<nodes.length;j++) {
                        var call=nodes[j];
                        if(call.getOpcode()==O.INVOKEVIRTUAL&&call.owner=='com/mojang/blaze3d/vertex/PoseStack'
                                &&(call.name=='popPose'||call.name=='m_85849_')&&call.desc=='()V')lastPop=call;
                    }
                    if(lastPop==null)throw new Error('Missing final item pose pop');
                    var hook=new List();
                    hook.add(new Var(O.ALOAD,1));hook.add(new Var(O.ALOAD,2));hook.add(new Var(O.ALOAD,4));hook.add(new Var(O.ALOAD,5));hook.add(new Var(O.ALOAD,8));
                    hook.add(new Call(O.INVOKESTATIC,helper,'afterItem',
                        '(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/resources/model/BakedModel;)V',false));
                    method.instructions.insertBefore(lastPop,hook);method.maxStack+=5;draws++;
                }
            }
            if(captures!=1||draws!=1)throw new Error('Weapon flame hooks mismatch: '+captures+'/'+draws);
            return node;
        }
    }};
}
