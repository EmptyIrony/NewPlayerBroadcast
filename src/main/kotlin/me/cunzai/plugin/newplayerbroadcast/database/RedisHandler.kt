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
        redisConnection.connection().use { connection ->
            connection.subscribe("new_player_broadcast", patternMode = false) {
                val playerName = get<NewPlayerJoined>(ignoreConstructor = true).name
                Components.text(
                    console().asLangText("new_player_broadcast", playerName).colored()
                ).hoverText(console().asLangText("new_player_broadcast_hover"))
                    .clickRunCommand("/newPlayer welcome $playerName")
                    .broadcast()
            }

            connection.subscribe("new_player_welcome", patternMode = false) {
                val text = get<NewPlayerWelcome>(ignoreConstructor = true).text
                Bukkit.broadcastMessage(text)
            }
        }

    }

    fun handleNewPlayerJoin(playerName: String) {
        redisConnection.connection().use { connection ->
            connection.publish("new_player_broadcast", NewPlayerJoined(playerName))
        }
    }

    fun handleWelcome(player: Player, playerName: String) {
        redisConnection.connection().use { connection ->
            val text = console().asLangText("new_player_welcome", player.name, playerName)
                .replacePlaceholder(player)
            connection.publish("new_player_welcome", text)
        }
    }

    class NewPlayerWelcome(val text: String)

    class NewPlayerJoined(val name: String)

}