package me.cunzai.plugin.newplayerbroadcast.database

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.expansion.AlkaidRedis
import taboolib.expansion.SingleRedisConnector
import taboolib.expansion.fromConfig
import taboolib.module.chat.Components
import taboolib.module.chat.colored
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.lang.asLangText
import taboolib.platform.compat.replacePlaceholder

@PlatformSide(Platform.BUKKIT)
object RedisHandler {

    @Config(value = "database.yml")
    lateinit var config: Configuration

    private val redisConnection: SingleRedisConnector by lazy {
        AlkaidRedis.create()
            .fromConfig(config.getConfigurationSection("redis")!!)
            .connect()
    }

    @Awake(LifeCycle.ENABLE)
    fun i() {
        redisConnection.connection().apply {
            subscribe("new_player_broadcast", patternMode = false) {
                Components.text(
                    console().asLangText("new_player_broadcast", message).colored()
                ).hoverText(console().asLangText("new_player_broadcast_hover"))
                    .clickRunCommand("/newplayer welcome $message")
                    .broadcast()
            }
        }

        redisConnection.connection().apply {
            subscribe("new_player_welcome", patternMode = false) {
                Bukkit.broadcastMessage(message)
            }
        }
    }

    fun handleNewPlayerJoin(playerName: String) {
        redisConnection.connection().publish("new_player_broadcast", playerName)
    }

    fun handleWelcome(player: Player, playerName: String) {
        val text = console().asLangText("new_player_welcome", player.name, playerName)
            .replacePlaceholder(player)

        redisConnection.connection().apply {
            publish("new_player_welcome", text)
        }
    }

}