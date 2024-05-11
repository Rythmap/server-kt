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
data class AcceptFriendRequest(val toNickname: String, val fromToken: String)
@Serializable
data class DeclineFriendRequest(val toNickname: String, val fromToken: String)

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
                        var fromFriends = fromDocument["friends"] as? MutableList<String>
                        var toFriendRequests = toDocument["friend_requests"] as? MutableList<String>

                        if (fromFriends == null) { // если у from нет друзей, то создаем пустой список
                            fromFriends = mutableListOf<String>()
                            accountsCollection.updateOne(Document("token", request.fromToken), Document("\$set", Document("friends", fromFriends)))
                        }
                        if (toFriendRequests == null) { // если у to нет запросов в друзья, то создаем пустой список
                            toFriendRequests = mutableListOf<String>()
                            accountsCollection.updateOne(Document("nickname", request.toNickname), Document("\$set", Document("friend_requests", toFriendRequests)))
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

                if (request.toNickname != null && request.fromToken != null) {
                    val toDocument = accountsCollection.find(Document("nickname", request.toNickname)).first()
                    val fromDocument = accountsCollection.find(Document("token", request.fromToken)).first()

                    if (toDocument != null && fromDocument != null) {
                        var toFriends = toDocument["friends"] as? MutableList<String>
                        var fromFriends = fromDocument["friends"] as? MutableList<String>
                        var fromFriendRequests = fromDocument["friend_requests"] as? MutableList<String>

                        if (toFriends == null) { // если у to нет друзей, то создаем пустой список
                            toFriends = mutableListOf<String>()
                            accountsCollection.updateOne(Document("nickname", request.toNickname), Document("\$set", Document("friends", toFriends)))
                        }
                        if (fromFriends == null) { // если у from нет друзей, то создаем пустой список
                            fromFriends = mutableListOf<String>()
                            accountsCollection.updateOne(Document("token", request.fromToken), Document("\$set", Document("friends", fromFriends)))
                        }
                        if (fromFriendRequests == null) { // если у from нет запросов в друзья, то создаем пустой список
                            fromFriendRequests = mutableListOf<String>()
                            accountsCollection.updateOne(Document("token", request.fromToken), Document("\$set", Document("friend_requests", fromFriendRequests)))
                        }

                        if (fromFriendRequests.contains(toDocument["nickname"])) { // если у from есть запрос в друзья от to
                            fromFriendRequests.remove(toDocument["nickname"])
                            accountsCollection.updateOne(Document("token", request.fromToken), Document("\$set", Document("friend_requests", fromFriendRequests)))

                            toFriends.add(fromDocument["nickname"] as String)
                            fromFriends?.add(toDocument["nickname"] as String)

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
                TODO("Decline friend request")
            }
        }
    }
}