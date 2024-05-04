package com.mvnh.plugins

import com.mvnh.controllers.accountController
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "src/main/resources/swagger.yaml")
    }

    routing {
        get("/") {
            call.respondRedirect("/swagger")
        }
    }

    routing {
        accountController()
    }
}