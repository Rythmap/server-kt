package com.mvnh.controllers

import com.mvnh.entities.*
import com.mvnh.entities.account.AccountLogin
import com.mvnh.entities.account.AccountRegister
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.Document

@Serializable
data class AccountAuthResponse(val token: String, @SerialName("token_type") val tokenType: String = "bearer")

@Serializable
data class AccountInfoResponse(@SerialName("account_id") val accountID: String?,
                               val token: String,
                               val nickname: String,
                               val email: String,
                               @SerialName("created_at") val createdAt: String)

fun Route.accountController() {
    val mongoDB = getMongoDatabase()

    route("/account") {
        post("/register") {
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
                        call.respond(HttpStatusCode.BadRequest, "Invalid email")
                        return@post
                    } else if (!validatePassword(account.password)) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid password")
                        return@post
                    } else if (!validateNickname(account.nickname)) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid nickname")
                        return@post
                    } else {
                        val document = createAccountDocument(account)

                        collection.insertOne(document)
                        call.respond(HttpStatusCode.OK, AccountAuthResponse(document["token"].toString()))
                    }
                }
            }
        }

        post("/login") {
            val credentials = call.receive<AccountLogin>()

            if (credentials.login.isEmpty() || credentials.password.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Empty fields")
                return@post
            } else {
                val collection = mongoDB.getCollection("accounts")

                if (!validateUserCredentials(collection, credentials)) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                    return@post
                } else {
                    val document = collection.find(Document(
                        if ("@" in credentials.login) "email" else "name", credentials.login
                    )).first()

                    call.respond(HttpStatusCode.OK, AccountAuthResponse(document?.get("token").toString()))
                }
            }
        }

        get("/info") {
            val token = call.parameters["token"]
            if (token == null) {
                call.respond(HttpStatusCode.BadRequest, "Token not provided")
                return@get
            } else {
                val collection = mongoDB.getCollection("accounts")
                val document = collection.find(Document("token", token)).first()
                if (document == null) {
                    call.respond(HttpStatusCode.NotFound, "Token not found")
                    return@get
                } else {
                    call.respond(
                        AccountInfoResponse(
                            accountID = document["account_id"] as? String,
                            token = document["token"] as String,
                            nickname = document["name"] as String,
                            email = document["email"] as String,
                            createdAt = document["created_at"].toString()
                        )
                    )
                }
            }
        }

        delete("/delete") {
            val login = call.parameters["login"]
            val password = call.parameters["password"]

            if (login == null || password == null) {
                call.respond(HttpStatusCode.BadRequest, "Empty fields")
                return@delete
            } else {
                val collection = mongoDB.getCollection("accounts")

                if (!validateUserCredentials(collection, AccountLogin(login, password))) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                    return@delete
                } else {
                    val document = collection.find(Document(
                        if ("@" in login) "email" else "name", login
                    )).first()

                    if (document != null) {
                        collection.deleteOne(document)
                        call.respond(HttpStatusCode.OK, "Account deleted")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Account not found")
                    }
                }
            }
        }
    }
}