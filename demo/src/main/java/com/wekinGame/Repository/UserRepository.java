package com.wekinGame.Repository;

import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class UserRepository {

    private MongoDatabase database = DatabaseRepository.getDatabase();
    private MongoCollection<Document> collection = database.getCollection("users");

    public List<Document> getUserInfoById(int id){
        Document queryParameter=new Document("_id",id);
        Document userInfo = collection.find(queryParameter)
            .projection(new Document("pseudo",1).append("date_naissance",1))
            .first();
        return userInfo;
    }

}