package cn.blockforge.generated.generatedmod.alchemy;

import cn.blockforge.generated.generatedmod.GeneratedMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side, saved per-player knowledge. Each formula covers every result grade. */
@Mod.EventBusSubscriber(modid = GeneratedMod.MOD_ID)
public final class FormulaKnowledge {
    private static final String KEY = "wuxingxiantu_formulas";
    private static final String UNLOCKED = "alchemyUnlocked";

    public static boolean unlocked(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(UNLOCKED);
    }

    public static boolean unlock(ServerPlayer player) {
        CompoundTag root = saved(player);
        if (root.getBoolean(UNLOCKED)) return false;
        root.putBoolean(UNLOCKED, true);
        return true;
    }

    public static void reset(ServerPlayer player) {
        saved(player).remove(KEY);
        saved(player).remove("alchemyUnderstanding");
        saved(player).putBoolean(UNLOCKED, false);
    }

    public static int learnAll(ServerPlayer player) {
        unlock(player);
        int learned = 0;
        for (PillCatalog pill : PillCatalog.values())
            if (learn(player, new ResourceLocation(GeneratedMod.MOD_ID, pill.id))) learned++;
        return learned;
    }

    private static CompoundTag saved(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(Player.PERSISTED_NBT_TAG)) persistent.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return persistent.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static boolean knows(Player player, ResourceLocation formula) {
        CompoundTag persistent = player.getPersistentData();
        return persistent.getCompound(Player.PERSISTED_NBT_TAG).getCompound(KEY).getBoolean(formula.toString());
    }

    public static boolean learn(ServerPlayer player, ResourceLocation formula) {
        return learn(player,formula,true);
    }
    public static boolean valid(ResourceLocation formula) {
        return formula!=null&&formula.getNamespace().equals(GeneratedMod.MOD_ID)&&PillCatalog.byId(formula.getPath())!=null;
    }
    public static boolean learn(ServerPlayer player, ResourceLocation formula, boolean notify) {
        if (!valid(formula)) return false;
        CompoundTag root = saved(player);
        CompoundTag formulas = root.getCompound(KEY);
        if (formulas.getBoolean(formula.toString())) return false;
        formulas.putBoolean(formula.toString(), true);
        root.put(KEY, formulas);
        if(notify)player.displayClientMessage(Component.translatable("message.wuxingxiantu.formula_learned",
                Component.translatable("item.wuxingxiantu." + formula.getPath())), false);
        return true;
    }

