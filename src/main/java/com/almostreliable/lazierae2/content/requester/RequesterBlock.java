package com.almostreliable.lazierae2.content.requester;

import com.almostreliable.lazierae2.content.GenericBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class RequesterBlock extends GenericBlock {

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RequesterEntity(pos, state);
    }
}
