package readers

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import utils.ASMUtils
import java.lang.reflect.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToLong
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField


object Reader {

    fun convertToString(field: KProperty1<Any, *>, value: Any?): Pair<String, String> {
        field.isAccessible = true
        if (value == null) {
            return Pair(getPropertyName(field), "Unknown")
        }
        if (field.javaField!!.isAnnotationPresent(TickProperty::class.java)) {
            if (value !is Long) {
                return Pair(getPropertyName(field), "Error: Incorrect field type")
            }
            return Pair(getPropertyName(field), "$value ticks (${(value / 2.0).roundToLong() / 10.0} second)")

        }
        if (field.javaField!!.isAnnotationPresent(FieldNameProperty::class.java)) {
            if (value !is String) {
                return Pair(getPropertyName(field), "Error: Incorrect field type")
            }
            return Pair(getPropertyName(field), "`$value`")

        }
        if (field.javaField!!.isAnnotationPresent(SoundProperty::class.java)) {
            if (value !is String) {
                return Pair(getPropertyName(field), "Error: Incorrect field type")
            }
            return Pair(getPropertyName(field), "[`${value}.ogg`](readme-assets/assets/mw/sounds/${value}.ogg)")

        }
        if (field.javaField!!.isAnnotationPresent(CrosshairProperty::class.java)) {
            if (value !is String) {
                return Pair(getPropertyName(field), "Error: Incorrect field type")
            }
            return Pair(getPropertyName(field), "[`${value}.png`](readme-assets/assets/mw/textures/crosshairs/${value.lowercase(Locale.getDefault())}.png)")

        }
        if (field.javaField!!.isAnnotationPresent(GunProperty::class.java)) {
            if (value !is String) {
                return Pair(getPropertyName(field), "Error: Incorrect field type")
            }
            return Pair(getPropertyName(field), "[`$value`](#$value)")

        }
        if (field.javaField!!.isAnnotationPresent(AmmoProperty::class.java)) {
            if (value !is String) {
                return Pair(getPropertyName(field), "Error: Incorrect field type")
            }
            return Pair(getPropertyName(field), "[`$value`](#$value)")
        }
        if (field.javaField!!.isAnnotationPresent(AttachmentProperty::class.java)) {
            if (value !is List<*>) {
                return Pair(getPropertyName(field), "Error: Incorrect field type")
            }
            return Pair(getPropertyName(field), "${(value as List<String>).joinToString(", ")}")
        }
        if (field.javaField!!.isAnnotationPresent(IngredientProperty::class.java)) {
            if (value !is List<*>) {
                return Pair(getPropertyName(field), "Error: Incorrect field type")
            }
            if (value.isEmpty()) {
                return Pair(getPropertyName(field), "Unknown")
            }
            return Pair(getPropertyName(field), value.joinToString(", "))
        }
        return Pair(getPropertyName(field), value.toString())
    }


    @OptIn(ExperimentalStdlibApi::class)
    fun getPropertyName(field: KProperty1<Any, *>): String {

        if (field.javaField!!.isAnnotationPresent(Property::class.java)) {
            val annotation = field.javaField!!.getAnnotation(Property::class.java)
            return annotation.name
        }
        var outputName = ""
        var wasUpperCase = false
        var first = true
        for (c in field.name) {
            if (first) {
                first = false
                outputName += c.uppercaseChar()
                continue
            }
            if (c.isUpperCase()) {
                if (!wasUpperCase) {
                    wasUpperCase = true
                    outputName += " "
                }
                outputName += c
            } else {
                outputName += c
                wasUpperCase = false
            }
        }
        return outputName
    }



    private fun readTickProperty(buffer: ArrayList<AbstractInsnNode>, currentIndex: Int): Long {
        val node = buffer[currentIndex - 1]
        return ASMUtils.getLong(node)
    }

