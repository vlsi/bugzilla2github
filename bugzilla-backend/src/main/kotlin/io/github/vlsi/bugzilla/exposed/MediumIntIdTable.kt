package io.github.vlsi.bugzilla.exposed

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

open class MediumIntIdTable(name: String = "", columnName: String = "id") : IdTable<Int>(name) {
    final override val id: Column<EntityID<Int>> = mediumint(columnName).autoIncrement().entityId()
    final override val primaryKey = PrimaryKey(id)
}

fun Table.mediumint(name: String): Column<Int> =
    registerColumn(name, MediumIntColumnType())

