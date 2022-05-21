import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import readers.Weapons
import sections.Section
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

    weapons = Weapons(input, classes)

    addOutput("""
# Vic's Modern Warfare Document/Manual
This document is unofficially generated! For more information, please check this [GitHub Repository](https://github.com/fan87/VMW-Document-Generator)

## Chapters
// INSERT_CHAPTER_TAG
""")

    for (section in sections) {
        section.render(input, classes) { addOutput(it) }
    }



    var inCodeBlock = false


    val namesCount = HashMap<String, Int>().withDefault { -1 }

    var hasFoundChapterTag = false

    var beforeInputBuffer = ""
    var afterInputBuffer = ""
    var chaptersBuffer = ""

    val lines = output.lines()
    for (line in lines.withIndex()) {
        if (line.value.startsWith("```")) {
            inCodeBlock = !inCodeBlock
        }
        if (line.value == "// INSERT_CHAPTER_TAG") {
            hasFoundChapterTag = true
            continue
        }
        if (!hasFoundChapterTag) {
            beforeInputBuffer += line.value + if (line.index != lines.lastIndex) "\n" else ""
        } else {
            afterInputBuffer += line.value + if (line.index != lines.lastIndex) "\n" else ""
        }
        if (!inCodeBlock && line.value.matches(Regex("#{1,} .*"))) {
            val level = line.value.indexOf(" ") - 1
            var originalName = line.value.substring(level + 2)
            var ancherName = originalName
            // Normalize it (if that's how you use this word)
            ancherName = ancherName.lowercase()
            ancherName = ancherName.replace(" ", "-")
            ancherName = ancherName.replace(Regex("[^a-z-]"), "")

            // Process duplicated ancher name
            var count = namesCount.getValue(ancherName) + 1
            namesCount[ancherName] = count

            if (count != 0) {
                ancherName += "-${count}"
            }

            chaptersBuffer += " - ".repeat(level) + "[$originalName](#$ancherName)" + "\n"
        }
    }

    writer.write(beforeInputBuffer + "\n" + chaptersBuffer + "\n" + afterInputBuffer)
    writer.close()

}

fun addOutput(data: String) {
    output += data
//    print(data)
}

typealias Classes = HashMap<String, ClassNode>