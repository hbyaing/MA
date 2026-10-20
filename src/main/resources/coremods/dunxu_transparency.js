function initializeCoreMod() {
    var O=Java.type('org.objectweb.asm.Opcodes');
    var List=Java.type('org.objectweb.asm.tree.InsnList');
    var Var=Java.type('org.objectweb.asm.tree.VarInsnNode');
    var Call=Java.type('org.objectweb.asm.tree.MethodInsnNode');
    return { 'dunxu_player_buffers': {
        target: {type:'CLASS',name:'net.minecraft.client.renderer.entity.player.PlayerRenderer'},
        transformer: function(node){
            var count=0;
            for(var i=0;i<node.methods.size();i++){
                var m=node.methods.get(i);
                if(m.desc!='(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V')continue;
                var hook=new List();
                hook.add(new Var(O.ALOAD,1));hook.add(new Var(O.ALOAD,5));
                hook.add(new Call(O.INVOKESTATIC,'cn/blockforge/generated/generatedmod/client/DunxuTransparency','wrap',
                    '(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/MultiBufferSource;)Lnet/minecraft/client/renderer/MultiBufferSource;',false));
                hook.add(new Var(O.ASTORE,5));m.instructions.insert(hook);m.maxStack+=2;count++;
            }
            if(count!=1)throw new Error('Expected one player render method, found '+count);
            return node;
        }
    }, 'dunxu_first_person_buffers': {
        target: {type:'CLASS',name:'net.minecraft.client.renderer.ItemInHandRenderer'},
        transformer: function(node){
            var count=0;
            for(var i=0;i<node.methods.size();i++){
                var m=node.methods.get(i);
                if(m.desc!='(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V')continue;
                var hook=new List();
                hook.add(new Var(O.ALOAD,1));hook.add(new Var(O.ALOAD,9));
                hook.add(new Call(O.INVOKESTATIC,'cn/blockforge/generated/generatedmod/client/DunxuTransparency','wrap',
                    '(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/MultiBufferSource;)Lnet/minecraft/client/renderer/MultiBufferSource;',false));
                hook.add(new Var(O.ASTORE,9));m.instructions.insert(hook);m.maxStack+=2;count++;
            }
            if(count!=1)throw new Error('Expected one first-person arm/item method, found '+count);
            return node;
        }
    }};
}
