package xyz.nucleoid.bedwars.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;

import java.util.Random;

@Mixin(SaplingBlock.class)
public abstract class SaplingBlockMixin {
	@Shadow public abstract void generate(ServerWorld serverWorld, BlockPos blockPos, BlockState blockState, Random random);

	/**
	 * Saplings grow faster in bedwars
	 *
	 * @author SuperCoder79
	 */
	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	public void handleRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		ManagedGameSpace gameSpace = GameSpaceManager.get().byWorld(world);
		if (gameSpace != null && gameSpace.getBehavior().testRule(BedWars.FAST_TREE_GROWTH) == ActionResult.SUCCESS) {
			if (world.getLightLevel(pos.up()) >= 9) {
				this.generate(world, pos, state, random);
			}

			ci.cancel();
		}
	}
}
