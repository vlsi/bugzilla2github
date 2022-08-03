package io.github.vlsi.bugzilla.dbexport

import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.LongColumnType

val ROW_NUMBER = CustomFunction<Long>("ROW_NUMBER", LongColumnType()).over()
