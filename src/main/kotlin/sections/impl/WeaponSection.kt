package sections.impl

import Classes
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodInsnNode
import readers.Reader
import readers.Weapon
import sections.Section
import utils.ASMUtils
import java.util.jar.JarFile
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.kotlinProperty

class WeaponSection(val name: String, val weapon: Weapon): Section(
    name,
    4) {

    override fun tryRender(jarFile: JarFile, classes: Classes, appendFunction: (data: String) -> Unit) {

        val properties = HashMap<String, String>()

        for (declaredField in weapon.javaClass.declaredFields) {
            declaredField.isAccessible = true
            val result =
                Reader.convertToString(declaredField.kotlinProperty as KProperty1<Any, *>, declaredField[weapon])
            properties[result.first] = result.second
        }

        appendFunction("| Key | Value |\n")
        appendFunction("| ---- | ---- |\n")
        appendFunction("| Class Name | [`com.vicmatskiv.mw.items.guns.${name}Factory`](readme-assets/com/vicmatskiv/mw/items/guns/${name}Factory.class) |\n")

        for (property in properties) {
            appendFunction("| " + property.key + " | " + property.value + " |\n")
        }

    }

}