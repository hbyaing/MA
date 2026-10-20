// Add only our independent visual at the dispatcher-owned entity origin.
// Vanilla fire tests, fire rendering, timers, shadow and overlay code remain untouched.
function initializeCoreMod() {
    var O=Java.type('org.objectweb.asm.Opcodes');
    var List=Java.type('org.objectweb.asm.tree.InsnList');
    var Var=Java.type('org.objectweb.asm.tree.VarInsnNode');
    var Call=Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var pose='Lcom/mojang/blaze3d/vertex/PoseStack;';
    var buffers='Lnet/minecraft/client/renderer/MultiBufferSource;';
    var entity='Lnet/minecraft/world/entity/Entity;';
    return {exotic_burn_entity_origin:{
        target:{type:'CLASS',name:'net.minecraft.client.renderer.entity.EntityRenderDispatcher'},
        transformer:function(node) {
            var hooks=0;
            for(var i=0;i<node.methods.size();i++) {
                var method=node.methods.get(i);
                if(method.desc!='('+entity+'DDDFF'+pose+buffers+'I)V')continue;
                var nodes=method.instructions.toArray();
                for(var j=0;j<nodes.length;j++) {
                    var call=nodes[j];
                    if(call.getOpcode()!=O.INVOKEVIRTUAL
                            ||call.owner!='net/minecraft/client/renderer/entity/EntityRenderer'
                            ||call.desc!='('+entity+'FF'+pose+buffers+'I)V')continue;
                    var hook=new List();
                    hook.add(new Var(O.ALOAD,1));
                    hook.add(new Var(O.ALOAD,10));
                    hook.add(new Var(O.ALOAD,11));
                    hook.add(new Call(O.INVOKESTATIC,
                            'cn/blockforge/generated/generatedmod/client/ExoticBurnRenderer','renderAtEntity',
                            '('+entity+pose+buffers+')V',false));
                    method.instructions.insert(call,hook);method.maxStack+=3;hooks++;
                }
            }
            if(hooks!=1)throw new Error('Exotic burn entity anchor mismatch: '+hooks);
            return node;
        }
    }};
}
