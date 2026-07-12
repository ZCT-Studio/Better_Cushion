package io.github.zct_studio.better_cushion.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyVariable(
            method = "causeFallDamage",
            at = @At("HEAD"),
            argsOnly = true,
            name = "damageModifier"
    )
    private float causeFallDamage$_damageModifier(float damageModifier) {
        LivingEntity entity = (LivingEntity)(Object)this;

        if (hasCushion(entity)) {
            return damageModifier * 0.50F; // 百分之50减伤
        }

        return damageModifier;
    }

    @Inject(
            method = "causeFallDamage",
            at = @At("HEAD")
    )
    private void causeFallDamage$(
            double fallDistance,
            float damageModifier,
            DamageSource damageSource,
            CallbackInfoReturnable<Boolean> cir
    ){

        LivingEntity entity =
                (LivingEntity)(Object)this;

        Cushion cushion = getCushion(entity);

        if (!entity.level().isClientSide()
                && cushion != null) {

            onCushionEffect(
                    entity,
                    cushion,
                    fallDistance
            );

            double bounce =
                    Math.min(
                            1.0,
                            0.25 + fallDistance * 0.03
                    );

            entity.setDeltaMovement(
                    entity.getDeltaMovement().x,
                    bounce,
                    entity.getDeltaMovement().z
            );
        }
    }

    @Unique
    private boolean hasCushion(LivingEntity entity) {
        AABB feetBox =
                entity.getBoundingBox()
                        .move(0, -0.2, 0)
                        .inflate(0.1);
        List<Cushion> cushions =
                entity.level()
                        .getEntitiesOfClass(
                                Cushion.class,
                                feetBox
                        );

        return !cushions.isEmpty();
    }

    @Unique
    private Cushion getCushion(LivingEntity entity) {
        AABB feetBox =
                entity.getBoundingBox()
                        .move(0, -0.2, 0)
                        .inflate(0.1);

        return entity.level()
                .getEntitiesOfClass(
                        Cushion.class,
                        feetBox
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Unique
    private void onCushionEffect(
            LivingEntity entity,
            Cushion cushion,
            double fallDistance
    ) {

        if (!(entity.level() instanceof ServerLevel level))
            return;

        int count = (int)Math.min(
                fallDistance * 3,
                40
        );

        Block wool = Blocks.WOOL.pick(cushion.getColor());

        level.sendParticles(
                new BlockParticleOption(
                        ParticleTypes.BLOCK,
                        wool.defaultBlockState()
                ),
                cushion.getX(),
                cushion.getY(0.6),
                cushion.getZ(),
                count,
                0.25,
                0.05,
                0.25,
                0.05
        );

        level.playSound(
                null,
                cushion.blockPosition(),
                SoundEvents.CUSHION_SIT,
                cushion.getSoundSource(),
                1.0F,
                1.0F
        );
    }
}