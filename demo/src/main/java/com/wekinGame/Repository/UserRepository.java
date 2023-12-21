package com.wekinGame.Repository;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

public class UserRepository {

    private static MongoCollection<Document> collection = DatabaseRepository.getDatabase().getCollection("users");

    public static Document getUserInfoById(int id){
        Document queryParameter=new Document("_id",id);
        Document userInfo = collection.find(queryParameter)
            .projection(new Document("pseudo",1).append("date_naissance",1))
            .first();
        return userInfo;
    }

}