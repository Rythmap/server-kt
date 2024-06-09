package com.mvnh.sockets

import com.mvnh.utils.getMongoDatabase
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Serializable
data class User(val nickname: String,
                var location: Location,
                var status: String,
                val token: String,
                var command: String? = null
)
@Serializable
data class Location(val lat: Double, val lng: Double)

val users = mutableListOf<User>()
val sessions = mutableSetOf<WebSocketSession>()
val hasSentLocation = mutableMapOf<WebSocketSession, Boolean>()

suspend fun broadcast(message: String) {
    sessions.forEach { session ->
        if (hasSentLocation[session] == true) {
            session.send(message)
            hasSentLocation[session] = false
        }
    }
}

// No.
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val distance = earthRadius * c
    return distance * 1000
}

fun Route.mapSocket() {
    val mongoDB = getMongoDatabase()
    val accountsCollection = mongoDB.getCollection("accounts")

    webSocket("/map") {
        sessions.add(this)
        val nickname = call.parameters["nickname"]
        if (nickname != null) {
            val user = users.find { it.nickname == nickname }
            if (user != null) {
                user.status = "online"
                broadcast(Json.encodeToString(user))
            }
        }

        for (frame in incoming) {
            frame as? Frame.Text ?: continue
            val receivedText = frame.readText()
            val receivedUser = Json.decodeFromString<User>(receivedText)

            val document = accountsCollection.find(Document("token", receivedUser.token)).first()
            if (document["nickname"] != receivedUser.nickname || document == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
                break
            }

            val existingUser = users.find { it.nickname == receivedUser.nickname }
            if (existingUser != null) {
                existingUser.location = receivedUser.location
                existingUser.status = receivedUser.status
            } else {
                users.add(receivedUser)
            }

            if (receivedUser.command == "updateStatus") {
                val user = users.find { it.nickname == receivedUser.nickname }
                if (user != null) {
                    user.status = "offline"
                    user.command = null
                }
                close(CloseReason(CloseReason.Codes.NORMAL, "User has gone offline"))
            }

            hasSentLocation[this] = true
            val nearbyUsers = users.filter {
                it.status == "online" && calculateDistance(
                    it.location.lat, it.location.lng,
                    receivedUser.location.lat, receivedUser.location.lng
                ) < 500
            }

            val response = Json.encodeToString(nearbyUsers)
            broadcast(response)
        }

        sessions.remove(this)
        hasSentLocation.remove(this)
    }
}