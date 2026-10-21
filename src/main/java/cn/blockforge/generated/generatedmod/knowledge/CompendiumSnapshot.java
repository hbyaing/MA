package cn.blockforge.generated.generatedmod.knowledge;

import cn.blockforge.generated.generatedmod.*;
import cn.blockforge.generated.generatedmod.alchemy.*;
import cn.blockforge.generated.generatedmod.plants.*;
import cn.blockforge.generated.generatedmod.progression.PlayerCrafts;
import cn.blockforge.generated.generatedmod.registry.ModRecipes;
import cn.blockforge.generated.generatedmod.yujian.SwordTechniqueRegistry;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Read-only view over real target knowledge. Pages never teach formulas or create a parallel unlock system. */
public final class CompendiumSnapshot {
    public static final String READER="MyriadCompendiumReader";
    public static final int MAX_PAGES=100,MAX_PAGE_JSON=8192,MAX_TOTAL_JSON=90000;
    private static final String[] SECTIONS={"herbs","alchemy","refining","arts","treasures","swords"};
    private final List<Component> pages=new ArrayList<>();
    private final Map<String,Integer> starts=new LinkedHashMap<>();
    private CompendiumSnapshot() {pages.add(Component.empty());}
    private static MutableComponent text(String id,Object... args) {return Component.translatable("knowledge.wuxingxiantu."+id,args);}
    private static MutableComponent named(String item) {return Component.translatable("item.wuxingxiantu."+item);}
    private static MutableComponent block(Component title,Component body) {
        return Component.empty().append(title.copy().withStyle(ChatFormatting.BOLD)).append("\n\n").append(body);
    }
    private void section(String id,Component introduction) {
        starts.put(id,pages.size()+1);
        pages.add(block(text("section."+id),introduction));
    }
    private void add(Component page) {if(pages.size()<MAX_PAGES)pages.add(page);}
    public static void refresh(ItemStack book,ServerPlayer reader) {
        var snapshot=new CompendiumSnapshot();
        snapshot.herbs();snapshot.alchemy(reader);snapshot.refining(reader);snapshot.arts(reader);
        snapshot.treasures(reader);snapshot.swords(reader);
        MutableComponent index=text("index_intro").append("\n\n");
        for(String section:SECTIONS)index.append(text("section."+section).withStyle(style->style.withColor(ChatFormatting.DARK_BLUE)
                .withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE,""+snapshot.starts.get(section)))))
                .append("\n");
        snapshot.pages.set(0,block(text("title"),index));
        write(book,snapshot.pages);
        book.getOrCreateTag().putUUID(READER,reader.getUUID());
    }
    public static void clear(ItemStack book) {
        write(book,List.of(block(text("title"),text("refresh_hint"))));
        book.getOrCreateTag().remove(READER);
    }
    private static void write(ItemStack book,List<Component> pages) {
        ListTag encoded=new ListTag();int size=0;
        for(Component page:pages) {
            String json=Component.Serializer.toJson(page);
            if(json.length()>MAX_PAGE_JSON||size+json.length()>MAX_TOTAL_JSON||encoded.size()>=MAX_PAGES)break;
            encoded.add(StringTag.valueOf(json));size+=json.length();
        }
        CompoundTag tag=book.getOrCreateTag();
        // Replace all rendered page data; never resolve a previous reader's signed/filter snapshot.
        tag.put("pages",encoded);tag.remove("filtered_pages");tag.remove("filtered_title");
        tag.putString("title","万界仙典");tag.putString("author","Myriad Ascension");tag.putBoolean("resolved",true);
        tag.putBoolean(MyriadCompendiumItem.MARKER,true);tag.putInt("generation",0);
    }
    private void herbs() {
        section("herbs",text("herbs_intro"));
        List<SpiritPlantDefinition> herbs=SpiritPlantCatalog.ALL.stream().limit(32).toList();
        for(int start=0;start<herbs.size();start+=2) {
            MutableComponent page=Component.empty();
            for(int i=start;i<Math.min(start+2,herbs.size());i++) {
                var herb=herbs.get(i);
                if(i>start)page.append("\n\n");
                page.append(named(herb.id).withStyle(ChatFormatting.BOLD)).append("\n");
                page.append(text("herb_cap",herb.medicinalTierCap)).append("\n");
                MutableComponent properties=Component.empty();
                for(var property:herb.properties) {
                    if(!properties.getSiblings().isEmpty())properties.append("·");
                    properties.append(Component.translatable(property.translationKey()));
                }
                page.append(properties);
            }
            add(page);
        }
    }
    private void alchemy(ServerPlayer player) {
        var account=PlayerCrafts.get(player.server).profession(player.getUUID(),"alchemy");
        section("alchemy",text(FormulaKnowledge.unlocked(player)?"alchemy_intro":"alchemy_locked")
                .append("\n\n").append(profession(account)));
        var recipes=player.server.getRecipeManager().getAllRecipesFor(ModRecipes.ALCHEMY_TYPE.get()).stream()
                .sorted(Comparator.comparing(recipe->recipe.getId().toString())).toList();
        for(PillCatalog pill:PillCatalog.values()) {
            ResourceLocation formula=new ResourceLocation(GeneratedMod.MOD_ID,pill.id);
            if(!FormulaKnowledge.unlocked(player)||!FormulaKnowledge.knows(player,formula)) {
                add(block(named(pill.id),text("locked_formula")));continue;
            }
            var variants=recipes.stream().filter(recipe->recipe.formulaId().equals(formula)).toList();
            MutableComponent body=text("known");
            if(!variants.isEmpty()) {
                AlchemyRecipe recipe=variants.get(0);
                body.append("\n").append(text("formula_gates",recipe.furnaceTier()+1,recipe.minFireTier(),
                        ProgressionGate.realmDisplayName(recipe.minimumRealm()))).append("\n");
                for(int i=0;i<Math.min(3,recipe.getIngredients().size());i++) {
                    var options=recipe.getIngredients().get(i).getItems();
                    if(options.length>0)body.append(options[0].getHoverName()).append(" ×"+recipe.counts().get(i)+"\n");
                }
                body.append(text("formula_variants",variants.size()));
            } else body.append("\n").append(text("no_recipe"));
            add(block(named(pill.id),body));
        }
    }
    private static MutableComponent profession(CompoundTag row) {
        if(!row.getBoolean("Learned"))return text("profession_locked");
        int rank=Math.max(0,Math.min(8,row.getInt("Rank")));
        return text("profession_status",rank+1,Math.max(0,row.getInt("Xp")),row.getCompound("Works").size());
    }
    private void refining(ServerPlayer player) {
        var row=PlayerCrafts.get(player.server).profession(player.getUUID(),"refining");
        section("refining",profession(row));
        if(row.getBoolean("Learned")) {
            int recipes=player.server.getRecipeManager().getAllRecipesFor(ModRecipes.REFINING_TYPE.get()).size();
            add(block(text("section.refining"),text("refining_intro",recipes)));
        }
    }
    private void arts(ServerPlayer player) {
        section("arts",text("arts_intro"));
        var data=GeneratedMod.data(player);
        for(var art:ArtCatalog.ENTRIES) {
            if(SwordTechniqueRegistry.isTechnique(art.id()))continue;
            boolean known=data!=null&&data.wuxingxiantu$hasLearnedArt(art.id());
            add(block(named(art.id()),known?ArtCatalog.function(art.id()):text("locked_art")));
        }
        for(var divine:DivineArtCatalog.ENTRIES) {
            boolean known=data!=null&&data.wuxingxiantu$hasLearnedDivineArt(divine.id());
            add(block(DivineArtCatalog.name(divine.id()),known?text("divine_brief."+divine.id()):text("locked_divine")));
        }
    }
    private void treasures(ServerPlayer player) {
        section("treasures",text("treasures_intro"));
        add(block(named("human_emperor_banner"),ProgressionGate.canUseSoulSystem(player,ProgressionGate.SOUL_BANNER)?
                text("banner_intro"):text("realm_locked",ProgressionGate.realmDisplayName(ProgressionGate.requiredRealmForSoulFeature(ProgressionGate.SOUL_BANNER)))));
        add(block(text("talisman_title"),TalismanKnowledge.has(player)?text("talisman_intro"):text("locked_talisman")));
    }
    private void swords(ServerPlayer player) {
        section("swords",text("swords_intro"));
        var data=GeneratedMod.data(player);
        for(var technique:SwordTechniqueRegistry.all()) {
            boolean known=data!=null&&data.wuxingxiantu$knowsSwordTechnique(technique.id());
            add(block(Component.literal(technique.name()),known?Component.literal(technique.description())
                    .append("\n\n").append(text("sword_limits",technique.requiredTier()+1,technique.requiredStage(),technique.insightCost())):
                    text("locked_sword")));
        }
    }
}
