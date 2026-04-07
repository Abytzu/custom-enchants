package com.customenchants.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ArcaneAnvilBlock extends Block {

    public ArcaneAnvilBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                  PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
            (syncId, inv, p) -> new ArcaneAnvilScreenHandler(syncId, inv,
                ScreenHandlerContext.create(world, pos)),
            Text.translatable("container.customenchants.arcane_anvil")
        ));
        return ActionResult.CONSUME;
    }
}
