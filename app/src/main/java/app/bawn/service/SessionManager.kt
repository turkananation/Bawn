package app.bawn.service

object SessionManager {
    private val unlockedPackages = mutableSetOf<String>()

    fun isUnlocked(packageName: String): Boolean {
        return unlockedPackages.contains(packageName)
    }

    fun notifyUnlock(packageName: String) {
        unlockedPackages.add(packageName)
    }

    fun clearSession() {
        unlockedPackages.clear()
    }
}