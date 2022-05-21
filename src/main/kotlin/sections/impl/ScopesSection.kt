package sections.impl

import Classes
import attachments
import sections.Section
import weapons
import java.util.jar.JarFile

class ScopesSection: Section("Scopes", 2) {

    override fun tryRender(jarFile: JarFile, classes: Classes, appendFunction: (data: String) -> Unit) {

        appendFunction("There are currently ${weapons.weapons.size} scopes in the game! Here's the list of them:\n\n")

        for (scope in attachments.scopes) {
            ScopeSection(scope.key, scope.value).render(jarFile, classes, appendFunction)

        }


    }

}