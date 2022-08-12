package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.exposed.over
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.LongColumnType

val ROW_NUMBER = CustomFunction<Long>("ROW_NUMBER", LongColumnType()).over()
