package io.github.zct_studio.better_cushion.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public abstract class EntityMixin {

	@Inject(
			method = "canBeCollidedWith",
			at = @At("HEAD"),
			cancellable = true
	)
	private void canBeCollidedWith$(
			Entity other,
			CallbackInfoReturnable<Boolean> cir
	) {
		if ((Object)this instanceof Cushion cushion) {
			cir.setReturnValue(!still(cushion));
		}
	}

	@SuppressWarnings("RedundantIfStatement")
    @Unique
	private boolean still(Cushion cushion) {
		BlockPos pos = cushion.blockPosition();

		BlockState state = cushion.level()
				.getBlockState(pos);

        if (state.is(BlockTags.BEDS)) {
            return true;
        }

        if (state.is(Blocks.STRAW_BED)) {
            return true;
        }

        return false;
	}
}