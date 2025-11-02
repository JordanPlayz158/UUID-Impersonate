package dev.jordanadams.uuidimpersonate

import com.mojang.authlib.GameProfile
import java.util.UUID

data class GameProfileMinimum(val id: UUID, val name: String) {
  companion object {
    // For Java calling
    fun from(profile: GameProfile) = profile.toMinimal()
  }

}

fun GameProfile.toMinimal(): GameProfileMinimum {
  return GameProfileMinimum(id, name)
}