package io.github.vlsi.bugzilla.ui

import react.FC
import react.Props
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Allows to create a property, that will be passed directly to JS.
 * For instance: [val PluginHook<*>.pluginName: String by DynamicProperty]
 */
inline fun <reified P> dynamicProperty() = object : ReadWriteProperty<Any?, P> {
    @Suppress("UnsafeCastFromDynamic")
    override fun getValue(thisRef: Any?, property: KProperty<*>): P =
        thisRef.asDynamic()[property.name]

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: P) {
        thisRef.asDynamic()[property.name] = value
    }
}

val StringProperty = dynamicProperty<String>()

/**
 * Allows to configure automatic name for [FC] when declaring it via delegation [val ComponentName by FC<...>]
 */
operator fun <P : Props> FC<P>.provideDelegate(thisRef: Nothing?, property: KProperty<*>):
        ReadOnlyProperty<Any?, FC<P>> {
    asDynamic().displayName = property.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    return ReadOnlyProperty { _, _ -> this@provideDelegate }
}
