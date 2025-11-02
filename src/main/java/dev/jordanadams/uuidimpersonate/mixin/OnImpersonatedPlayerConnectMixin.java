package dev.jordanadams.uuidimpersonate.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import dev.jordanadams.uuidimpersonate.GameProfileMinimum;
import dev.jordanadams.uuidimpersonate.HasImpersonator;
import dev.jordanadams.uuidimpersonate.UUIDImpersonate;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class OnImpersonatedPlayerConnectMixin {
  @Unique
  GameProfile originalProfile;

  @Shadow
  GameProfile profile;

  @Shadow
  @Final
  MinecraftServer server;

  @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;"), method = "acceptPlayer")
	private void afterSourceProfileJoinCheck(CallbackInfo ci) {
    UUID impersonatedUuid = UUIDImpersonate.INSTANCE.getImpersonatedUuid(GameProfileMinimum.Companion.from(profile));

    if (impersonatedUuid == null) {
      return;
    }

    String impersonatedName = UUIDImpersonate.INSTANCE.getOrCacheUsernameFromUuid(server, impersonatedUuid);

    if (impersonatedName == null) {
      UUIDImpersonate.LOGGER.warn("Failed to impersonate {} (for {}) as a name could not be found in user cache or retrieved from Mojang's API.", impersonatedUuid, profile.getName());
      return;
    }

    originalProfile = profile;
    profile = new GameProfile(impersonatedUuid, impersonatedName);
  }

  // Can't set the name to include impersonated in GameProfile due to 16 character hard limit so setting it in custom name
  @Inject(at = @At(value = "HEAD"), method = "addToServer")
  private void testPlayerProfileName(CallbackInfo ci, @Local(argsOnly = true) ServerPlayerEntity player) {
    if (originalProfile == null) {
      return;
    }

    if (player instanceof HasImpersonator hasImpersonator) {
      hasImpersonator.uUIDImpersonate$setImpersonator(GameProfileMinimum.Companion.from(originalProfile));
    }
  }
}