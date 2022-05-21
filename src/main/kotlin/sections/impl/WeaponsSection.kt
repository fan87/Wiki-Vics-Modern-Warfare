package sections.impl

import Classes
import sections.Section
import weapons
import java.util.jar.JarFile

class WeaponsSection: Section("Weapons", 2) {

    override fun tryRender(jarFile: JarFile, classes: Classes, appendFunction: (data: String) -> Unit) {

        appendFunction("There are currently ${weapons.weapons.size} weapons that's marked as `Gun` in the game! Here's the list of them:\n\n")

        for (weapon in weapons.weapons) {
            WeaponSection(weapon.key, weapon.value).render(jarFile, classes, appendFunction)

        }


    }

}