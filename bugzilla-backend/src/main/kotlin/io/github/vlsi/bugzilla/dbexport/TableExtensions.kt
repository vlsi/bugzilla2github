package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.DbEnum
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class InstantColumnType: ColumnType(), IDateColumnType {
    override val hasTimePart: Boolean
        get() = true

    override fun notNullValueToDB(value: Any): Any {
        return when (value) {
            is Instant -> value.toJavaInstant().atOffset(ZoneOffset.UTC).toLocalDate()
            is java.time.Instant -> value.atOffset(ZoneOffset.UTC).toLocalDate()
            is OffsetDateTime -> value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDate()
            else -> throw IllegalArgumentException("Invalid value for InstantColumnType: $value")
        }
    }

    override fun valueFromDB(value: Any): Any {
        return when (value) {
            is LocalDateTime -> value.atOffset(ZoneOffset.UTC).toInstant().toKotlinInstant()
            is java.time.Instant -> value.toKotlinInstant()
            is OffsetDateTime -> value.toInstant().toKotlinInstant()
            else -> throw IllegalArgumentException("Invalid value for InstantColumnType: $value")
        }
    }

    override fun readObject(rs: ResultSet, index: Int): Any? =
        rs.getObject(index, LocalDateTime::class.java)

    override fun sqlType() = currentDialect.dataTypeProvider.dateTimeType()
}

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
