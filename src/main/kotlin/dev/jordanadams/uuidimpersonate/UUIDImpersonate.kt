package dev.jordanadams.uuidimpersonate

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.context.CommandContext
import dev.jordanadams.uuidimpersonate.command.UUIDImpersonateRootCommand
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
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

  private val playerUuidToUuidToImpersonate = HashMap<GameProfileMinimum, UUID>()

	override fun onInitialize() {
    CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
      UUIDImpersonateRootCommand.register(dispatcher)
    }
	}

  fun impersonatePlayerFromCommand(context: CommandContext<ServerCommandSource>, targetUuid: UUID): Int {
    val source = context.source
    val sourcePlayer = source.playerOrThrow

    if (sourcePlayer.uuid == targetUuid) {
      source.sendFeedback(
        { Text.literal("You cannot impersonate yourself :P") },
        false)
      return 0
    }

    impersonatePlayer(sourcePlayer, targetUuid)
    return 1
  }

  fun impersonatePlayer(player: ServerPlayerEntity, target: UUID) {
    impersonatePlayer(player.gameProfile.toMinimal(), target)

    val impersonatedName = getOrCacheUsernameFromUuid(player.server, target)

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

  fun impersonatePlayer(source: GameProfileMinimum, target: UUID) {
    playerUuidToUuidToImpersonate[source] = target
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

  fun getImpersonatedUuid(source: GameProfileMinimum) = playerUuidToUuidToImpersonate[source]

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