package readers

import Classes
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodInsnNode
import java.util.jar.JarFile
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class Attachments(jarFile: JarFile, classes: Classes) {

    val attachments = HashMap<String, Attachment>()
    val scopes = HashMap<String, Scope>()

    init {
        val attachmentsClass = classes["com/vicmatskiv/mw/Attachments"]!!

        val instructionBuffer = ArrayList<AbstractInsnNode>().apply {
            attachmentsClass.methods.first { it.name == "init" }.instructions
                .filter { it !is LabelNode && it !is LineNumberNode }
                .forEach {
                    add(it)
                }
        }
        var currentScope: Scope? = null
        var currentAttachment: Attachment? = null

        for (instructionWithIndex in instructionBuffer.withIndex()) {
            val instruction = instructionWithIndex.value
            val index = instructionWithIndex.index
            if (instruction is MethodInsnNode && instruction.opcode == Opcodes.INVOKESPECIAL && instruction.name == "<init>" && instruction.owner == "com/vicmatskiv/weaponlib/AttachmentBuilder") {
                currentAttachment = Attachment()
            }
            if (instruction is MethodInsnNode && instruction.opcode == Opcodes.INVOKESPECIAL && instruction.name == "<init>" && instruction.owner == "com/vicmatskiv/weaponlib/ItemScope\$Builder") {
                currentScope = Scope()
            }
            if (currentScope != null) {
                if (instruction is MethodInsnNode && (instruction.owner == "com/vicmatskiv/weaponlib/AttachmentBuilder" || instruction.owner == "com/vicmatskiv/weaponlib/ItemScope\$Builder") && instruction.name.startsWith("with")) {
                    var done = false
                    for (field in Scope::class.memberProperties) {
                        if (field is KMutableProperty1<*, *>) {
                            val field = field as KMutableProperty1<Any, Any>
                            if (instruction.name == "with" + field.name.replaceFirstChar { it.uppercaseChar() }) {
                                val value = Reader.read(field, instructionBuffer, currentScope, index)
                                if (value != null) {
                                    field.isAccessible = true
                                    field.set(currentScope, value)
                                }
                                done = true
                                break
                            }
                        }
                    }
                    if (!done) {
                        System.err.println("[Scope] Could not find field of " + instruction.name)
                    }
                }
                if (instruction is FieldInsnNode && instruction.opcode == Opcodes.PUTSTATIC && instruction.owner == "com/vicmatskiv/mw/Attachments" && instruction.desc == "Lcom/vicmatskiv/weaponlib/ItemAttachment;") {
                    scopes[instruction.name] = currentScope;
                    currentAttachment = null;
                }
            }


            //<editor-fold desc="Attachments">
            if (currentAttachment != null) {
                if (instruction is MethodInsnNode && instruction.owner == "com/vicmatskiv/weaponlib/AttachmentBuilder" && instruction.name.startsWith("with")) {
                    var done = false
                    for (field in Attachment::class.memberProperties) {
                        if (field is KMutableProperty1<*, *>) {
                            val field = field as KMutableProperty1<Any, Any>
                            if (instruction.name == "with" + field.name.replaceFirstChar { it.uppercaseChar() }) {
                                val value = Reader.read(field, instructionBuffer, currentAttachment, index)
                                if (value != null) {
                                    field.isAccessible = true
                                    field.set(currentAttachment, value)
                                }
                                done = true
                                break
                            }
                        }
                    }
                    if (!done) {
                        System.err.println("[Attachments] Could not find field of " + instruction.name)
                    }
                }
                if (instruction is FieldInsnNode && instruction.opcode == Opcodes.PUTSTATIC && instruction.owner == "com/vicmatskiv/mw/Attachments" && instruction.desc == "Lcom/vicmatskiv/weaponlib/ItemAttachment;") {
                    attachments[instruction.name] = currentAttachment;
                    currentAttachment = null;
                }
            }
            //</editor-fold>

        }

    }

}

class Scope {

    ////////// Item Property //////////
    var modId: String? = null
    @Property("Item ID")
    var name: String? = null
    var textureName: String? = null
    var maxStackSize: Int = 1

    @FieldNameProperty var creativeTab: String? = null
    @FieldNameProperty var category: String = "scope"

    @Property("Crafting Materials")
    @IngredientProperty var crafting: List<String> = listOf()
    @CrosshairProperty var crosshair: String? = null

    var zoomRange: FloatRange? = null
    var nightVision: Boolean = false
    var opticalZoom: Boolean = false

}

class Attachment {

    ////////// Item Property //////////
    var modId: String? = null
    @Property("Item ID")
    var name: String? = null
    var textureName: String? = null
    var maxStackSize: Int = 1

    @FieldNameProperty var creativeTab: String? = null
    @FieldNameProperty var category: String? = null

    @Property("Crafting Materials")
    @IngredientProperty var crafting: List<String> = listOf()
    @CrosshairProperty var crosshair: String? = null



}

data class FloatRange(
    val first: Float,
    val second: Float
) {

}