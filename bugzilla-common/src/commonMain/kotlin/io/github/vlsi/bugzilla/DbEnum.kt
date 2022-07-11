package io.github.vlsi.bugzilla

interface WithValue<V> {
    val dbValue: V
}

interface DbEnum<T : Enum<T>, V> {
    val values: Map<V, T>
    val enums: Map<T, V>
    operator fun get(name: V) = values.getValue(name)
    operator fun get(enum: T) = enums.getValue(enum)
}

inline fun <reified T, V> dbEnum(): DbEnum<T, V> where T : Enum<T>, T : WithValue<V> =
    object : DbEnum<T, V> {
        override val values = enumValues<T>().associateBy { it.dbValue }
        override val enums = enumValues<T>().associateWith { it.dbValue }
    }
