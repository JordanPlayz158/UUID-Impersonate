package dev.jordanadams.uuidimpersonate.command

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.jordanadams.uuidimpersonate.UUIDImpersonate.impersonatePlayerFromCommand
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

class UUIDImpersonateImpersonatePlayerSubCommand : SubCommand {
  override fun getArgument(): ArgumentBuilder<ServerCommandSource, *>? {
    return CommandManager.argument("player", GameProfileArgumentType.gameProfile())
  }

  override fun getChildren(): Array<SubCommand> {
    return arrayOf(UUIDImpersonateImpersonateUUIDOtherPlayerSubCommand())
  }

  override fun run(context: CommandContext<ServerCommandSource>): Int {
    val gameProfileArgument = context.getArgument("player", GameProfileArgumentType.GameProfileArgument::class.java)
    val gameProfile = gameProfileArgument.getNames(context.source).first()

    return impersonatePlayerFromCommand(context, gameProfile.id)
  }
}