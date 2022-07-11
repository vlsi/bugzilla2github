package io.github.vlsi.bugzilla

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.sources.ValueSource
import com.typesafe.config.Config
import com.typesafe.config.ConfigValueType

/**
 * Pass values from application.conf to Click as if they were on the command line.
 */
class ConfigValueSource(
    private val config: Config
) : ValueSource {
    private val optionNamer = ValueSource.getKey()

    companion object {
        val DASH = Regex("-\\w")
    }

    override fun getValues(context: Context, option: Option): List<ValueSource.Invocation> {
        var path = option.valueSourceKey ?: optionNamer(context, option)
        if (path.startsWith("db-")) {
            path = path.replaceFirst("db-", "bugzilla.database.")
        } else if (path.startsWith("github-")) {
            path = path.replaceFirst("github-", "github.")
        } else if (path == "bugzilla-url") {
            path = "bugzilla.url"
        }
        // attachments-repository => attachmentsRepository
        path = path.replace(DASH) { it.value.substring(1).uppercase() }

        return when {
            config.hasPath(path) -> {
                val value = config.getValue(path)
                when(value.valueType()) {
                    ConfigValueType.NULL -> emptyList()
                    else -> ValueSource.Invocation.just(value.unwrapped())
                }
            }
            else -> emptyList()
        }
    }
}
