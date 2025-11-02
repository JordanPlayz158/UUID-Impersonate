package dev.jordanadams.uuidimpersonate.mixin;

import com.mojang.authlib.GameProfile;
import dev.jordanadams.uuidimpersonate.HasImpersonator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
  public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
    super(world, pos, yaw, gameProfile);
  }

  @Inject(at = @At("HEAD"), method = "getPlayerListName", cancellable = true)
  private void getServerListName(CallbackInfoReturnable<Text> cir) {
    if (this instanceof HasImpersonator) {
      cir.setReturnValue(getName());
    }
  }
}
