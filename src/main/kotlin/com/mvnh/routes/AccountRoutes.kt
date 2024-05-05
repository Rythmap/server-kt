package com.mvnh.routes

import com.mvnh.entities.account.*
import com.mvnh.utils.*
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
data class AccountInfoPrivateResponse(@SerialName("account_id") val accountID: String,
                               val token: String,
                               val nickname: String,
                               @SerialName("visible_name") val visibleName: AccountVisibleName?,
                               val email: String,
                               @SerialName("music_preferences") val musicPreferences: List<String>? = null,
                               @SerialName("other_preferences") val otherPreferences: List<String>? = null,
                               val about: String? = null,
                               @SerialName("created_at") val createdAt: String)

@Serializable
data class AccountInfoPublicResponse(@SerialName("account_id") val accountID: String,
                               val nickname: String,
                               @SerialName("visible_name") val visibleName: AccountVisibleName?,
                               @SerialName("music_preferences") val musicPreferences: List<String>? = null,
                               @SerialName("other_preferences") val otherPreferences: List<String>? = null,
                               val about: String? = null,
                               @SerialName("created_at") val createdAt: String)

fun Route.accountRoutes() {
    val mongoDB = getMongoDatabase()
    val accountsCollection = mongoDB.getCollection("accounts")

    route("/account") {
        post("/register") {
            val account = call.receive<AccountRegister>()

            if (account.nickname.isEmpty() || account.password.isEmpty() || account.email.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Empty fields") // 400
                return@post
            } else {
                if (checkEmailExists(accountsCollection, account.email)) {
                    call.respond(HttpStatusCode.Conflict, "Email already exists") // 409
                    return@post
                } else if (checkNicknameExists(accountsCollection, account.nickname)) {
                    call.respond(HttpStatusCode.Conflict, "Nickname already exists")
                    return@post
                } else {
                    if (!validateEmail(account.email)) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid email") // 400
                        return@post
                    } else if (!validatePassword(account.password)) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid password")
                        return@post
                    } else if (!validateNickname(account.nickname)) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid nickname")
                        return@post
                    } else {
                        val document = createAccountDocument(account)

                        accountsCollection.insertOne(document)
                        call.respond(HttpStatusCode.OK, AccountAuthResponse(document["token"].toString())) // 200
                    }
                }
            }
        }

        post("/login") {
            val credentials = call.receive<AccountLogin>()

            if (credentials.login.isEmpty() || credentials.password.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Empty fields") // 400
                return@post
            } else {
                if (!validateUserCredentials(accountsCollection, credentials)) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials") // 401
                    return@post
                } else {
                    val document = accountsCollection.find(Document(
                        if ("@" in credentials.login) "email" else "nickname", credentials.login
                    )).first()

                    call.respond(HttpStatusCode.OK, AccountAuthResponse(document?.get("token").toString())) // 200
                }
            }
        }

        route("/info") {
            get("/public") {
                val nickname = call.parameters["nickname"]
                if (nickname == null) {
                    call.respond(HttpStatusCode.BadRequest, "Nickname not provided")
                    return@get
                } else {
                    val document = accountsCollection.find(Document("nickname", nickname)).first()
                    if (document == null) {
                        call.respond(HttpStatusCode.NotFound, "Account not found")
                        return@get
                    } else {
                        val visibleNameDocument = document["visible_name"] as Document?
                        call.respond(
                            AccountInfoPublicResponse(
                                accountID = document["account_id"] as String,
                                nickname = document["nickname"] as String,
                                visibleName = AccountVisibleName(
                                    name = visibleNameDocument?.get("name") as String?,
                                    surname = visibleNameDocument?.get("surname") as String?
                                ),
                                musicPreferences = document["music_preferences"] as List<String>?,
                                otherPreferences = document["other_preferences"] as List<String>?,
                                createdAt = document["created_at"].toString()
                            )
                        )
                    }
                }
            }

            get("/private") {
                val token = call.parameters["token"]
                if (token == null) {
                    call.respond(HttpStatusCode.BadRequest, "Token not provided")
                    return@get
                } else {
                    val document = accountsCollection.find(Document("token", token)).first()
                    if (document == null) {
                        call.respond(HttpStatusCode.NotFound, "Token not found")
                        return@get
                    } else {
                        val visibleNameDocument = document["visible_name"] as Document?
                        call.respond(
                            AccountInfoPrivateResponse(
                                accountID = document["account_id"] as String,
                                token = document["token"] as String,
                                nickname = document["nickname"] as String,
                                visibleName = AccountVisibleName(
                                    name = visibleNameDocument?.get("name") as String?,
                                    surname = visibleNameDocument?.get("surname") as String?
                                ),
                                musicPreferences = document["music_preferences"] as List<String>?,
                                otherPreferences = document["other_preferences"] as List<String>?,
                                about = document["about"] as String?,
                                email = document["email"] as String,
                                createdAt = document["created_at"].toString()
                            )
                        )
                    }
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
                if (!validateUserCredentials(accountsCollection, AccountLogin(login, password))) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                    return@delete
                } else {
                    val document = accountsCollection.find(Document(
                        if ("@" in login) "email" else "nickname", login
                    )).first()

                    if (document != null) {
                        accountsCollection.deleteOne(document)
                        call.respond(HttpStatusCode.OK, "Account deleted")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Account not found")
                    }
                }
            }
        }

        route("/update") {
            post("/info") {
                val account = call.receive<AccountUpdateInfo>()

                val document = accountsCollection.find(Document("token", account.token)).first()
                if (document == null) {
                    call.respond(HttpStatusCode.NotFound, "Token not found")
                    return@post
                } else {
                    accountsCollection.updateOne(Document("token", account.token), Document("\$set", Document("visible_name.name", account.visibleName?.name)))
                    accountsCollection.updateOne(Document("token", account.token), Document("\$set", Document("visible_name.surname", account.visibleName?.surname)))
                    accountsCollection.updateOne(Document("token", account.token), Document("\$set", Document("music_preferences", account.musicPreferences)))
                    accountsCollection.updateOne(Document("token", account.token), Document("\$set", Document("other_preferences", account.otherPreferences)))
                    accountsCollection.updateOne(Document("token", account.token), Document("\$set", Document("about", account.about)))
                    call.respond(HttpStatusCode.OK, "Account updated")
                }
            }

            post("/nickname") {
                val account = call.receive<AccountUpdateNickname>()

                val document = accountsCollection.find(Document("token", account.token)).first()

                if (document == null) {
                    call.respond(HttpStatusCode.NotFound, "Token not found")
                    return@post
                } else {
                    if (!validateNickname(account.newNickname)) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid nickname")
                        return@post
                    } else if (checkNicknameExists(accountsCollection, account.newNickname)) {
                        call.respond(HttpStatusCode.Conflict, "Nickname already exists")
                        return@post
                    } else {
                        accountsCollection.updateOne(Document("token", account.token), Document("\$set", Document("nickname", account.newNickname)))
                        call.respond(HttpStatusCode.OK, "Nickname changed")
                    }
                }
            }

            post("/password") {
                val account = call.receive<AccountUpdatePassword>()

                val document = accountsCollection.find(Document("nickname", account.nickname)).first()

                if (document == null) {
                    call.respond(HttpStatusCode.NotFound, "Account not found")
                    return@post
                } else {
                    if (!validatePassword(account.newPassword)) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid new password")
                        return@post
                    } else {
                        if (validateUserCredentials(accountsCollection, AccountLogin(account.nickname, account.currentPassword))) {
                            accountsCollection.updateOne(Document("nickname", account.nickname), Document("\$set", Document("password", hashPassword(account.newPassword))))
                            call.respond(HttpStatusCode.OK, "Password changed")
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                        }
                    }
                }
            }
        }
    }
}