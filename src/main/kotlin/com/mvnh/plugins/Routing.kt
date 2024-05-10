package com.mvnh.plugins

import com.mvnh.routes.accountRoutes
import com.mvnh.routes.musicRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "/home/Rythmap-server-ktor/swagger.yaml")
        //swaggerUI(path = "swagger", swaggerFile = "C:\\Users\\13mvnh\\Code\\Kotlin\\Rythmap-server\\src\\main\\resources\\swagger.yaml")
    }
    
    routing {
        get("/") {
            call.respondRedirect("/swagger")
        }
    }

    routing {
        accountRoutes()
        musicRoutes()
    }
}