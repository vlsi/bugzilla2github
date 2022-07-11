package io.github.vlsi.bugzilla.ui

import io.github.vlsi.bugzilla.dto.MigrateStatusResponse
import kotlinx.coroutines.launch
import react.*
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div

val Migrate by VFC {
    val coroutineScope = useContext(CoroutineScopeContext)
    val migrateService = useMemo { MigrateService(coroutineScope.coroutineContext) }
    var status by useState<MigrateStatusResponse>()

    useEffectOnce {
        coroutineScope.launch {
            status = migrateService.status()
        }

    }
    val mig = status
    when {
        mig == null -> {
            div {
                +"Loading..."
            }
        }
        !mig.isRunning -> {
            div {
                +"No migration in progress"
            }
            button {
                +"Migrate"
                onClick = {
                    coroutineScope.launch {
                        migrateService.migrate()
                    }
                }
            }
        }
        else -> {
            div {
                +"Migration in progress"
            }
        }
    }
}
