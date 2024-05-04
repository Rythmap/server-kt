package com.mvnh.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.musicRoutes() {
    route("/music") {
        get("/search") {
            call.respondText("Music search")
        }
    }
}