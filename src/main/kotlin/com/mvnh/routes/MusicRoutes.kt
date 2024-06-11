package com.mvnh.routes

import com.mvnh.utils.getMongoDatabase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.bson.Document
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@Serializable
data class TrackInfo(
    @SerialName("track_id") val trackId: Int,
    val title: String,
    val artist: String,
    val img: String,
    val duration: Int,
    val minutes: Int,
    val seconds: Int,
    val album: Int,
    @SerialName("download_link") val downloadLink: String
)

fun Route.musicRoutes() {
    val mongoDB = getMongoDatabase()
    val accountsCollection = mongoDB.getCollection("accounts")

    route("/music") {
        route("/yandex") {
            route("/track") {
                get("/get_and_save_current") {
                    val rythmapToken = call.parameters["rythmapToken"]
                    val yandexToken = call.parameters["yandexToken"]
                    if (yandexToken == null) {
                        call.respond(HttpStatusCode.BadRequest, "Token is missing")
                        return@get
                    } else {
                        val pythonPath: String
                        val scriptPath: String
                        if (File("C:\\Users\\13mvnh\\AppData\\Local\\Programs\\Python\\Python312\\python.exe").exists()
                            && File("C:\\Users\\13mvnh\\Code\\Kotlin\\Rythmap-server\\src\\main\\kotlin\\com\\mvnh\\music\\yandex\\get_current_track.py").exists()) {
                            pythonPath = "C:\\Users\\13mvnh\\AppData\\Local\\Programs\\Python\\Python312\\python.exe"
                            scriptPath = "C:\\Users\\13mvnh\\Code\\Kotlin\\Rythmap-server\\src\\main\\kotlin\\com\\mvnh\\music\\yandex\\get_current_track.py"
                        } else {
                            pythonPath = "/usr/bin/python3"
                            scriptPath = "/home/Rythmap-server-ktor/music/yandex/get_current_track.py"
                        }

                        val process = ProcessBuilder(pythonPath, scriptPath, "--token=$yandexToken").start()

                        val outputReader = BufferedReader(InputStreamReader(process.inputStream))
                        val errorReader = BufferedReader(InputStreamReader(process.errorStream))

                        val output = outputReader.readText()
                        val error = errorReader.readText()

                        if (output.isNotEmpty()) {
                            val jsonOutput = Json.decodeFromString<TrackInfo>(output)

                            val document = accountsCollection.find(Document("token", rythmapToken)).first()
                            if (document != null) {
                                accountsCollection.updateOne(
                                    Document("token", rythmapToken),
                                    Document("\$set", Document("last_tracks.yandex_track",
                                        Document.parse(
                                            Json.encodeToJsonElement(jsonOutput).toString()
                                        )
                                    ))
                                )
                                call.respond(HttpStatusCode.OK, jsonOutput)
                            } else {
                                call.respond(HttpStatusCode.BadRequest, "Output is not null but token is invalid")
                            }
                        } else if (error.isNotEmpty()) {
                            call.respond(HttpStatusCode.InternalServerError, error)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, "Unknown error")
                        }
                    }
                }

                get("/info") {
                    val trackID = call.parameters["trackID"]
                    if (trackID == null) {
                        call.respond(HttpStatusCode.BadRequest, "Track ID is missing")
                        return@get
                    } else {
                        val pythonPath: String
                        val scriptPath: String
                        if (File("C:\\Users\\13mvnh\\AppData\\Local\\Programs\\Python\\Python312\\python.exe").exists()
                            && File("C:\\Users\\13mvnh\\Code\\Kotlin\\Rythmap-server\\src\\main\\kotlin\\com\\mvnh\\music\\yandex\\get_track_info.py").exists()) {
                            pythonPath = "C:\\Users\\13mvnh\\AppData\\Local\\Programs\\Python\\Python312\\python.exe"
                            scriptPath = "C:\\Users\\13mvnh\\Code\\Kotlin\\Rythmap-server\\src\\main\\kotlin\\com\\mvnh\\music\\yandex\\get_track_info.py"
                        } else {
                            pythonPath = "/usr/bin/python3"
                            scriptPath = "/home/Rythmap-server-ktor/music/yandex/get_track_info.py"
                        }

                        val process = ProcessBuilder(pythonPath, scriptPath, "--trackID=$trackID").start()

                        val outputReader = BufferedReader(InputStreamReader(process.inputStream))
                        val errorReader = BufferedReader(InputStreamReader(process.errorStream))

                        val output = outputReader.readText()
                        val error = errorReader.readText()

                        if (output != null) {
                            val jsonOutput = Json.parseToJsonElement(output).jsonObject
                            call.respond(HttpStatusCode.OK, jsonOutput)
                        } else if (error != null) {
                            call.respond(HttpStatusCode.InternalServerError, error)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, "Unknown error")
                        }
                    }
                }
            }
        }
    }
}