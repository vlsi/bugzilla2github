package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.dto.BugId

class BugLinks(
    duplicates: List<Pair<BugId, BugId>>,
    dependencies: List<Pair<BugId, BugId>>,
) {
    val duplicates = duplicates.sortedBy { it.second.value }.groupBy({ it.first }, { it.second })
    val duplicatedBy = duplicates.sortedBy { it.first.value }.groupBy({ it.second }, { it.first })

    val blockedBy = dependencies.sortedBy { it.second.value }.groupBy({ it.first }, { it.second })
    val blocks = dependencies.sortedBy { it.first.value }.groupBy({ it.second }, { it.first })
}
