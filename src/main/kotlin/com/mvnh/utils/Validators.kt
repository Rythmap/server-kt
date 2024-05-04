package com.mvnh.utils

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
    return collection.find(Document("email", email)).first() != null // If email exists, return true
}

// Nickname
fun validateNickname(nickname: String): Boolean {
    val nicknameRegex = Regex("^[A-Za-z0-9+_.-]{3,}\$")
    return nicknameRegex.matches(nickname)
}
fun checkNicknameExists(collection: MongoCollection<Document>, nickname: String): Boolean {
    return collection.find(Document("nickname", nickname)).first() != null // If nickname exists, return true
}

// Password
fun validatePassword(password: String): Boolean {
    // Minimum eight characters, at least one uppercase letter, one lowercase letter, one number and one special character
    val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$")
    return passwordRegex.matches(password)
}
fun hashPassword(password: String): String {
    return BCrypt.hashpw(password, BCrypt.gensalt())
}

fun validateUserCredentials(collection: MongoCollection<Document>, account: AccountLogin): Boolean {
    val document = collection.find(Document(
        if ("@" in account.login) "email" else "nickname", account.login
    )).first()
    return document != null && BCrypt.checkpw(account.password, document["password"] as String)
}

fun generateToken(): String {
    return UUID.randomUUID().toString()
}

fun generateAccountID(): String {
    return (0..31).map { ('0'..'9').random() }.joinToString("")
}

fun createAccountDocument(account: AccountRegister): Document {
    val document = Document()
    document["account_id"] = generateAccountID()
    document["token"] = generateToken()
    document["nickname"] = account.nickname
    document["visible_name"] = account.visibleName
    document["password"] = hashPassword(account.password)
    document["email"] = account.email
    document["music_preferences"] = account.musicPreferences
    document["other_preferences"] = account.otherPreferences
    document["about"] = account.about
    document["created_at"] = Date()
    return document
}