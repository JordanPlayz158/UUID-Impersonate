package dev.jordanadams.uuidimpersonate.mixin;

import com.mojang.authlib.GameProfile;
import dev.jordanadams.uuidimpersonate.GameProfileMinimum;
import dev.jordanadams.uuidimpersonate.HasImpersonator;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class OnPlayerEntityGetName extends LivingEntity implements HasImpersonator {

  @Shadow
  public abstract GameProfile getGameProfile();

  protected OnPlayerEntityGetName(EntityType<? extends LivingEntity> entityType, World world) {
    super(entityType, world);
  }

  @Inject(at = @At("HEAD"), method = "getName", cancellable = true)
  private void getName(CallbackInfoReturnable<Text> cir) {
    if (uUIDImpersonate$getImpersonator() == null) {
      return;
    }

    cir.setReturnValue(Text.literal(
        String.format("%s (Impersonated by %s)", getGameProfile().getName(), uUIDImpersonate$getImpersonator().getName())));
  }

  @Unique
  GameProfileMinimum impersonator;

  @Override
  public GameProfileMinimum uUIDImpersonate$getImpersonator() {
    return impersonator;
  }

  @Unique
  public void uUIDImpersonate$setImpersonator(GameProfileMinimum profile) {
    impersonator = profile;
  }
}
