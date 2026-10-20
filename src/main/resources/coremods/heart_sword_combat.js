function initializeCoreMod() {
    var O=Java.type('org.objectweb.asm.Opcodes');
    var Var=Java.type('org.objectweb.asm.tree.VarInsnNode');
    var Call=Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var helper='cn/blockforge/generated/generatedmod/combat/HeartSwordCombatProfile';
    function absorption(node) {
        var reads=0,writes=0;
        for(var m=0;m<node.methods.size();m++) {
            var method=node.methods.get(m);
            if(method.desc!='(Lnet/minecraft/world/damagesource/DamageSource;F)V'
                    ||(method.name!='actuallyHurt'&&method.name!='m_6475_'))continue;
            var instructions=method.instructions.toArray();
            for(var i=0;i<instructions.length;i++) {
                var call=instructions[i];
                if(call.getOpcode()!=O.INVOKEVIRTUAL)continue;
                var read=call.desc=='()F'&&(call.name=='getAbsorptionAmount'||call.name=='m_6103_');
                var write=call.desc=='(F)V'&&(call.name=='setAbsorptionAmount'||call.name=='m_7911_');
                if(!read&&!write)continue;
                method.instructions.insertBefore(call,new Var(O.ALOAD,1));
                method.instructions.set(call,new Call(O.INVOKESTATIC,helper,read?'absorption':'setAbsorption',
                        read?'(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)F'
                            :'(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/damagesource/DamageSource;)V',false));
                if(read)reads++;else writes++;
            }
            method.maxStack++;
        }
        if(reads<2||writes<1)throw new Error('Heart true absorption hooks missing in '+node.name+': '+reads+'/'+writes);
        return node;
    }
    return {
        'heart_melee_and_player_absorption': {
            target:{type:'CLASS',name:'net.minecraft.world.entity.player.Player'},
            transformer:function(node) {
                var attributes=0,hits=0,sweeps=0;
                for(var m=0;m<node.methods.size();m++) {
                    var method=node.methods.get(m);
                    if(method.desc!='(Lnet/minecraft/world/entity/Entity;)V'
                            ||(method.name!='attack'&&method.name!='m_5706_'))continue;
                    var instructions=method.instructions.toArray();
                    for(var i=0;i<instructions.length;i++) {
                        var call=instructions[i];
                        if(call.getOpcode()!=O.INVOKEVIRTUAL)continue;
                        if(call.desc=='(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D'
                                &&(call.name=='getAttributeValue'||call.name=='m_21133_')) {
                            method.instructions.set(call,new Call(O.INVOKESTATIC,helper,'attackAttribute',
                                    '(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/ai/attributes/Attribute;)D',false));
                            attributes++;
                        }
                        if((call.owner=='net/minecraft/world/entity/Entity'||call.owner=='net/minecraft/world/entity/LivingEntity')
                                &&call.desc=='(Lnet/minecraft/world/damagesource/DamageSource;F)Z'
                                &&(call.name=='hurt'||call.name=='m_6469_')) {
                            method.instructions.insertBefore(call,new Var(O.ALOAD,0));
                            method.instructions.set(call,new Call(O.INVOKESTATIC,helper,'meleeHit',
                                    '(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;FLnet/minecraft/world/entity/player/Player;)Z',false));
                            if(call.owner=='net/minecraft/world/entity/Entity')hits++;else sweeps++;
                        }
                    }
                    method.maxStack++;
                }
                if(attributes<1||hits!=1||sweeps!=1)throw new Error('Heart sword expected one primary and one sweep site: '+attributes+'/'+hits+'/'+sweeps);
                return absorption(node);
            }
        },
        'heart_living_absorption': {
            target:{type:'CLASS',name:'net.minecraft.world.entity.LivingEntity'},transformer:absorption
        }
    };
}
