package io.github.vlsi.bugzilla.exposed

import io.github.vlsi.bugzilla.DbEnum
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi

fun Table.instantAsUtcDateTime(columnName: String): Column<Instant> =
    registerColumn(columnName, InstantColumnType())

@Suppress("UNCHECKED_CAST")
fun <T : Enum<T>, V: Any> Table.dbEnumeration(name: String, enum: DbEnum<T, V>) =
    customEnumeration(name, null, { enum[it as V] }, { enum[it] })

fun Table.bool01(name: String, sql: String? = null): Column<Boolean> =
    registerColumn(name, object: ColumnType() {
        override fun sqlType(): String = sql ?: error("Column $name should exists in database ")

        override fun valueFromDB(value: Any): Boolean = when(value) {
            is String -> value == "1"
            is Int -> value != 0
            else -> error("Invalid value for $name: $value")
        }

        override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
            if (value == null) {
                stmt.setNull(index, IntegerColumnType())
            } else {
                stmt[index] = value
            }
        }

        override fun notNullValueToDB(value: Any): Any = when(value) {
            is Boolean -> if (value) 1 else 0
            else -> error("Invalid value for $name: $value")
        }
    })
