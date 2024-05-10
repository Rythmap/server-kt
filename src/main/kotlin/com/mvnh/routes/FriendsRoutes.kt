package com.mvnh.routes

import com.mvnh.utils.getMongoDatabase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.bson.Document

@Serializable
data class SendFriendRequest(val fromToken: String, val toNickname: String, val message: String? = null)
@Serializable
data class AcceptFriendRequest(val toToken: String, val fromNickname: String)
@Serializable
data class DeclineFriendRequest(val toToken: String, val fromNickname: String)

fun Route.friendsRoutes() {
    val mongoDB = getMongoDatabase()
    val accountsCollection = mongoDB.getCollection("accounts")

    route("/friends") {
        route("/request") {
            post("/send") {
                val request = call.receive<SendFriendRequest>()

                // possible request codes are 200, 400, 401
                if (request.fromToken != null && request.toNickname != null) {
                    val fromDocument = accountsCollection.find(Document("token", request.fromToken)).first()
                    val toDocument = accountsCollection.find(Document("nickname", request.toNickname)).first()

                    if (fromDocument != null && toDocument != null) {
                        var fromFriends = fromDocument["friends"] as MutableList<String>
                        var toFriendRequests = toDocument["friend_requests"] as MutableList<String>

                        if (fromFriends == null) { // если у from нет друзей, то создаем пустой список
                            accountsCollection.updateOne(Document("token", request.fromToken), Document("\$set", Document("friends", mutableListOf<String>())))
                            fromFriends = fromDocument["friends"] as MutableList<String>
                        }
                        if (toFriendRequests == null) { // если у to нет запросов в друзья, то создаем пустой список
                            accountsCollection.updateOne(Document("nickname", request.toNickname), Document("\$set", Document("friend_requests", mutableListOf<String>())))
                            toFriendRequests = toDocument["friend_requests"] as MutableList<String>
                        }

                        if (!fromFriends.contains(request.toNickname) && !toFriendRequests.contains(fromDocument["nickname"])) { // если у from нет в друзьях to И у to нет запроса от from
                            toFriendRequests.add(fromDocument["nickname"] as String)
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

                if (request.toToken != null && request.fromNickname != null) {
                    val toDocument = accountsCollection.find(Document("token", request.toToken)).first()
                    val fromDocument = accountsCollection.find(Document("nickname", request.fromNickname)).first()

                    if (toDocument != null && fromDocument != null) {
                        var toFriends = toDocument["friends"] as MutableList<String>
                        var fromFriends = fromDocument["friends"] as MutableList<String>
                        var toFriendRequests = toDocument["friend_requests"] as MutableList<String>

                        if (toFriends == null) {
                            accountsCollection.updateOne(Document("token", request.toToken), Document("\$set", Document("friends", mutableListOf<String>())))
                            toFriends = toDocument["friends"] as MutableList<String>
                        }
                        if (fromFriends == null) {
                            accountsCollection.updateOne(Document("nickname", request.fromNickname), Document("\$set", Document("friends", mutableListOf<String>())))
                            fromFriends = fromDocument["friends"] as MutableList<String>
                        }
                        if (toFriendRequests == null) {
                            accountsCollection.updateOne(Document("token", request.toToken), Document("\$set", Document("friend_requests", mutableListOf<String>())))
                            toFriendRequests = toDocument["friend_requests"] as MutableList<String>
                        }

                        if (toFriendRequests.contains(fromDocument["nickname"])) {
                            toFriends.add(fromDocument["nickname"] as String)
                            fromFriends.add(toDocument["nickname"] as String)
                            toFriendRequests.remove(fromDocument["nickname"] as String)

                            accountsCollection.updateOne(Document("token", request.toToken), Document("\$set", Document("friends", toFriends)))
                            accountsCollection.updateOne(Document("nickname", request.fromNickname), Document("\$set", Document("friends", fromFriends)))
                            accountsCollection.updateOne(Document("token", request.toToken), Document("\$set", Document("friend_requests", toFriendRequests)))

                            call.respond(HttpStatusCode.OK, "Friend request accepted")
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "No friend request from this user")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid token or nickname")
                    }
                }
            }

            post("/decline") {
                val request = call.receive<DeclineFriendRequest>()

                if (request.toToken != null && request.fromNickname != null) {
                    val toDocument = accountsCollection.find(Document("token", request.toToken)).first()
                    val fromDocument = accountsCollection.find(Document("nickname", request.fromNickname)).first()

                    if (toDocument != null && fromDocument != null) {
                        var toFriendRequests = toDocument["friend_requests"] as MutableList<String>

                        if (toFriendRequests == null) {
                            accountsCollection.updateOne(Document("token", request.toToken), Document("\$set", Document("friend_requests", mutableListOf<String>())))
                            toFriendRequests = toDocument["friend_requests"] as MutableList<String>
                        }

                        if (toFriendRequests.contains(fromDocument["nickname"])) {
                            toFriendRequests.remove(fromDocument["nickname"] as String)

                            accountsCollection.updateOne(Document("token", request.toToken), Document("\$set", Document("friend_requests", toFriendRequests)))

                            call.respond(HttpStatusCode.OK, "Friend request declined")
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "No friend request from this user")
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Invalid token or nickname")
                    }
                }
            }
        }
    }
}