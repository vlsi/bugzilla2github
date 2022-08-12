package io.github.vlsi.bugzilla.exposed

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.IDateColumnType
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
