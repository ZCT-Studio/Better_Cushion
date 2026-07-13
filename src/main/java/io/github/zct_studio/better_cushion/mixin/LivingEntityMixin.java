package io.github.zct_studio.better_cushion.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("resource")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @SuppressWarnings("ModifyVariableMayUseName")
    @ModifyVariable(
            method = "causeFallDamage",
            at = @At("HEAD"),
            argsOnly = true
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

        Entity cushion = getCushion(entity);

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
    private boolean isCushion(Object entity) {
        return entity.getClass().getName()
                .equals("com.leclowndu93150.cushionbackport.entity.Cushion");
    }

    @Unique
    private boolean hasCushion(LivingEntity entity) {
        AABB feetBox =
                entity.getBoundingBox()
                        .move(0, -0.2, 0)
                        .inflate(0.1);
        List<Entity> cushions =
                entity.level()
                        .getEntities(
                                entity,
                                feetBox,
                                this::isCushion
                        );

        return !cushions.isEmpty();
    }

    @Unique
    private Entity getCushion(LivingEntity entity) {
        AABB feetBox =
                entity.getBoundingBox()
                        .move(0, -0.2, 0)
                        .inflate(0.1);

        return entity.level()
                .getEntitiesOfClass(
                        Entity.class,
                        feetBox,
                        this::isCushion
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Unique
    private static Method getColorMethod;

    static {
        try {
            Class<?> clazz = Class.forName(
                    "com.leclowndu93150.cushionbackport.entity.Cushion"
            );

            getColorMethod = clazz.getMethod("getColor");

        } catch (Exception ignored) {
        }
    }

    @Unique
    private static SoundEvent getCushionSitSound() {
        try {
            Class<?> clazz = Class.forName(
                    "com.leclowndu93150.cushionbackport.registry.CBSounds"
            );

            Field field = clazz.getField("CUSHION_SIT");

            Supplier<?> supplier = (Supplier<?>) field.get(null);

            return (SoundEvent) supplier.get();

        } catch (Exception e) {
            return null;
        }
    }

    @Unique
    private Block WoolColored(DyeColor color) {
        Block wool = null;
        //? if >=26.2 {
        // wool = Blocks.WOOL.pick(color);
        //? } else {
        /*wool = switch (color) {
            case WHITE -> Blocks.WHITE_WOOL;
            case ORANGE -> Blocks.ORANGE_WOOL;
            case MAGENTA -> Blocks.MAGENTA_WOOL;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_WOOL;
            case YELLOW -> Blocks.YELLOW_WOOL;
            case LIME -> Blocks.LIME_WOOL;
            case PINK -> Blocks.PINK_WOOL;
            case GRAY -> Blocks.GRAY_WOOL;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_WOOL;
            case CYAN -> Blocks.CYAN_WOOL;
            case PURPLE -> Blocks.PURPLE_WOOL;
            case BLUE -> Blocks.BLUE_WOOL;
            case BROWN -> Blocks.BROWN_WOOL;
            case GREEN -> Blocks.GREEN_WOOL;
            case RED -> Blocks.RED_WOOL;
            case BLACK -> Blocks.BLACK_WOOL;
        };
        *///? }

        return wool;
    }

    @Unique
    private void onCushionEffect(
            LivingEntity entity,
            Entity cushion,
            double fallDistance
    ) {

        if (!(entity.level() instanceof ServerLevel level))
            return;

        int count = (int)Math.min(
                fallDistance * 3,
                40
        );

        Block wool = null;

        try {
            wool = WoolColored((DyeColor) getColorMethod.invoke(cushion));
        } catch (IllegalAccessException | InvocationTargetException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        if (wool != null) {
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
        }

        var SoundEvents$CUSHION_SIT = getCushionSitSound();

        if (SoundEvents$CUSHION_SIT != null) {
            level.playSound(
                    null,
                    cushion.blockPosition(),
                    getCushionSitSound(),
                    cushion.getSoundSource(),
                    1.0F,
                    1.0F
            );
        }
    }
}
