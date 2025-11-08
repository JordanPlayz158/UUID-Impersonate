package dev.jordanadams.uuidimpersonate.command

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

class UUIDImpersonateImpersonateUUIDOtherPlayerSubCommand : SubCommand {
  override fun getArgument(): ArgumentBuilder<ServerCommandSource, *>? {
    return CommandManager.argument("sourcePlayer", GameProfileArgumentType.gameProfile())
  }

  override fun getChildren(): Array<SubCommand> {
    return arrayOf(UUIDImpersonateImpersonateUUIDOtherPlayerModifyNameSubCommand())
  }

  override fun run(context: CommandContext<ServerCommandSource>): Int {
    val gameProfile = getProfile(context, "player")

    val uuid = if (gameProfile !== null) {
      gameProfile.id
    } else {
      context.getArgument("uuid", UUID::class.java)
    }

    val sourceProfile = getProfile(context, "sourcePlayer")

    val error = impersonatePlayer(sourceProfile!!.toMinimal(), uuid)

    if (error !== null) {
      context.source.sendFeedback({ Text.literal(error) }, false)
      return 0
    }
    return 1
  }
}