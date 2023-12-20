package com.wekinGame.Repository;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class EntryRepository {

    private MongoDatabase database = DatabaseRepository.getDatabase();
    private MongoCollection<Document> collection = database.getCollection("entrees");

    public List<Document> getEntriesByIdWiki(int id){
        Document searchQuery = new Document();
        searchQuery.put("id_wiki", id);
        List<Document> entries = new ArrayList<>();
        FindIterable<Document> cursor = collection.find(searchQuery);
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                entries.add(cursorIterator.next());
            }
        }
        return entries;
    }

}