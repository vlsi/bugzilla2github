package io.github.vlsi.bugzilla.exposed

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.vendors.MysqlDialect
import org.jetbrains.exposed.sql.vendors.currentDialect

class MediumIntColumnType: ColumnType() {
    override fun sqlType(): String =
        when {
            currentDialect is MysqlDialect -> "MEDIUMINT"
            else -> currentDialect.dataTypeProvider.integerType()
        }

    override fun valueFromDB(value: Any): Int = when (value) {
        is Long -> value.toInt()
        is Number -> value.toInt()
        is String -> value.toInt()
        else -> error("Unexpected value of type 3-byte int: $value of ${value::class.qualifiedName}")
    }
}