    private fun readFieldNameProperty(buffer: ArrayList<AbstractInsnNode>, currentIndex: Int): String? {
        val node = buffer[currentIndex - 1]
        if (node is FieldInsnNode) {
            return node.name
        }
        return null
    }
    private fun readSoundProperty(buffer: ArrayList<AbstractInsnNode>, currentIndex: Int): String? {
        val node = buffer[currentIndex - 1]
        return ASMUtils.getString(node)
    }
    private fun readCrosshairProperty(buffer: ArrayList<AbstractInsnNode>, currentIndex: Int): String? {
        val node = buffer[currentIndex - 1]
        if (node is FieldInsnNode) {
            return node.name
        }
        return null
    }
    private fun readGunProperty(buffer: ArrayList<AbstractInsnNode>, currentIndex: Int): String? {
        val node = buffer[currentIndex - 1]
        if (node is FieldInsnNode) {
            return node.name
        }
        return null
    }
    private fun readAttachmentProperty(field: KProperty1<Any, *>, buffer: ArrayList<AbstractInsnNode>, currentIndex: Int, instance: Any): ArrayList<String> {
        val list = (field.get(instance) as ArrayList<String>?)?: arrayListOf()
        var currentIndex = currentIndex
        while (currentIndex > 0) {
            currentIndex--
            var node = buffer[currentIndex]
            if (node is FieldInsnNode) {
                list.add(node.name)
                return list
            }
        }
        return arrayListOf()
    }
    private fun readAmmoProperty(buffer: ArrayList<AbstractInsnNode>, currentIndex: Int): String? {
        val node = buffer[currentIndex - 1]
        if (node is FieldInsnNode) {
            return node.name
        }
        return null
    }
    private fun readIngredients(buffer: ArrayList<AbstractInsnNode>, currentIndex: Int): List<String> {
        val ingredients = ArrayList<String>()
        for (indexedValue in buffer.withIndex().filter { it.index < currentIndex }.reversed()) {
            val insn = indexedValue.value
            if (insn.opcode == Opcodes.ANEWARRAY) {
                break
            }
            if (insn is FieldInsnNode) {
                ingredients.add(ASMUtils.getFieldName(insn))
            }
        }
        return ingredients
    }

    fun read(field: KProperty1<Any, *>, buffer: ArrayList<AbstractInsnNode>, instance: Any, currentIndex: Int): Any? {
        if (field.javaField!!.isAnnotationPresent(TickProperty::class.java)) {
            return readTickProperty(buffer, currentIndex)
        }
        if (field.javaField!!.isAnnotationPresent(FieldNameProperty::class.java)) {
            return readFieldNameProperty(buffer, currentIndex)
        }
        if (field.javaField!!.isAnnotationPresent(SoundProperty::class.java)) {
            return readSoundProperty(buffer, currentIndex)
        }
        if (field.javaField!!.isAnnotationPresent(CrosshairProperty::class.java)) {
            return readCrosshairProperty(buffer, currentIndex)
        }
        if (field.javaField!!.isAnnotationPresent(GunProperty::class.java)) {
            return readGunProperty(buffer, currentIndex)
        }
        if (field.javaField!!.isAnnotationPresent(AmmoProperty::class.java)) {
            return readAmmoProperty(buffer, currentIndex)
        }
        if (field.javaField!!.isAnnotationPresent(AttachmentProperty::class.java)) {
            return readAttachmentProperty(field, buffer, currentIndex, instance)
        }
        if (field.javaField!!.isAnnotationPresent(IngredientProperty::class.java)) {
            return readIngredients(buffer, currentIndex)
        }

        if (field.returnType.classifier == Float::class) {
            return ASMUtils.getFloat(buffer[currentIndex - 1])
        } else if (field.returnType.classifier == Long::class) {
            return ASMUtils.getLong(buffer[currentIndex - 1])
        } else if (field.returnType.classifier == Int::class) {
            return ASMUtils.getInt(buffer[currentIndex - 1])
        } else if (field.returnType.classifier == String::class) {
            return ASMUtils.getString(buffer[currentIndex - 1])
        } else {
            error("Unsupported field type: " + field.returnType)
        }
    }


}


// Declaration
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Property(val name: String)

// Number
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class TickProperty

// String
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class FieldNameProperty
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class SoundProperty

// Enum / Registry
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class GunProperty
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class AttachmentProperty
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class AmmoProperty
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CrosshairProperty

// Misc
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class IngredientProperty