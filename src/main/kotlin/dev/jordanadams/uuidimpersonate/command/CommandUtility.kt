package dev.jordanadams.uuidimpersonate.command

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.command.ServerCommandSource

class CommandUtility {
  companion object {
    fun registerRootCommand(dispatcher: CommandDispatcher<ServerCommandSource>, rootCommand: SubCommand) {
      val rootCommandLiteral = rootCommand.toLiteral()

      rootCommand.getChildren().forEach { child -> registerSubCommand(rootCommandLiteral, child) }

      dispatcher.register(rootCommandLiteral)
    }

    private fun registerSubCommand(
      parentLiteral: ArgumentBuilder<ServerCommandSource, *>,
      subCommand: SubCommand
    ) {
      val subCommandLiteral = subCommand.toArgumentBuilder()

      subCommand.getChildren().forEach { child -> registerSubCommand(subCommandLiteral, child) }

      configureSubCommand(parentLiteral, subCommand, subCommandLiteral)
    }


    private fun configureSubCommand(
      parent: ArgumentBuilder<ServerCommandSource, *>,
      subCommand: SubCommand,
      subCommandLiteral: ArgumentBuilder<ServerCommandSource, *> = subCommand.toArgumentBuilder(),
    ) {
      subCommandLiteral.requires(subCommand.getRequires())

      if (subCommand.doesExecute()) {
        subCommandLiteral.executes(subCommand)
      }

      parent.then(subCommandLiteral)
    }

    fun getProfile(context: CommandContext<ServerCommandSource>, name: String): GameProfile? {
      val gameProfileArgument = context.getArgument(name, GameProfileArgumentType.GameProfileArgument::class.java)

      if (gameProfileArgument === null) {
        return null
      }

      val gameProfiles = gameProfileArgument.getNames(context.source)
      return gameProfiles.first()
    }
  }
}