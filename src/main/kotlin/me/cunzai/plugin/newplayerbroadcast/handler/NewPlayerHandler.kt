package me.cunzai.plugin.newplayerbroadcast.handler

import me.cunzai.plugin.newplayerbroadcast.config.ConfigLoader
import me.cunzai.plugin.newplayerbroadcast.database.MySQLHandler
import me.cunzai.plugin.newplayerbroadcast.database.RedisHandler
import net.luckperms.api.event.user.UserFirstLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submitAsync


@PlatformSide(Platform.BUNGEE)
object NewPlayerHandlerBungee {
    @SubscribeEvent
    fun e(e: UserFirstLoginEvent) {
        submitAsync {
            try {
                MySQLHandler.newPlayerTable.workspace(MySQLHandler.datasource) {
                    insert("player_name", "timestamp") {
                        value(e.username, System.currentTimeMillis())
                    }
                }.run()
            } catch (_: Exception) {

            }
        }
    }
}

@PlatformSide(Platform.BUKKIT)
object NewPlayerHandlerBukkit {
    @SubscribeEvent
    fun e(e: PlayerJoinEvent) {
        val player = e.player
        if (ConfigLoader.broadcastOnThisServer) {
            submitAsync {
                MySQLHandler.newPlayerTable.workspace(MySQLHandler.datasource) {
                    select {
                        where {
                            "player_name" eq player.name
                        }
                    }
                }.firstOrNull {
                    MySQLHandler.newPlayerTable.workspace(MySQLHandler.datasource) {
                        delete {
                            where {
                                "player_name" eq player.name
                            }
                        }
                    }.run()

                    RedisHandler.handleNewPlayerJoin(player.name)
                }
            }
        }
    }
}