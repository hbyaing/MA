package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.balance.XuanlingAnvilBalance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

/** Anchored spiritual workstation. Wear survives mining; exhausted anvils can be restored. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="wuxingxiantu")
public final class XuanlingAnvilBlock extends HorizontalDirectionalBlock {
    // Vanilla mining removes the block before playerDestroy asks for its loot.
    // Keep only this tick's removal snapshot so the dropped item receives the library.
    private static final java.util.Map<net.minecraft.server.level.ServerLevel,java.util.Map<Long,net.minecraft.nbt.CompoundTag>> REMOVED=new java.util.IdentityHashMap<>();
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void clearRemovalSnapshots(net.minecraftforge.event.TickEvent.ServerTickEvent event){
        if(event.phase==net.minecraftforge.event.TickEvent.Phase.END)REMOVED.clear();
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGH)
    public static void recordTemplateInteraction(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock e){
        var state=e.getLevel().getBlockState(e.getPos());
        InteractionHand templateHand=templateHand(e.getEntity(),e.getHand());
        if(state.getBlock() instanceof XuanlingAnvilBlock block&&templateHand!=null){
            // Vanilla tries the main hand first: opening a menu there prevents an
            // offhand attempt. Resolve both hands before it consumes the click.
            e.setCancellationResult(block.use(state,e.getLevel(),e.getPos(),e.getEntity(),templateHand,e.getHitVec()));e.setCanceled(true);
        }
    }
    private static InteractionHand templateHand(Player player,InteractionHand requested){
        if(AnvilTemplateMemory.isTemplate(player.level(),player.getItemInHand(requested)))return requested;
        if(AnvilTemplateMemory.isTemplate(player.level(),player.getMainHandItem()))return InteractionHand.MAIN_HAND;
        if(AnvilTemplateMemory.isTemplate(player.level(),player.getOffhandItem()))return InteractionHand.OFF_HAND;
        return null;
    }
    public static final IntegerProperty DAMAGE = IntegerProperty.create("damage", 0, 3);
    // Match the widened model's three base steps. The old middle-step outline
    // let visible outer ledges be clicked through to the block underneath.
    private static final VoxelShape NORTH = Shapes.or(Block.box(.90,0,1.95,15.10,3.12,14.05),
            Block.box(1.54,3.08,2.82,14.46,3.75,13.18),Block.box(2.35,3.67,3.64,13.65,4.50,12.36),
            Block.box(4.75,4.42,4.83,11.25,9.49,11.17),Block.box(3.80,8.84,4.91,12.20,9.37,11.09),
            Block.box(3.00,9.33,4.40,13.00,9.87,11.60),Block.box(2.15,9.82,3.86,13.85,10.36,12.14),
            Block.box(1.39,10.32,3.31,14.61,10.85,12.69),Block.box(.79,10.81,2.72,15.21,11.31,13.28),
            Block.box(0,11.20,1.93,16,16.02,14.07));
    private static final VoxelShape EAST = rotateShape(NORTH);
    private static VoxelShape rotateShape(VoxelShape original){
        VoxelShape[] rotated={Shapes.empty()};
        original.forAllBoxes((x0,y0,z0,x1,y1,z1)->rotated[0]=Shapes.or(rotated[0],Shapes.box(1-z1,y0,x0,1-z0,y1,x1)));
        return rotated[0].optimize();
    }

    public XuanlingAnvilBlock() {
        super(Properties.copy(Blocks.ANVIL).noOcclusion().lightLevel(s -> 6 - s.getValue(DAMAGE)));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DAMAGE, 0));
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(!next.is(this)&&level instanceof net.minecraft.server.level.ServerLevel server){
            var item=new net.minecraft.world.item.ItemStack(asItem());var memory=AnvilTemplateMemory.get(server);memory.writeToItem(pos,item);
            if(item.hasTag())REMOVED.computeIfAbsent(server,ignored->new java.util.HashMap<>()).put(pos.asLong(),item.getTag().copy());
            memory.clear(pos);
        }
        super.onRemove(state,level,pos,next,moving);
    }
    @Override public java.util.List<net.minecraft.world.item.ItemStack> getDrops(BlockState state,net.minecraft.world.level.storage.loot.LootParams.Builder builder){
        var drops=super.getDrops(state,builder);
        var origin=builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN);
        if(origin!=null)for(var stack:drops)if(stack.is(asItem())){
            var pos=BlockPos.containing(origin);AnvilTemplateMemory.get(builder.getLevel()).writeToItem(pos,stack);
            var snapshot=REMOVED.getOrDefault(builder.getLevel(),java.util.Map.of()).get(pos.asLong());
            if(snapshot!=null&&!stack.getOrCreateTag().contains(AnvilTemplateMemory.ITEM_TAG))stack.getOrCreateTag().merge(snapshot.copy());
        }
        return drops;
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,net.minecraft.world.entity.LivingEntity placer,net.minecraft.world.item.ItemStack stack){
        super.setPlacedBy(level,pos,state,placer,stack);
        if(level instanceof net.minecraft.server.level.ServerLevel server)AnvilTemplateMemory.get(server).restoreFromItem(server,pos,stack);
    }
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack, BlockGetter level,
            java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.wuxingxiantu.anvil_costs",
                XuanlingAnvilBalance.manaPerLevel(), XuanlingAnvilBalance.repairCost())
                .withStyle(net.minecraft.ChatFormatting.AQUA));
        tooltip.add(Component.literal("手持模板右键吸收一份；界面重复锻造无需再放模板，挖下后保留。").withStyle(net.minecraft.ChatFormatting.AQUA));
        if(stack.hasTag()&&stack.getTag().contains(AnvilTemplateMemory.ITEM_TAG,9))tooltip.add(Component.literal("已吸收模板："+stack.getTag().getList(AnvilTemplateMemory.ITEM_TAG,10).size()+" 种").withStyle(net.minecraft.ChatFormatting.AQUA));
        if (stack.hasTag() && stack.getTag().contains("BlockStateTag", 10)) {
            String damage = stack.getTag().getCompound("BlockStateTag").getString("damage");
            if (!damage.isEmpty()) tooltip.add(Component.translatable("tooltip.wuxingxiantu.anvil_wear", damage)
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DAMAGE);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(FACING))); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? NORTH : EAST;
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        InteractionHand templateHand=templateHand(player,hand);
        if(templateHand!=null){
            if(!level.isClientSide&&level instanceof net.minecraft.server.level.ServerLevel server){
                boolean added=AnvilTemplateMemory.get(server).learn(pos,player.getItemInHand(templateHand));
                if(added){player.getItemInHand(templateHand).shrink(1);player.getInventory().setChanged();}
                player.displayClientMessage(Component.literal(added?"已吸收一份锻造模板，可在铁砧界面反复使用":"此模板已吸收或模板库已满；未消耗模板"),true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (player.isShiftKeyDown() && !player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.CONSUME;
        if (player.isShiftKeyDown()) {
            int damage = state.getValue(DAMAGE), cost = XuanlingAnvilBalance.repairCost();
            var data = GeneratedMod.data(player);
            if (damage == 0) player.displayClientMessage(Component.translatable("message.wuxingxiantu.anvil_intact"), true);
            else if (!player.getAbilities().instabuild && (data == null || data.wuxingxiantu$getAura() < cost))
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.anvil_mana_missing", cost), true);
            else {
                if (!player.getAbilities().instabuild) {
                    data.wuxingxiantu$setAura(data.wuxingxiantu$getAura() - cost);
                    GeneratedMod.sync(serverPlayer);
                }
                level.setBlock(pos, state.setValue(DAMAGE, damage - 1), 3);
                level.levelEvent(1030, pos, 0);
                player.displayClientMessage(Component.translatable("message.wuxingxiantu.anvil_restored", player.getAbilities().instabuild ? 0 : cost), true);
            }
        } else if (state.getValue(DAMAGE) == 3) {
            player.displayClientMessage(Component.translatable("message.wuxingxiantu.anvil_exhausted", XuanlingAnvilBalance.repairCost()), true);
        } else {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((id, inventory, p) ->
                    new XuanlingAnvilMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
                    Component.translatable("block.wuxingxiantu.xuanling_anvil")),buffer->{
                        buffer.writeBlockPos(pos);var templates=AnvilTemplateMemory.get(serverPlayer.serverLevel()).at(pos);
                        buffer.writeVarInt(templates.size());for(var template:templates)buffer.writeItem(template);
                    });
        }
        return InteractionResult.CONSUME;
    }
}
