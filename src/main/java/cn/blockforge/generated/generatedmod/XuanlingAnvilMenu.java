package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.balance.XuanlingAnvilBalance;
import cn.blockforge.generated.generatedmod.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;

/** Reuses vanilla recipes, rename packets, repair penalties and the 40-level limit. */
public final class XuanlingAnvilMenu extends AnvilMenu {
    private final DataSlot wear = DataSlot.standalone();
    private final DataSlot unitCost = DataSlot.standalone();
    private final DataSlot unitCostHigh = DataSlot.standalone();
    // DataSlot is transmitted as a signed short; a bounded library index also
    // avoids depending on registry IDs fitting in that range in large modpacks.
    private final DataSlot selectedTemplateIndex=DataSlot.standalone();
    private int rememberedTemplateIndex=1;
    private java.util.List<ItemStack> templates=java.util.List.of();
    public XuanlingAnvilMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(id, inventory, access);
        setUnitCost();
        addDataSlot(wear);
        addDataSlot(unitCost);
        addDataSlot(unitCostHigh);
        addDataSlot(selectedTemplateIndex);
        templates=access.evaluate((level,pos)->level instanceof net.minecraft.server.level.ServerLevel server?AnvilTemplateMemory.get(server).at(pos):java.util.List.<ItemStack>of(),java.util.List.of());
        for(int i=3;i<slots.size();i++){var original=slots.get(i);var moved=new net.minecraft.world.inventory.Slot(original.container,original.getContainerSlot(),original.x,original.y+40);moved.index=i;slots.set(i,moved);}
    }
    public static XuanlingAnvilMenu fromNetwork(int id, Inventory inventory, FriendlyByteBuf buffer) {
        buffer.readBlockPos();
        var menu=new XuanlingAnvilMenu(id, inventory, ContainerLevelAccess.NULL);
        if(buffer.readableBytes()>0){int count=buffer.readVarInt();if(count<0||count>64)throw new IllegalArgumentException("Invalid anvil template library size");var list=new java.util.ArrayList<ItemStack>();for(int i=0;i<count;i++)list.add(buffer.readItem());menu.templates=list;}
        return menu;
    }
    public ComponentTemplate selectedTemplate(){int index=selectedTemplateIndex==null?0:selectedTemplateIndex.get();return new ComponentTemplate(index<=0||index>templates.size()?ItemStack.EMPTY:templates.get(index-1).copy());}
    public record ComponentTemplate(ItemStack stack){}
    public java.util.List<ItemStack> recordedTemplates(){return java.util.Collections.unmodifiableList(templates);}
    @Override public boolean clickMenuButton(Player player,int button){
        if(!stillValid(player)||button<0||button>65)return false;
        if(button==0)selectedTemplateIndex.set(0);
        else if(button>=2){
            int index=button-2;if(index<0||index>=templates.size())return false;
            rememberedTemplateIndex=index+1;selectedTemplateIndex.set(rememberedTemplateIndex);
        }else{
            if(templates.isEmpty())return false;
            rememberedTemplateIndex=Math.max(1,Math.min(rememberedTemplateIndex,templates.size()));
            selectedTemplateIndex.set(rememberedTemplateIndex);
        }
        createResult();broadcastChanges();return true;
    }
    @Override public void createResult(){
        if(selectedTemplateIndex==null||selectedTemplateIndex.get()==0){super.createResult();return;}
        var inputs=new net.minecraft.world.SimpleContainer(selectedTemplate().stack(),inputSlots.getItem(0),inputSlots.getItem(1));
        ItemStack output=ItemStack.EMPTY;
        for(var recipe:player.level().getRecipeManager().getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMITHING)){
            if(recipe.matches(inputs,player.level())){output=recipe.assemble(inputs,player.level().registryAccess());break;}
        }
        repairItemCountCost=1;setMaximumCost(output.isEmpty()?0:1);resultSlots.setItem(0,output);broadcastChanges();
    }
    @Override public MenuType<?> getType() { return ModMenus.XUANLING_ANVIL.get(); }
    @Override protected boolean isValidBlock(BlockState state) { return state.is(WuXingBlocks.XUANLING_ANVIL.get()); }
    private void setUnitCost() {
        int value = XuanlingAnvilBalance.manaPerLevel();
        unitCost.set(value & 0xFFFF);
        unitCostHigh.set(value >>> 16);
    }
    public int manaCost() {
        int unit = (unitCost.get() & 0xFFFF) | ((unitCostHigh.get() & 0xFFFF) << 16);
        return (int)Math.min(Integer.MAX_VALUE, (long)Math.max(0, getCost()) * unit);
    }
    public int damage() { return wear.get(); }
    @Override public void broadcastChanges() {
        access.execute((level, pos) -> {
            BlockState state = level.getBlockState(pos);
            wear.set(isValidBlock(state) ? state.getValue(XuanlingAnvilBlock.DAMAGE) : 3);
            setUnitCost();
        });
        super.broadcastChanges();
    }
    @Override protected boolean mayPickup(Player player, boolean hasItem) {
        if (!hasItem || getCost() <= 0 || !stillValid(player)) return false;
        boolean operational = access.evaluate((level, pos) -> {
            BlockState state = level.getBlockState(pos);
            return isValidBlock(state) && state.getValue(XuanlingAnvilBlock.DAMAGE) < 3;
        }, wear.get() < 3);
        if (!operational) return false;
        var data = GeneratedMod.data(player);
        return player.getAbilities().instabuild || data != null && data.wuxingxiantu$getAura() >= manaCost();
    }
    @Override protected void onTake(Player player, ItemStack output) {
        if (player.level().isClientSide) return;
        // Vanilla Slot.mayPickup validates before moving the result, on the same server thread.
        int price = manaCost(), materialCount = repairItemCountCost;
        var data = GeneratedMod.data(player);
        if (!player.getAbilities().instabuild) {
            data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - price);
            GeneratedMod.sync((ServerPlayer)player);
        }
        float breakChance = ForgeHooks.onAnvilRepair(player, output, inputSlots.getItem(0), inputSlots.getItem(1));
        ItemStack remaining = inputSlots.getItem(1).copy();
        if (materialCount > 0 && remaining.getCount() > materialCount) remaining.shrink(materialCount);
        else remaining = ItemStack.EMPTY;
        inputSlots.setItem(0, ItemStack.EMPTY);
        inputSlots.setItem(1, remaining);
        setMaximumCost(0);
        access.execute((level, pos) -> {
            BlockState state = level.getBlockState(pos);
            if (!isValidBlock(state)) return;
            if (!player.getAbilities().instabuild && player.getRandom().nextFloat() < breakChance)
                level.setBlock(pos, state.setValue(XuanlingAnvilBlock.DAMAGE,
                        Math.min(3, state.getValue(XuanlingAnvilBlock.DAMAGE) + 1)), 3);
            level.levelEvent(1030, pos, 0);
        });
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index == getResultSlot() && !mayPickup(player, getSlot(index).hasItem())) return ItemStack.EMPTY;
        return super.quickMoveStack(player, index);
    }
}
