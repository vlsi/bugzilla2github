package io.github.vlsi.bugzilla.dbexport

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

open class StringIdTable(name: String = "", columnName: String = "id", length: Int = 50) :
    IdTable<String>(name) {
    final override val id: Column<EntityID<String>> = varchar(columnName, length).entityId()
    final override val primaryKey = PrimaryKey(id)
}
