package com.mvnh.utils

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase

fun getMongoDatabase(): MongoDatabase {
    val mongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString("mongodb://localhost:27017"))
        .build()
    val mongoClient = MongoClients.create(mongoClientSettings)
    return mongoClient.getDatabase("rythmap")
}