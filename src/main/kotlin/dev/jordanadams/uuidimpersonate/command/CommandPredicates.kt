package dev.jordanadams.uuidimpersonate.command

import dev.jordanadams.uuidimpersonate.UUIDImpersonate.getImpersonatorProfileFromPlayer
import net.minecraft.server.command.ServerCommandSource
import java.util.function.Predicate

class CommandPredicates {
  companion object {
    val IS_PLAYER = Predicate<ServerCommandSource> { source -> source.isExecutedByPlayer }
    val IS_IMPERSONATED: Predicate<ServerCommandSource> = IS_PLAYER.and(Predicate<ServerCommandSource> { source ->
      getImpersonatorProfileFromPlayer(source.player!!) !== null })
    val IS_OWNER_PERMISSION_LEVEL = Predicate<ServerCommandSource> { source -> source.hasPermissionLevel(4) }
  }
}