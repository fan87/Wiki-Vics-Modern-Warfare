package readers

import Classes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodInsnNode
import java.util.jar.JarFile
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class Weapons(jarFile: JarFile, classes: Classes) {

    val weapons = HashMap<String, Weapon>()

    init {
        for (entry in classes.filter {
            it.key.startsWith("com/vicmatskiv/mw/items/guns/") && it.key.endsWith("Factory")
        }) {
            val weapon = Weapon()
            val instructionBuffer = ArrayList<AbstractInsnNode>().apply {
                entry.value.methods.first { it.name == "createGun" }.instructions
                    .filter { it !is LabelNode && it !is LineNumberNode }
                    .forEach {
                    add(it)
                }
            }

            for (instructionWithIndex in instructionBuffer.withIndex()) {
                val instruction = instructionWithIndex.value
                val index = instructionWithIndex.index
                if (instruction is MethodInsnNode && instruction.owner == "com/vicmatskiv/weaponlib/Weapon\$Builder" && instruction.name.startsWith("with")) {
                    var done = false
                    for (field in Weapon::class.memberProperties) {
                        if (field is KMutableProperty1<*, *>) {
                            val field = field as KMutableProperty1<Any, Any>
                            if (instruction.name == "with" + field.name.replaceFirstChar { it.uppercaseChar() }) {
                                val value = Reader.read(field, instructionBuffer, weapon, index)
                                if (value != null) {
                                    field.isAccessible = true
                                    field.set(weapon, value)
                                }
                                done = true
                                break
                            }
                        }
                    }
                    if (!done) {
                        System.err.println("Could not find field of " + instruction.name)
                    }


                }
            }


            weapons[entry.key.replace(Regex("com/vicmatskiv/mw/items/guns/(.*)Factory"), "$1")] = weapon


        }

    }

}


class Weapon {

    ////////// Item Property //////////
    var modId: String? = null
    @Property("Item ID")
    var name: String? = null

    @FieldNameProperty var creativeTab: String? = null

    ////////// Gun Properties //////////
    @AmmoProperty
    var ammo: String? = null
    var ammoCapacity = 0
    var recoil = 1.0f
    var fireRate = 0.5f
    var inaccuracy = 1.0f
    var smokeEnabled = true
    var bleedingCoefficient = 1.0F
    var flashIntensity = 0.2F
    var isOneClickBurstAllowed = false
    @Property("Crafting Materials")
    @IngredientProperty
    var crafting: List<String> = listOf()

    ////////// Sound //////////
    @SoundProperty var shootSound: String? = null
    @SoundProperty var silencedShootSound: String? = null
    @SoundProperty var reloadSound: String? = null
    @SoundProperty var reloadIterationSound: String? = null
    @SoundProperty var inspectSound: String? = null
    @SoundProperty var drawSound: String? = null
    @SoundProperty var allReloadIterationsCompletedSound: String? = null
    @SoundProperty var unloadSound: String? = null
    @SoundProperty var ejectSpentRoundSound: String? = null
    @SoundProperty var endOfShootSound: String? = null
    @SoundProperty var burstShootSound: String? = null
    @SoundProperty var silencedBurstShootSound: String? = null
    @SoundProperty var exceededMaxShotsSound: String? = null




    ////////// Crosshair //////////
    @CrosshairProperty var crosshair: String? = null
    @Property("Crosshair (Running)")
    @CrosshairProperty var crosshairRunning: String? = null
    @Property("Crosshair (Zoomed)")
    @CrosshairProperty var crosshairZoomed: String? = null

    ////////// Ammo //////////
    @Property("Ammo Damage")
    var spawnEntityDamage = 0f
    @Property("Ammo Explosion Radius")
    var spawnEntityExplosionRadius = 0f
    @Property("Ammo Destroy Blocks")
    var isDestroyingBlocks = true
    @Property("Ammo Gravity Motion (Block/tick<sup>2</sup>)")
    var spawnEntityGravityVelocity = 0f
    @Property("Ammo Particle Age Coefficient")
    var spawnEntityParticleAgeCoefficient = 1.0f
    @Property("Ammo Smoke Particle Age Coefficient")
    var spawnEntitySmokeParticleAgeCoefficient = 1.0f
    @Property("Ammo Explosion Particle Scale Coefficient")
    var spawnEntityExplosionParticleScaleCoefficient = 1.5f
    @Property("Ammo Smoke Particle Scale Coefficient")
    var spawnEntitySmokeParticleScaleCoefficient = 1.0f
    @Property("Ammo Speed")
    var spawnEntitySpeed = 10.0f


    ////////// Time //////////
    @Property("Reload Time")
    @TickProperty var reloadingTime = 10L
    @Property("Unload Time")
    @TickProperty var unloadingTimeout = 10L
    @Property("Load Iteration Time")
    @TickProperty var loadIterationTimeout = 10L

    @AttachmentProperty var compatibleAttachment = ArrayList<String>()


}