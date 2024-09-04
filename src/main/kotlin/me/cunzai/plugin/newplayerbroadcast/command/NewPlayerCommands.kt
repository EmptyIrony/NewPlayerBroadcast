package me.cunzai.plugin.newplayerbroadcast.command

import me.cunzai.plugin.newplayerbroadcast.config.ConfigLoader
import me.cunzai.plugin.newplayerbroadcast.database.MySQLHandler
import me.cunzai.plugin.newplayerbroadcast.database.RedisHandler
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.*
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.expansion.createHelper
import taboolib.platform.util.sendLang

@PlatformSide(Platform.BUKKIT)
@CommandHeader("newPlayer")
object NewPlayerCommands {

    @CommandBody(permissionDefault = PermissionDefault.TRUE, permission = "command.welcome")
    val welcome = subCommand {
        dynamic("玩家名字") {
            execute<Player> { sender, _, argument ->
                submitAsync {
                    MySQLHandler.newPlayerTable.workspace(MySQLHandler.datasource) {
                        select {
                            where {
                                "player_name" eq argument
                            }
                        }
                    }.firstOrNull {
                        MySQLHandler.claimedTable.workspace(MySQLHandler.datasource) {
                            select {
                                where {
                                    ("new_player_name" eq argument) and ("claim_player_name" eq sender.name)
                                }
                            }
                        }.firstOrNull {
                            sender.sendLang("claimed", argument)
                        } ?: run {
                            MySQLHandler.claimedTable.workspace(MySQLHandler.datasource) {
                                insert("new_player_name", "claim_player_name", "timestamp") {
                                    value(argument, sender.name, System.currentTimeMillis())
                                }
                            }.run()

                            RedisHandler.handleWelcome(sender, argument)

                            submit {
                                ConfigLoader.rewards.forEach { command ->
                                    Bukkit.dispatchCommand(
                                        Bukkit.getConsoleSender(),
                                        command.replace("%player%", sender.name)
                                    )
                                }
                            }
                        }
                    } ?: run {
                        sender.sendLang("no_new_player", argument)
                    }
                }
            }
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            ConfigLoader.config.reload()
            ConfigLoader.i()
            sender.sendMessage("ok")
        }
    }

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

}