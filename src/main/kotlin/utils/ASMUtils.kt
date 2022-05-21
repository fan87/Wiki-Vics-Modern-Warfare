package utils

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import java.util.*

object ASMUtils {

    fun getCrosshair(node: AbstractInsnNode): String {
        return "[`${getString(node)}.png`](readme-assets/assets/mw/textures/crosshairs/${getString(node).lowercase(Locale.getDefault())}.png)"
    }

    fun getSound(node: AbstractInsnNode): String {
        return "[`${getString(node)}.ogg`](readme-assets/assets/mw/sounds/${getString(node)}.ogg)"
    }

    fun getString(node: AbstractInsnNode): String {
        return (node as LdcInsnNode).cst.toString()
    }

    fun getFieldName(node: AbstractInsnNode): String {
        if (node is FieldInsnNode) {
            return node.name
        }
        error("Unable to parse field name of $node")
    }

    fun getFloat(node: AbstractInsnNode): Float {
        if (node is LdcInsnNode && node.cst is Float) {
            return node.cst as Float
        }
        if (node is InsnNode) {
            if (node.opcode >= Opcodes.FCONST_0 && node.opcode <= Opcodes.FCONST_2) {
                return (node.opcode - Opcodes.FCONST_0).toFloat()
            }
        }
        error("Unable to parse float of $node")
    }

    fun getLong(node: AbstractInsnNode): Long {
        if (node is LdcInsnNode && node.cst is Long) {
            return node.cst as Long
        }
        if (node is InsnNode) {
            if (node.opcode >= Opcodes.LCONST_0 && node.opcode <= Opcodes.LCONST_1) {
                return (node.opcode - Opcodes.LCONST_0).toLong()
            }
        }
        error("Unable to parse long of $node")
    }

    fun getInt(node: AbstractInsnNode): Int {
        if (node is IntInsnNode) {
            return node.operand
        }
        if (node is LdcInsnNode && node.cst is Int) {
            return node.cst as Int
        }
        if (node is InsnNode) {
            if (node.opcode >= Opcodes.ICONST_M1 && node.opcode <= Opcodes.ICONST_5) {
                return node.opcode - Opcodes.ICONST_0
            }
        }
        error("Unable to parse int of $node")
    }

}