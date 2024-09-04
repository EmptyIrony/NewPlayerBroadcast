package me.cunzai.plugin.newplayerbroadcast.database

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.database.ColumnOptionSQL
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.Table
import taboolib.module.database.getHost

object MySQLHandler {

    @Config(value = "database.yml")
    lateinit var config: Configuration

    private val host by lazy {
        config.getHost("mysql")
    }

    val datasource by lazy {
        host.createDataSource()
    }

    val claimedTable by lazy {
        Table("new_player_claim_record", host) {
            add {
                id()
            }

            add("new_player_name") {
                type(ColumnTypeSQL.VARCHAR, 64) {
                    options(ColumnOptionSQL.KEY)
                }
            }

            add("claim_player_name") {
                type(ColumnTypeSQL.VARCHAR, 64) {
                    options(ColumnOptionSQL.KEY)
                }
            }

            add("timestamp") {
                type(ColumnTypeSQL.BIGINT)
            }
        }
    }

    val newPlayerTable by lazy {
        Table("new_player_record", host) {
            add("player_name") {
                type(ColumnTypeSQL.VARCHAR, 64) {
                    options(ColumnOptionSQL.PRIMARY_KEY)
                }
            }

            add("broadcast") {
                type(ColumnTypeSQL.INT)
            }

            add("timestamp") {
                type(ColumnTypeSQL.BIGINT)
            }
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun i() {
        if (Platform.CURRENT == Platform.BUKKIT) {
            claimedTable.workspace(datasource) {
                createTable(checkExists = true)
            }.run()
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun i1() {
        newPlayerTable.workspace(datasource) {
            createTable(checkExists = true)
        }.run()
    }

}