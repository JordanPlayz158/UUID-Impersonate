package dev.jordanadams.uuidimpersonate.command

class UUIDImpersonateRootCommand : SubCommand {
  override fun getName(): String {
    return "uuidimpersonate"
  }

  override fun doesExecute(): Boolean {
    return false
  }

  override fun getChildren(): Array<SubCommand> {
    return arrayOf(UUIDImpersonateImpersonateSubCommand(), UUIDImpersonateClearSubCommand())
  }
}