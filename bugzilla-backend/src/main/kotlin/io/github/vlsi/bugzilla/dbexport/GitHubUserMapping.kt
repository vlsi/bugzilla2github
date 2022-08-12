package io.github.vlsi.bugzilla.dbexport

import com.typesafe.config.Config
import com.typesafe.config.ConfigUtil

class GitHubUserMapping(
    configuration: Config
) {
    val mailToLogin = configuration.getConfig("converter-settings.users")
        .entrySet()
        .associate {  ConfigUtil.splitPath(it.key).single() to it.value.unwrapped().toString() }

    fun gitHubLoginOrNull(email: String) = mailToLogin[email]
}
