package cn.blockforge.generated.generatedmod;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 灵修者职业与灵蕴台工作站点。 */
public final class WuXingProfessions {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, GeneratedMod.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, GeneratedMod.MOD_ID);

    public static final RegistryObject<PoiType> ALCHEMIST_POI = POI_TYPES.register(
            "alchemist_furnace", () -> new PoiType(ImmutableSet.copyOf(
                    WuXingBlocks.ALCHEMY_FURNACE.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final RegistryObject<VillagerProfession> ALCHEMIST = PROFESSIONS.register(
            "alchemist", () -> new VillagerProfession("alchemist",
                    holder -> holder.is(ALCHEMIST_POI.getId()), holder -> holder.is(ALCHEMIST_POI.getId()),
                    ImmutableSet.of(), ImmutableSet.of(), net.minecraft.sounds.SoundEvents.VILLAGER_WORK_CLERIC));

    public static final RegistryObject<PoiType> SPIRIT_POI = POI_TYPES.register(
            "spirit_platform", () -> new PoiType(
                    ImmutableSet.copyOf(WuXingBlocks.SPIRIT_PLATFORM.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final RegistryObject<VillagerProfession> SPIRIT_CULTIVATOR = PROFESSIONS.register(
            "spirit_cultivator", () -> new VillagerProfession(
                    "spirit_cultivator",
                    holder -> holder.is(SPIRIT_POI.getId()),
                    holder -> holder.is(SPIRIT_POI.getId()),
                    ImmutableSet.of(), ImmutableSet.of(), net.minecraft.sounds.SoundEvents.VILLAGER_WORK_CLERIC));
    /** 道士：以绘制台为工作站点，出售符箓相关道具，玩家以灵石交易。 */
    public static final RegistryObject<PoiType> TAOIST_POI = POI_TYPES.register(
            "taoist_platform", () -> new PoiType(
                    ImmutableSet.copyOf(WuXingBlocks.DRAWING_PLATFORM.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final RegistryObject<VillagerProfession> TAOIST = PROFESSIONS.register(
            "taoist", () -> new VillagerProfession(
                    "taoist",
                    holder -> holder.is(TAOIST_POI.getId()),
                    holder -> holder.is(TAOIST_POI.getId()),
                    ImmutableSet.of(), ImmutableSet.of(), net.minecraft.sounds.SoundEvents.VILLAGER_WORK_CLERIC));

    private WuXingProfessions() {}
}
