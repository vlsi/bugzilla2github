package io.github.vlsi.bugzilla.crawler

import jetbrains.exodus.entitystore.Entity
import kotlinx.dnq.XdEntity
import kotlinx.dnq.XdNaturalEntityType
import kotlinx.dnq.java.time.xdLocalDateTimeProp
import kotlinx.dnq.simple.min
import kotlinx.dnq.xdRequiredIntProp

class XdBug(entity: Entity) : XdEntity(entity) {
    companion object : XdNaturalEntityType<XdBug>()

    var bug_id by xdRequiredIntProp(unique = true) { min(0) }

    var changeddate by xdLocalDateTimeProp()

//    val chg by xdStringProp<XdBug>().wrap()

//    var name by xdStringProp()
//    var description by xdStringProp()
//    var priority by xdEnumProp(Priority::class)
//    var status by xdEnumProp(Status::class)
//    var assignee by xdLink1(XdUser)
//    var reporter by xdLink1(XdUser)
//    var comments by xdLink1(XdComment)
//    var attachments by xdLink1(XdAttachment)
//    var history by xdLink1(XdHistory)
}


