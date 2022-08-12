package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.exposed.mediumint
import org.jetbrains.exposed.dao.id.IntIdTable

object ConvBugIssues: IntIdTable("conv_bug_issues") {
    val bug_id = reference("bug_id", Bugs).uniqueIndex()
    val issue_number = mediumint("issue_number").uniqueIndex()
}
