package cn.blockforge.generated.generatedmod.combat;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;

/** Marks only paid domain pulses while retaining vanilla magic/lightning mitigation. */
public final class DomainDamageSource extends DamageSource {
    public DomainDamageSource(Player caster,boolean lightning){
        super(caster.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(lightning?DamageTypes.LIGHTNING_BOLT:DamageTypes.MAGIC),caster,caster);
    }
}
