package dev.jordanadams.uuidimpersonate

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.context.CommandContext
import dev.jordanadams.uuidimpersonate.command.CommandUtility
import dev.jordanadams.uuidimpersonate.command.UUIDImpersonateRootCommand
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID

object UUIDImpersonate : ModInitializer {
  const val MOD_ID = "uuid-impersonate"
  @JvmField
  val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

  val HTTP_CLIENT: HttpClient = HttpClient.newHttpClient()
  val GSON: Gson = Gson()

  private val playerUuidToUuidToImpersonate = HashMap<GameProfileMinimum, ImpersonateData>()

  private lateinit var server: MinecraftServer

	override fun onInitialize() {
    ServerLifecycleEvents.SERVER_STARTED.register { server ->
      this.server = server
    }

    CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
      CommandUtility.registerRootCommand(dispatcher, UUIDImpersonateRootCommand())
    }

    ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
      val player = handler.player

      if (player is HasImpersonator && player.`uUIDImpersonate$getImpersonator`() !== null) {
        player.sendMessage(Text.literal(String.format(
          "You are currently impersonating %s. Use `/uuidimpersonate clear` to stop impersonating", player.gameProfile.name)))
      }
    }
	}

  fun impersonatePlayerFromCommand(context: CommandContext<ServerCommandSource>, targetUuid: UUID, modifyName: Boolean = true): Int {
    return impersonatePlayerFromCommand(context, context.source.playerOrThrow, targetUuid, modifyName)
  }

  fun impersonatePlayerFromCommand(context: CommandContext<ServerCommandSource>, player: ServerPlayerEntity, targetUuid: UUID, modifyName: Boolean = true): Int {
    val error = impersonatePlayer(player.gameProfile.toMinimal(), targetUuid, modifyName)
    if (error !== null) {
      context.source.sendFeedback(
        { Text.literal(error) },
        false)
      return 0
    }

    return 1
  }

  /**
   * @return string if error, otherwise null
   */
  fun impersonatePlayer(source: GameProfileMinimum, target: UUID, modifyName: Boolean = true, autoDisconnect: Boolean = true): String? {
    if (source.id == target) {
      return "You cannot impersonate yourself :P"
    }

    playerUuidToUuidToImpersonate[source] = ImpersonateData(target, modifyName)

    if (autoDisconnect) {
      val player = server.playerManager.getPlayer(source.id)

      if (player !== null) {
        val impersonatedName = getOrCacheUsernameFromUuid(target)

        val identifier = if (impersonatedName !== null) {
          impersonatedName
        } else {
          target
        }

        player.networkHandler.disconnect(
          Text.literal(
            "You have been kicked and will be %s the next time you login."
              .format(identifier)))

      }
    }

    return null
  }

  fun stopImpersonatePlayer(player: ServerPlayerEntity) {
    val originalProfile = getImpersonatorProfileFromPlayer(player)

    if (originalProfile === null) {
      return
    }

    stopImpersonatePlayer(originalProfile)
    player.networkHandler.disconnect(
      Text.literal(
        "You have been kicked and will be yourself again the next time you login."))
  }

  fun stopImpersonatePlayer(profile: GameProfileMinimum) {
    playerUuidToUuidToImpersonate.remove(profile)
  }

  fun getImpersonateData(source: GameProfileMinimum) = playerUuidToUuidToImpersonate[source]

  fun getOrCacheUsernameFromUuid(uuid: UUID) = getOrCacheUsernameFromUuid(server, uuid)

  fun getOrCacheUsernameFromUuid(server: MinecraftServer, uuid: UUID): String? {
    val userCache = server.userCache

    if (userCache !== null) {
      val profile = userCache.getByUuid(uuid)

      if (profile.isPresent) {
        return profile.get().name
      }
    }

    val username = getUsernameFromUuid(uuid)

    if (username !== null) {
      server.userCache?.add(GameProfile(uuid, username))
    }

    return username
  }

  fun getUsernameFromUuid(uuid: UUID): String? {
    try {
      val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/$uuid"))
        .timeout(Duration.ofSeconds(15))
        .build()

      val response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString())
      val json = GSON.fromJson(response.body(), JsonObject::class.java)

      return json.get("name").asString
    } catch (_: Throwable) {
      return null
    }
  }

  fun getImpersonatorProfileFromPlayer(player: PlayerEntity): GameProfileMinimum?
    = if (player is HasImpersonator) player.`uUIDImpersonate$getImpersonator`() else null
}