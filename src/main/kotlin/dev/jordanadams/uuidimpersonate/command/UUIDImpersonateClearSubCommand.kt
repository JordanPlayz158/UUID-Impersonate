package dev.jordanadams.uuidimpersonate.command

import com.mojang.brigadier.context.CommandContext
import dev.jordanadams.uuidimpersonate.UUIDImpersonate
import net.minecraft.server.command.ServerCommandSource
import java.util.function.Predicate

class UUIDImpersonateClearSubCommand : SubCommand {
  override fun run(context: CommandContext<ServerCommandSource>): Int {
    UUIDImpersonate.stopImpersonatePlayer(context.source.player!!)
    return 1
  }

  override fun getName() = "clear"

  override fun getRequires(): Predicate<ServerCommandSource> = CommandPredicates.IS_IMPERSONATED
}