    /** Old stack data migrates once on login, never as a recurring knowledge reward. */
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag root = saved(player);
        int version = root.getInt("alchemyDataVersion");
        if (version >= 3) return;
        if(!root.contains("V5KnowledgeOriginal")){var backup=new CompoundTag();backup.putInt("Version",version);backup.put("Formulas",root.getCompound(KEY).copy());backup.put("Understanding",root.getCompound("alchemyUnderstanding").copy());root.put("V5KnowledgeOriginal",backup);}
        // Existing knowledge remains usable after upgrading, without granting new recipes.
        if (!root.getCompound(KEY).isEmpty()) root.putBoolean(UNLOCKED, true);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var stack = player.getInventory().getItem(slot);
            if (version < 1 && PillData.isLegacy(stack)) player.getInventory().setItem(slot, PillData.normalize(stack));
        }
        if(!root.getCompound(KEY).isEmpty()&&!understands(player,"observation")){var curriculum=cn.blockforge.generated.generatedmod.alchemy.research.ResearchCurriculum.load(player);curriculum.available(player).stream().findFirst().ifPresent(template->study(player,curriculum,template));}
        root.putInt("alchemyDataVersion", 3);
        player.getInventory().setChanged();
    }

    @SubscribeEvent public static void clone(PlayerEvent.Clone event) {
        CompoundTag original = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag target = saved(event.getEntity());
        if (original.contains(KEY)) target.put(KEY, original.getCompound(KEY).copy());
        if(original.contains("V5KnowledgeOriginal"))target.put("V5KnowledgeOriginal",original.getCompound("V5KnowledgeOriginal").copy());
        if(original.contains("alchemyResearchRecords"))target.put("alchemyResearchRecords",original.getList("alchemyResearchRecords",net.minecraft.nbt.Tag.TAG_COMPOUND).copy());
        if(original.contains("alchemyUnderstanding"))target.put("alchemyUnderstanding",original.getCompound("alchemyUnderstanding").copy());
        target.putInt("alchemyDataVersion", original.getInt("alchemyDataVersion"));
        target.putBoolean(UNLOCKED, original.getBoolean(UNLOCKED));
    }

    public static net.minecraft.nbt.ListTag researchRecords(Player p){return saved(p).getList("alchemyResearchRecords",net.minecraft.nbt.Tag.TAG_COMPOUND).copy();}
    /** Same persisted knowledge account. Callers receive a copy; only trusted server operations write it. */
    public static CompoundTag understanding(Player p){return saved(p).getCompound("alchemyUnderstanding").copy();}
    public static boolean understands(Player p,String concept){return understanding(p).getCompound("Concepts").contains(concept);}
    public static boolean studyResearchMethods(ServerPlayer p,String hash,CompoundTag definitions){
        if(!cn.blockforge.generated.generatedmod.alchemy.research.ResearchMethods.qualified(p)||!hash.matches("[a-f0-9]{64}")||definitions.size()>128)return false;
        var state=understanding(p);if(state.getString("ResearchMethodHash").equals(hash))return false;state.putString("ResearchMethodHash",hash);state.put("ResearchMethodDefinitions",definitions.copy());saved(p).put("alchemyUnderstanding",state);return true;
    }
    public static int study(ServerPlayer p,cn.blockforge.generated.generatedmod.alchemy.research.ResearchCurriculum curriculum,
                            cn.blockforge.generated.generatedmod.alchemy.research.ResearchCurriculum.Template template){
        if(!unlocked(p)||!curriculum.templates.contains(template)||!knows(p,ResourceLocation.tryParse(template.formula())))return 0;
        var state=understanding(p);var before=state.getCompound("Concepts");var after=before.copy();int added=0;
        int practice=cn.blockforge.generated.generatedmod.alchemy.research.ResearchPractice.records(p).size();
        // One prerequisite layer per reading; copies/renames/source revisions cannot retrain the same concept.
        for(var node:curriculum.concepts)if(template.concepts().contains(node.id())&&!before.contains(node.id())&&
                node.requires().stream().allMatch(before::contains)&&practice>=node.practice()){
            after.putString(node.id(),node.hash());added++;
        }
        if(added>0){state.put("Concepts",after);saved(p).put("alchemyUnderstanding",state);}return added;
    }
    public static boolean saveTemplate(ServerPlayer p,cn.blockforge.generated.generatedmod.alchemy.research.ResearchCurriculum curriculum,
                                       cn.blockforge.generated.generatedmod.alchemy.research.ResearchCurriculum.Template template){
        if(!unlocked(p)||!curriculum.templates.contains(template)||!knows(p,ResourceLocation.tryParse(template.formula()))||
                !template.concepts().stream().allMatch(id->understands(p,id)))return false;
        var state=understanding(p);var notes=state.getCompound("Templates");if(notes.size()>=32||notes.contains(template.revision()))return false;
        var note=new CompoundTag();note.putString("Id",template.id());note.putString("Revision",template.revision());note.putString("Source",template.formula());
        note.putString("Curriculum",curriculum.hash);note.putString("Status","UNVERIFIED");
        note.putString("Module",template.module());note.putLong("MinimumMilli",template.minimum());note.putInt("MaximumToxicityPpm",template.toxicity());
        var advice=new net.minecraft.nbt.ListTag();template.advice().forEach(s->advice.add(net.minecraft.nbt.StringTag.valueOf(s)));note.put("Advice",advice);
        notes.put(template.revision(),note);state.put("Templates",notes);saved(p).put("alchemyUnderstanding",state);return true;
    }
    public static boolean recordResearch(ServerPlayer p,String fingerprint,String summary){
        if(!unlocked(p)||!fingerprint.matches("[a-f0-9]{64}")||summary.length()>128)return false;
        var records=researchRecords(p);if(records.size()>=64||records.stream().anyMatch(t->((CompoundTag)t).getString("Fingerprint").equals(fingerprint)))return false;
        var record=new CompoundTag();record.putString("Fingerprint",fingerprint);record.putString("Summary",summary);records.add(record);saved(p).put("alchemyResearchRecords",records);return true;
    }
    private FormulaKnowledge() {}
}
