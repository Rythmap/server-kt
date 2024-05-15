package com.mvnh.plugins

import com.mvnh.routes.accountRoutes
import com.mvnh.routes.friendsRoutes
import com.mvnh.routes.musicRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
    routing {
        if (File("/home/Rythmap-server-ktor/swagger.yaml").exists()) {
            swaggerUI(path = "swagger", swaggerFile = "/home/Rythmap-server-ktor/swagger.yaml")
        } else {
            swaggerUI(path = "swagger", swaggerFile = "C:\\Users\\13mvnh\\Code\\Kotlin\\Rythmap-server\\src\\main\\resources\\swagger.yaml")
        }
    }
    
    routing {
        get("/") {
            call.respondRedirect("/swagger")
        }
    }

    routing {
        accountRoutes()
        friendsRoutes()
        musicRoutes()
    }
}