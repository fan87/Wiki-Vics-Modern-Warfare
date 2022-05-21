package sections.impl

import Classes
import readers.Reader
import readers.Attachment
import sections.Section
import java.util.jar.JarFile
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.kotlinProperty

class AttachmentSection(val name: String, val attachment: Attachment): Section(
    name,
    4) {

    override fun tryRender(jarFile: JarFile, classes: Classes, appendFunction: (data: String) -> Unit) {

        val properties = HashMap<String, String>()

        for (declaredField in attachment.javaClass.declaredFields) {
            declaredField.isAccessible = true
            val result =
                Reader.convertToString(declaredField.kotlinProperty as KProperty1<Any, *>, declaredField[attachment])
            properties[result.first] = result.second
        }

        appendFunction("| Key | Value |\n")
        appendFunction("| ---- | ---- |\n")

        for (property in properties) {
            appendFunction("| " + property.key + " | " + property.value + " |\n")
        }

    }

}