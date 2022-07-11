package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.DbEnum
import io.github.vlsi.bugzilla.WithValue
import io.github.vlsi.bugzilla.dbEnum

enum class CommentTypes(override val dbValue: Int): WithValue<Int> {
    NORMAL(0),
    // extra_data == dup_id
    DUPE_OF(1),
    // extra_data == bug_id
    HAS_DUPE(2),
    // extra_data == attachment_id
    ATTACHMENT_CREATED(5),
    // extra_data == attachment_id
    ATTACHMENT_UPDATED(6),
    ;
    companion object: DbEnum<CommentTypes, Int> by dbEnum()
}
