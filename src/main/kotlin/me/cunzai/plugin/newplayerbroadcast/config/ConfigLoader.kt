package me.cunzai.plugin.newplayerbroadcast.config

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getStringListColored

@PlatformSide(Platform.BUKKIT)
object ConfigLoader {
    @Config(value = "config.yml")
    lateinit var config: Configuration

    val rewards = ArrayList<String>()

    var broadcastOnThisServer = false

    @Awake(LifeCycle.ENABLE)
    fun i() {
        rewards.clear()
        rewards += config.getStringListColored("rewards")
        broadcastOnThisServer = config.getBoolean("broadcastOnThisServer")
    }

}