package dev.jordanadams.uuidimpersonate.command

import net.minecraft.server.command.ServerCommandSource
import java.util.function.Predicate

class UUIDImpersonateImpersonateSubCommand : SubCommand {
  override fun getName(): String {
    return "impersonate"
  }

  override fun getRequires(): Predicate<ServerCommandSource>? {
    return CommandPredicates.IS_IMPERSONATED.negate().and(CommandPredicates.IS_OWNER_PERMISSION_LEVEL)
  }

  override fun getChildren(): Array<SubCommand> {
    return arrayOf(
      UUIDImpersonateImpersonatePlayerSubCommand(),
      UUIDImpersonateImpersonateUUIDSubCommand())
  }

  override fun doesExecute(): Boolean {
    return false
  }
}