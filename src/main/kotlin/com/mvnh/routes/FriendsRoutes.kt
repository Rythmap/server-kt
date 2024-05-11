package com.mvnh.routes

import com.mvnh.utils.getMongoDatabase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.bson.Document
import kotlin.String as String1

@Serializable
data class SendFriendRequest(val fromToken: String1, val toNickname: String1, val message: String1? = null)
@Serializable
data class AcceptFriendRequest(val toNickname: String1, val fromToken: String1)
@Serializable
data class DeclineFriendRequest(val toNickname: String1, val fromToken: String1)

fun Route.friendsRoutes() {
    val mongoDB = getMongoDatabase()
    val accountsCollection = mongoDB.getCollection("accounts")

    route("/friends") {
        route("/request") {
            post("/send") {
                val request = call.receive<SendFriendRequest>()

                if (request.fromToken != null && request.toNickname != null) {
                    val fromDocument = accountsCollection.find(Document("token", request.fromToken)).first()
                    val toDocument = accountsCollection.find(Document("nickname", request.toNickname)).first()

                    if (fromDocument != null && toDocument != null) {
                        var fromFriends = fromDocument["friends"] as? MutableList<String1>
                        var toFriendRequests = toDocument["friend_requests"] as? MutableList<String1>

                        if (fromFriends == null) {
                            fromFriends = mutableListOf()
                            accountsCollection.updateOne(Document("token", request.fromToken), Document("\$set", Document("friends", fromFriends)))
                        }
                        if (toFriendRequests == null) {
                            toFriendRequests = mutableListOf()
                            accountsCollection.updateOne(Document("nickname", request.toNickname), Document("\$set", Document("friend_requests", toFriendRequests)))
                        }

                        if (!fromFriends.contains(request.toNickname) && !toFriendRequests.contains(fromDocument["nickname"])) { // если у from нет в друзьях to И у to нет запроса от from
                            toFriendRequests.add(fromDocument["nickname"] as String1)
                            accountsCollection.updateOne(Document("nickname", request.toNickname), Document("\$set", Document("friend_requests", toFriendRequests)))
                            call.respond(HttpStatusCode.OK, "Friend request sent")
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Friend request already sent")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid token or nickname")
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Missing parameters")
                }
            }

            post("/accept") {
                val request = call.receive<AcceptFriendRequest>()

                if (request.toNickname != null && request.fromToken != null) {
                    val toDocument = accountsCollection.find(Document("nickname", request.toNickname)).first()
                    val fromDocument = accountsCollection.find(Document("token", request.fromToken)).first()

                    if (toDocument != null && fromDocument != null) {
                        var toFriends = toDocument["friends"] as? MutableList<String1>
                        var fromFriends = fromDocument["friends"] as? MutableList<String1>
                        var fromFriendRequests = fromDocument["friend_requests"] as? MutableList<String1>

                        if (toFriends == null) {
                            toFriends = mutableListOf()
                            accountsCollection.updateOne(Document("nickname", request.toNickname), Document("\$set", Document("friends", toFriends)))
                        }
                        if (fromFriends == null) {
                            fromFriends = mutableListOf()
                            accountsCollection.updateOne(Document("token", request.fromToken), Document("\$set", Document("friends", fromFriends)))
                        }
                        if (fromFriendRequests == null) {
                            fromFriendRequests = mutableListOf()
                            accountsCollection.updateOne(Document("token", request.fromToken), Document("\$set", Document("friend_requests", fromFriendRequests)))
                        }

                        if (fromFriendRequests.contains(toDocument["nickname"])) {
                            fromFriendRequests.remove(toDocument["nickname"])
                            accountsCollection.updateOne(Document("token", request.fromToken), Document("\$set", Document("friend_requests", fromFriendRequests)))

                            toFriends.add(fromDocument["nickname"] as String1)
                            fromFriends?.add(toDocument["nickname"] as String1)

                            accountsCollection.updateOne(Document("nickname", request.toNickname), Document("\$set", Document("friends", toFriends)))
                            accountsCollection.updateOne(Document("token", request.fromToken), Document("\$set", Document("friends", fromFriends)))

                            call.respond(HttpStatusCode.OK, "Friend request accepted")
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "No friend request from this user")
                        }
                    }
                }
            }

            post("/decline") {
                val request = call.receive<DeclineFriendRequest>()

                if (request.toNickname != null && request.fromToken != null) {
                    val toDocument = accountsCollection.find(Document("nickname", request.toNickname)).first()
                    val fromDocument = accountsCollection.find(Document("token", request.fromToken)).first()

                    if (toDocument != null && fromDocument != null) {
                        var fromFriendRequests = fromDocument["friend_requests"] as? MutableList<String1>

                        if (fromFriendRequests == null) {
                            fromFriendRequests = mutableListOf()
                            accountsCollection.updateOne(
                                Document("token", request.fromToken),
                                Document("\$set", Document("friend_requests", fromFriendRequests))
                            )
                        }

                        if (fromFriendRequests?.contains(toDocument["nickname"]) == true) {
                            fromFriendRequests?.remove(toDocument["nickname"])
                            accountsCollection.updateOne(
                                Document("token", request.fromToken),
                                Document("\$set", Document("friend_requests", fromFriendRequests))
                            )

                            call.respond(HttpStatusCode.OK, "Friend request declined")
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "No friend request from this user")
                        }
                    }
                }
            }

            post("/cancel") {
                TODO("Not yet implemented")
            }

            post("/remove") {
                TODO("Not yet implemented")
            }
        }
    }
}