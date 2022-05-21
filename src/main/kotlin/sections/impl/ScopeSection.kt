package sections.impl

import Classes
import readers.Reader
import readers.Scope
import sections.Section
import java.util.jar.JarFile
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.kotlinProperty

class ScopeSection(val name: String, val scope: Scope): Section(
    name,
    4) {

    override fun tryRender(jarFile: JarFile, classes: Classes, appendFunction: (data: String) -> Unit) {

        val properties = HashMap<String, String>()

        for (declaredField in scope.javaClass.declaredFields) {
            declaredField.isAccessible = true
            val result =
                Reader.convertToString(declaredField.kotlinProperty as KProperty1<Any, *>, declaredField[scope])
            properties[result.first] = result.second
        }

        appendFunction("| Key | Value |\n")
        appendFunction("| ---- | ---- |\n")

        for (property in properties) {
            appendFunction("| " + property.key + " | " + property.value + " |\n")
        }

    }

}