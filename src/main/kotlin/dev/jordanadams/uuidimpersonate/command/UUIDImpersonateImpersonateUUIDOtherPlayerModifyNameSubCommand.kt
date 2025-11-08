package dev.jordanadams.uuidimpersonate.command

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.jordanadams.uuidimpersonate.UUIDImpersonate.impersonatePlayer
import dev.jordanadams.uuidimpersonate.command.CommandUtility.Companion.getProfile
import dev.jordanadams.uuidimpersonate.toMinimal
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import java.util.UUID
import java.util.function.Predicate

class UUIDImpersonateImpersonateUUIDOtherPlayerModifyNameSubCommand : SubCommand {
  override fun getArgument(): ArgumentBuilder<ServerCommandSource, *>? {
    return CommandManager.argument("modifyName", BoolArgumentType.bool())

  }

  override fun getRequires(): Predicate<ServerCommandSource> {
    // If console sent the command
    //   The first check is the name set by MinecraftServer#getCommandSource
    //   The second check is just in case someone's IGN can be "Server"
    return Predicate { source -> source.name == "Server" && source.entity == null }
  }

  override fun run(context: CommandContext<ServerCommandSource>): Int {
    val gameProfileArgument = context.getArgument("player", GameProfileArgumentType.GameProfileArgument::class.java)

    val uuid = if (gameProfileArgument !== null) {
      val gameProfiles = gameProfileArgument.getNames(context.source)
      val gameProfile = gameProfiles.first()

      gameProfile.id
    } else {
      context.getArgument("uuid", UUID::class.java)
    }

    val modifyName = context.getArgument("modifyName", Boolean::class.java)

    val sourceProfile = getProfile(context, "sourcePlayer")


    val error = impersonatePlayer(sourceProfile!!.toMinimal(), uuid, modifyName)

    if (error !== null) {
      context.source.sendFeedback({ Text.literal(error) }, false)
      return 0
    }
    return 1
  }
}