import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import readers.Weapons
import sections.impl.WeaponsSection
import java.io.File
import java.util.jar.JarFile
import kotlin.system.exitProcess

lateinit var weapons: Weapons

var output = ""
val inputFile = File("mw.jar")
val outputFile = File("readme.md")
val writer = outputFile.writer()
val assetsSection = File("readme-assets")

fun main() {


    if (!inputFile.exists()) {
        error("Please put the vic's modern warfare mod jar in the working directory.")
    }
    val input = JarFile(inputFile, false)
    val classes = Classes()

    assetsSection.deleteRecursively()

    for (entry in input.entries()) {
        if (entry.name.endsWith("/")) continue
        File(assetsSection, entry.name).parentFile.mkdirs()
        val outputStream = File(assetsSection, entry.name).outputStream()
        outputStream.write(input.getInputStream(entry).readBytes())
        outputStream.close()

        if (entry.name.endsWith(".class")) {
            val classNode = ClassNode()
            ClassReader(input.getInputStream(entry)).accept(classNode, 0)
            classes[classNode.name] = classNode
            System.err.println("Loaded " + entry.name)
        }
    }

    val sections = arrayListOf(
        WeaponsSection(),
    )

    addOutput("""
# Vic's Modern Warfare Document/Manual
This document is unofficially generated! For more information, please check this [GitHub Repository](https://github.com/fan87/VMW-Document-Generator)

""")

    weapons = Weapons(input, classes)


    for (section in sections) {
        section.render(input, classes) { addOutput(it) }
    }

}

fun addOutput(data: String) {
    output += data
//    print(data)
    writer.write(data)
    writer.flush()
}

typealias Classes = HashMap<String, ClassNode>