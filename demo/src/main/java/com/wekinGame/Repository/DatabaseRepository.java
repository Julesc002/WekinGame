package com.wekinGame.Repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import lombok.Getter;

public class DatabaseRepository {

    private static MongoClient mongoClient = MongoClients.create("mongodb+srv://gamer:ratio@bdwekingame.decr9eq.mongodb.net/");
    @Getter
    private static MongoDatabase database = mongoClient.getDatabase("WekinGame");

}