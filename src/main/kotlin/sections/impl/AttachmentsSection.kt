package sections.impl

import Classes
import attachments
import sections.Section
import weapons
import java.util.jar.JarFile

class AttachmentsSection: Section("Attachments", 2) {

    override fun tryRender(jarFile: JarFile, classes: Classes, appendFunction: (data: String) -> Unit) {

        appendFunction("There are currently ${weapons.weapons.size} attachments in the game! Here's the list of them:\n\n")

        for (attachment in attachments.attachments) {
            AttachmentSection(attachment.key, attachment.value).render(jarFile, classes, appendFunction)

        }


    }

}