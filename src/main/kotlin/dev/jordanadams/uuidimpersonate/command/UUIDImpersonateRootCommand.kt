package dev.jordanadams.uuidimpersonate.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.server.command.ServerCommandSource

class UUIDImpersonateRootCommand : SubCommand {
  companion object {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
      val rootCommandLiteral = UUIDImpersonateRootCommand().toLiteral()

      registerSubCommand(rootCommandLiteral, UUIDImpersonateClearSubCommand())
      registerSubCommand(rootCommandLiteral, UUIDImpersonateImpersonateSubCommand())

      dispatcher.register(rootCommandLiteral)
    }

    private fun registerSubCommand(
      parentLiteral: LiteralArgumentBuilder<ServerCommandSource>,
      subCommand: SubCommand
    ) {
      val subCommandLiteral = subCommand.toArgumentBuilder()

      subCommand.getChildren().forEach { child -> configureSubCommand(subCommandLiteral, child) }

      configureSubCommand(parentLiteral, subCommand, subCommandLiteral)
    }


    private fun configureSubCommand(
      parent: ArgumentBuilder<ServerCommandSource, *>,
      subCommand: SubCommand,
      subCommandLiteral: ArgumentBuilder<ServerCommandSource, *> = subCommand.toArgumentBuilder(),
    ) {
      if (subCommand.getRequires() !== null) subCommandLiteral.requires(subCommand.getRequires())

      if (subCommand.doesExecute()) {
        subCommandLiteral.executes(subCommand)
      }

      parent.then(subCommandLiteral)
    }
  }

  override fun getName(): String {
    return "uuidimpersonate"
  }

  override fun doesExecute(): Boolean {
    return false
  }
}