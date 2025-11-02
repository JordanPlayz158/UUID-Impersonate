package dev.jordanadams.uuidimpersonate.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import java.util.function.Predicate

interface SubCommand : Command<ServerCommandSource> {
  fun getName(): String? {
    return null
  }
  fun getRequires(): Predicate<ServerCommandSource>? {
    return null
  }

  fun getArgument(): ArgumentBuilder<ServerCommandSource, *>? {
    return null
  }

  fun getChildren(): Array<SubCommand> {
    return emptyArray()
  }

  override fun run(context: CommandContext<ServerCommandSource>): Int {
    return 1
  }

  fun doesExecute(): Boolean {
    return true
  }

  fun toLiteral(): LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal(getName())
  }

  fun toArgumentBuilder(): ArgumentBuilder<ServerCommandSource, *> {
    if (getName() !== null) {
      return toLiteral()
    }

    if (getArgument() !== null) {
      return getArgument()!!
    }

    throw IllegalStateException("A subcommand has been attempted to be registered without a name AND without an argument")
  }
}