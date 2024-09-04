package me.cunzai.plugin.newplayerbroadcast.handler

import me.cunzai.plugin.newplayerbroadcast.config.ConfigLoader
import me.cunzai.plugin.newplayerbroadcast.database.MySQLHandler
import me.cunzai.plugin.newplayerbroadcast.database.RedisHandler
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.user.UserFirstLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submitAsync
import taboolib.platform.BungeePlugin


@PlatformSide(Platform.BUNGEE)
object NewPlayerHandlerBungee {
    @Awake(LifeCycle.ENABLE)
    fun i() {
        LuckPermsProvider.get()
            .eventBus
            .subscribe(BungeePlugin.getInstance(), UserFirstLoginEvent::class.java) { event ->
                try {
                    MySQLHandler.newPlayerTable.workspace(MySQLHandler.datasource) {
                        insert("player_name", "timestamp") {
                            value(event.username, System.currentTimeMillis())
                        }
                    }.run()
                } catch (e: Exception) {
                    e.printStackTrace()
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
                            "player_name" eq player.name and ("broadcast" eq 0)
                        }
                    }
                }.firstOrNull {
                    MySQLHandler.newPlayerTable.workspace(MySQLHandler.datasource) {
                        update {
                            set("broadcast", 1)
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