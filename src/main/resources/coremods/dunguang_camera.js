function initializeCoreMod() {
    var O=Java.type('org.objectweb.asm.Opcodes');
    var List=Java.type('org.objectweb.asm.tree.InsnList');
    var Var=Java.type('org.objectweb.asm.tree.VarInsnNode');
    var Call=Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var API=Java.type('net.minecraftforge.coremod.api.ASMAPI');
    return { 'realm_flight_boost_camera': {
        target: {type:'CLASS',name:'net.minecraft.client.Camera'},
        transformer: function(node){
            var count=0;
            var zoom=API.mapMethod('m_90566_');
            for(var i=0;i<node.methods.size();i++){
                var m=node.methods.get(i);
                if(m.desc!='(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V')continue;
                for(var j=0;j<m.instructions.size();j++){
                    var insn=m.instructions.get(j);
                    if(!(insn instanceof Call)||insn.owner!='net/minecraft/client/Camera'||insn.desc!='(D)D'
                            ||(insn.name!=zoom&&insn.name!='getMaxZoom'))continue;
                    // Retain getMaxZoom's receiver and replace only its distance argument.
                    // Both third-person orientations already flow through this one call;
                    // the original eight-corner collision query and move remain intact.
                    var hook=new List();
                    hook.add(new Var(O.ALOAD,0));hook.add(new Var(O.FLOAD,5));
                    hook.add(new Call(O.INVOKESTATIC,
                        'cn/blockforge/generated/generatedmod/client/realmflight/RealmFlightCamera',
                        'requestedDistance','(DLnet/minecraft/client/Camera;F)D',false));
                    m.instructions.insertBefore(insn,hook);m.maxStack+=2;count++;j+=3;
                }
            }
            if(count!=1)throw new Error('Expected one third-person Camera.getMaxZoom call, found '+count);
            return node;
        }
    }};
}
