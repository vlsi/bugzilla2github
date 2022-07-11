package io.github.vlsi.bugzilla.ui

import RenderedIssue
import io.github.vlsi.bugzilla.dto.BugId
import kotlin.coroutines.CoroutineContext

class BugService(coroutineContext: CoroutineContext) {
    private val transport = Transport(coroutineContext)

    suspend fun renderBugById(id: BugId): RenderedIssue {
        return transport.get("bug", RenderedIssue.serializer(), "id" to id)
    }
}
