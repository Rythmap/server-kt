package com.mvnh

import com.mvnh.plugins.configureRouting
import com.mvnh.plugins.configureSerialization
import com.mvnh.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
    configureSerialization()
    configureSockets()
}
