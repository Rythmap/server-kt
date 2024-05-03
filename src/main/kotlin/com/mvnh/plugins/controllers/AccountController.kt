package com.mvnh.plugins.controllers

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mvnh.entities.*
import com.mvnh.entities.account.AccountRegister
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountRegisterResponse(val token: String, val token_type: String = "bearer")

@Serializable
data class AccountInfoResponse(val token: String,
                               val name: String,
                               val email: String,
                               @SerialName("created_at") val createdAt: String)

fun Route.accountController() {
    val mongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString("mongodb://localhost:27017"))
        .build()
    val mongoClient = MongoClients.create(mongoClientSettings)
    val mongoDB = mongoClient.getDatabase("rythmap")

    post("/account/register") {
        val account = call.receive<AccountRegister>()

        if (account.nickname.isEmpty() || account.password.isEmpty() || account.email.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Empty fields")
            return@post
        } else {
            val collection = mongoDB.getCollection("accounts")

            if (checkEmailExists(collection, account.email)) {
                call.respond(HttpStatusCode.Conflict, "Email already exists")
                return@post
            } else if (checkNicknameExists(collection, account.nickname)) {
                call.respond(HttpStatusCode.Conflict, "Nickname already exists")
                return@post
            } else {
                if (!validateEmail(account.email)) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid email: must be a valid email address")
                    return@post
                } else if (!validatePassword(account.password)) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid password: must contain at least 8 characters and 1 number")
                    return@post
                } else if (!validateNickname(account.nickname)) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid nickname: must contain at least 3 characters and no special characters")
                    return@post
                } else {
                    val document = createAccountDocument(collection, account)

                    collection.insertOne(document)
                    call.respond(HttpStatusCode.OK, AccountRegisterResponse(document["token"].toString()))
                }
            }
        }
    }

    post("/account/login") {

    }

    get("/account/info") {
        val token = call.parameters["token"]
        if (token == null) {
            call.respond(HttpStatusCode.BadRequest, "Token not provided")
        } else {
            val collection = mongoDB.getCollection("accounts")
            val document = collection.find(org.bson.Document("token", token)).first()
            if (document == null) {
                call.respond(HttpStatusCode.NotFound, "Token not found")
                return@get
            } else {
                call.respond(
                    AccountInfoResponse(
                        token = document["token"] as String,
                        name = document["name"] as String,
                        email = document["email"] as String,
                        createdAt = document["created_at"].toString()
                    ),
                )
            }
        }
    }
}