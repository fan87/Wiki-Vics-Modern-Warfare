package sections

import Classes
import java.util.jar.JarFile

abstract class Section(val sectionTitle: String, val headerSize: Int) {

    companion object {
        val sections = ArrayList<Section>()
    }

    protected abstract fun tryRender(jarFile: JarFile, classes: Classes, appendFunction: (data: String) -> Unit)

    open fun render(jarFile: JarFile, classes: Classes, appendFunction: (data: String) -> Unit) {
        sections.add(this)
        appendFunction("#".repeat(headerSize) + " " + sectionTitle + "\n")
        tryRender(jarFile, classes, appendFunction)
    }

}