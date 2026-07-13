package io.github.zct_studio.better_cushion.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("resource")
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
        if (isCushion(this)) {
            cir.setReturnValue(!still((Entity)(Object)this));
        }
    }


    @Unique
    private boolean isCushion(Object entity) {
        return entity.getClass().getName()
                .equals("com.leclowndu93150.cushionbackport.entity.Cushion");
    }


    @Unique
    private boolean still(Entity cushion) {
        BlockPos pos = cushion.blockPosition();

        BlockState state = cushion.level()
                .getBlockState(pos);

        if (state.is(BlockTags.BEDS)) {
            return true;
        }

        return false;
    }
}
