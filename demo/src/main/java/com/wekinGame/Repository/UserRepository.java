package com.wekinGame.Repository;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;
import com.wekinGame.ressources.Hasher;

public class UserRepository {

    private static MongoCollection<Document> collection = DatabaseRepository.getDatabase().getCollection("users");

    public static Document getUserInfoById(int id){
        Document queryParameter=new Document("_id", id);
        Document userInfo = collection.find(queryParameter)
            .projection(new Document("pseudo",1).append("date_naissance",1))
            .first();
        return userInfo;
    }

    public static Document getFromPseudoAndPassword(String pseudo, String password) {
        List<Document> searchParameters = new ArrayList<>();
        searchParameters.add(new Document("pseudo",pseudo));
        searchParameters.add(new Document("mdp",Hasher.hashPassword(password)));
        Document searchQuery = new Document("$and", searchParameters);
        return collection.find(searchQuery).projection(Projections.include("_id")).first();
    }

    public static boolean usernameOrEmailTaken(String username, String email) {
        
        Document criteriaPseudo = new Document(
            "pseudo",
            new Document("$regex", "^"+username+"$").append("$options", "i")
        );
        Document criteriaMail = new Document(
            "email",
            new Document("$regex", "^"+email+"$").append("$options", "i")
        );
        List<Document> searchParameters = new ArrayList<>();
        searchParameters.add(criteriaPseudo);
        searchParameters.add(criteriaMail);
        Document searchQuery = new Document("$or", searchParameters);
        return (collection.find(searchQuery).first() != null);
    }

    public static boolean exist(int id) {
        Document queryParameter = new Document("_id", id);
        return (collection.find(queryParameter).first() != null);
    }

    public static void push(Document newUser) {
        collection.insertOne(newUser);
    }

    public static void delete(int id) {
        Document queryParameter = new Document("_id", id);
        collection.deleteOne(queryParameter);
    }
}