package io.github.vlsi.bugzilla.dbexport

import org.jetbrains.exposed.sql.JoinType

object BugzillaQueries {
    fun allBugs(product: String, includeIssueNumber: Boolean = false) =
        Products.join(Bugs, joinType = JoinType.INNER, onColumn = Products.id, otherColumn = Bugs.product_id) {
            Products.name eq product
        }.let {
            if (!includeIssueNumber) {
                it
            } else {
                it.join(
                    ConvBugIssues,
                    joinType = JoinType.INNER,
                    onColumn = Bugs.id,
                    otherColumn = ConvBugIssues.bug_id
                )
            }
        }
}
