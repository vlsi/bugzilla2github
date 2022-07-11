package io.github.vlsi.bugzilla.ui

import io.github.vlsi.bugzilla.dto.MigrateStatusResponse
import io.github.vlsi.bugzilla.dto.StartMigrateResponse
import kotlin.coroutines.CoroutineContext

class MigrateService(coroutineContext: CoroutineContext) {
    private val transport = Transport(coroutineContext)

    suspend fun migrate() =
        transport.post("/migration/start", StartMigrateResponse.serializer())

    suspend fun status(): MigrateStatusResponse =
        transport.get("/migration/status", MigrateStatusResponse.serializer())
}
