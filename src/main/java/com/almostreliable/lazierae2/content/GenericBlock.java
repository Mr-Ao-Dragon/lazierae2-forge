package com.almostreliable.lazierae2.content;

import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.util.GuiUtil.Tooltip;
import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public abstract class GenericBlock extends Block implements EntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected GenericBlock() {
        super(Properties.of(Material.METAL).strength(3f).sound(SoundType.METAL));
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false).setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var superState = super.getStateForPlacement(context);
        var state = superState == null ? defaultBlockState() : superState;
        return state.setValue(ACTIVE, false).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        var be = level.getBlockEntity(pos);
        if (!level.isClientSide && be instanceof GenericEntity entity) {
            entity.playerDestroy(player.isCreative());
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
        builder.add(FACING);
    }

    @Override
    public void appendHoverText(
        ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag
    ) {
        var description = TextUtil.translateAsString(TRANSLATE_TYPE.TOOLTIP, f("{}.description", getId()));
        if (!description.isEmpty()) {
            tooltip.addAll(Tooltip
                .builder()
                .line(Screen::hasShiftDown, f("{}.description", getId()), ChatFormatting.AQUA)
                .hotkeyHoldAction(() -> !Screen.hasShiftDown(), "key.keyboard.left.shift", "extended_info")
                .build());
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        var entity = level.getBlockEntity(pos);
        if (entity instanceof MenuProvider menuProvider && player instanceof ServerPlayer invoker) {
            NetworkHooks.openGui(invoker, menuProvider, pos);
        }
        return InteractionResult.CONSUME;
    }

    public String getId() {
        var registryName = getRegistryName();
        assert registryName != null;
        return registryName.getPath();
    }
}
