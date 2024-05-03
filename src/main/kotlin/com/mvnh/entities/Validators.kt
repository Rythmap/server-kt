package com.mvnh.entities

import com.mongodb.client.MongoCollection
import com.mvnh.entities.account.AccountLogin
import com.mvnh.entities.account.AccountRegister
import org.bson.Document
import org.mindrot.jbcrypt.BCrypt
import java.util.*

// Email
fun validateEmail(email: String): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@(.+)\$")
    return emailRegex.matches(email)
}
fun checkEmailExists(collection: MongoCollection<Document>, email: String): Boolean {
    return collection.find(Document("email", email)).first() != null
}

// Nickname
fun validateNickname(nickname: String): Boolean {
    val nicknameRegex = Regex("^[A-Za-z0-9+_.-]{3,}\$")
    return nicknameRegex.matches(nickname)
}
fun checkNicknameExists(collection: MongoCollection<Document>, nickname: String): Boolean {
    return collection.find(Document("name", nickname)).first() != null
}

// Password
fun validatePassword(password: String): Boolean {
    val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$")
    return passwordRegex.matches(password)
}
fun hashPassword(password: String): String {
    return BCrypt.hashpw(password, BCrypt.gensalt())
}

/*fun validateAccountLogin(account: AccountLogin): Boolean {
    return if ("@" in account.login) validateEmail(account.login) else validateNickname(account.login) && validatePassword(account.password)
}*/

fun generateToken(): String {
    return UUID.randomUUID().toString()
}

fun createAccountDocument(collection: MongoCollection<Document>, account: AccountRegister): Document {
    val document = Document()
    document["token"] = generateToken()
    document["name"] = account.nickname
    document["password"] = hashPassword(account.password)
    document["email"] = account.email
    document["created_at"] = Date()
    return document
}