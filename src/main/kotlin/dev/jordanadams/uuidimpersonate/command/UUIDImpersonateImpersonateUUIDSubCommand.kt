package dev.jordanadams.uuidimpersonate.command

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.jordanadams.uuidimpersonate.UUIDImpersonate.impersonatePlayerFromCommand
import net.minecraft.command.argument.UuidArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import java.util.UUID

class UUIDImpersonateImpersonateUUIDSubCommand : SubCommand {
  override fun getArgument(): ArgumentBuilder<ServerCommandSource, *> {
    return CommandManager.argument("uuid", UuidArgumentType.uuid())
  }

  override fun run(context: CommandContext<ServerCommandSource>): Int {
    val uuid = context.getArgument("uuid", UUID::class.java)

    return impersonatePlayerFromCommand(context, uuid)
  }
}