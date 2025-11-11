package dev.jordanadams.uuidimpersonate.mixin;

import com.mojang.authlib.GameProfile;
import dev.jordanadams.uuidimpersonate.GameProfileMinimum;
import dev.jordanadams.uuidimpersonate.HasImpersonator;
import dev.jordanadams.uuidimpersonate.ImpersonateData;
import dev.jordanadams.uuidimpersonate.UUIDImpersonate;
import java.util.UUID;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
class OnPlayerCreationMixin {

  @Shadow
  @Final
  private MinecraftServer server;

  @Inject(at = @At("HEAD"), method = "createPlayer", cancellable = true)
  private void createPlayer(GameProfile profile, SyncedClientOptions syncedOptions, CallbackInfoReturnable<ServerPlayerEntity> cir) {
    ImpersonateData impersonateData = UUIDImpersonate.INSTANCE.getImpersonateData(
        GameProfileMinimum.Companion.from(profile));

    if (impersonateData == null) {
      return;
    }

    UUID impersonatedUuid = impersonateData.getUuid();

    String impersonatedName = UUIDImpersonate.INSTANCE.getOrCacheUsernameFromUuid(impersonatedUuid);

    if (impersonatedName == null) {
      UUIDImpersonate.LOGGER.warn("Failed to impersonate {} (for {}) as a name could not be found in user cache or retrieved from Mojang's API.", impersonatedUuid, profile.getName());
      return;
    }

    GameProfile impersonatedProfile = new GameProfile(impersonatedUuid, impersonatedName);

    ServerPlayerEntity player = new ServerPlayerEntity(server, server.getOverworld(), impersonatedProfile, syncedOptions);

    if (player instanceof HasImpersonator hasImpersonator) {
      hasImpersonator.uUIDImpersonate$setImpersonator(GameProfileMinimum.Companion.from(profile));
    }

    cir.setReturnValue(player);
  }
}