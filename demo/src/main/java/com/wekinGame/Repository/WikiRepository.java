package com.wekinGame.Repository;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class WikiRepository {

    private MongoDatabase database = DatabaseRepository.getDatabase();
    private MongoCollection<Document> collection = database.getCollection("wikis");

    public Document getWikiById(int id) {
        Document searchQuery = new Document();
        searchQuery.put("_id", id);
        return collection.find(searchQuery).first();
    }

    public List<Document> getWikisByNamePrefix(String prefix) {
        Document searchQuery = new Document();
        searchQuery.put("nom", new Document("$regex", prefix).append("$options", "i"));
        List<Document> wikis = new ArrayList<>();
        FindIterable<Document> cursor = collection.find(searchQuery);
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                wikis.add(cursorIterator.next());
            }
        }
        return wikis;
    }

}