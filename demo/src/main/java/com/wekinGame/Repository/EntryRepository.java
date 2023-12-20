package com.wekinGame.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.UpdateResult;

public class EntryRepository {

    private static MongoDatabase database = DatabaseRepository.getDatabase();
    private static MongoCollection<Document> collection = database.getCollection("entrees");

    public static List<Document> getEntriesByIdWiki(int id){
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

    public static List<Document> getEntryByWikiAndCategory(int id_wiki, String nameCategory){
        List<Document> entry = new ArrayList<Document>();
            Document searchQuery = new Document();
            searchQuery.put("id_wiki", id_wiki);
            searchQuery.put("categories", nameCategory);
            List<Bson> pipeline = Arrays.asList(
                    Aggregates.match(searchQuery),
                    Aggregates.project(Projections.fields(
                    Projections.include("_id", "nom"))));
            AggregateIterable<Document> cursor = collection.aggregate(pipeline);
            try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
                while (cursorIterator.hasNext()) {
                    entry.add(cursorIterator.next());
                }
            }
            return entry;
    }

    public static void removeCategoryFromWikiEntries(Integer idWiki, String category) {
        Document searchQuery = new Document();
        searchQuery.put("id_wiki", idWiki);
        searchQuery.put("categories", category);
        Document updateQuery = new Document("$pull", new Document("categories", category));
        collection.updateMany(searchQuery, updateQuery);
    }

    public static void removeEntriesWithNoCategories() {
        collection.deleteMany(new Document("categories", new Document("$size", 0)));
    }

    public static String modifyCategoriesNameForEntries(String oldStringCategory,int idWiki, Document setQuery){
    try{
        Document searchQuery = new Document("$and", Arrays.asList(
            Filters.eq("id_wiki", idWiki),
            Filters.eq("categories", oldStringCategory))); 
        UpdateResult result = collection.updateMany(searchQuery,setQuery);
        if (result.getModifiedCount() == 0) {
            return "404";
        }
            return "200";
        } catch (Exception e) {
            e.printStackTrace();
            return "500";
        }
    }
}