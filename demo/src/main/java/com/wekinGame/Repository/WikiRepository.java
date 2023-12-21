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
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

public class WikiRepository {

    private static MongoDatabase database = DatabaseRepository.getDatabase();
    private static MongoCollection<Document> collection = database.getCollection("wikis");

    public static Document getWikiById(int id) {
        Document searchQuery = new Document();
        searchQuery.put("_id", id);
        return collection.find(searchQuery).first();
    }

    public static List<Document> getWikisByNamePrefix(String prefix) {
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

    public static List<Document> getAdminsByWikiId(int id) {
        List<Document> results = new ArrayList<>();
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(new Document("_id", id)),
            Aggregates.lookup("users","admins","_id","adminsdata"),
            Aggregates.unwind("$adminsdata"),
            Aggregates.project(Projections.fields(
                Projections.include("adminsdata.pseudo", "adminsdata._id")
            ))
        );
        MongoCollection<Document> collection = database.getCollection("wikis");
        AggregateIterable<Document> cursor = collection.aggregate(pipeline);
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                results.add(cursorIterator.next());
            }
        }
        return results;
    }

    public static void addCategory(int id, Document wiki){
        Document searchQuery = new Document();
        searchQuery.put("_id", id);
        collection.replaceOne(searchQuery, wiki);
    }

    public static void getDeleteCategory(int idWiki, String nameCategory){
        collection.updateOne(Filters.eq("_id", idWiki), Updates.pull("categories", nameCategory));
        EntryRepository.removeCategoryFromWikiEntries(idWiki, nameCategory);
        EntryRepository.removeEntriesWithNoCategories();
    }

    public static String modifyCategoryNameForWikis(String oldStringCategory, int idWiki, Document setQuery) {
        try {
            Document searchQuery = new Document("$and", Arrays.asList(
            Filters.eq("_id", idWiki),
            Filters.eq("categories", oldStringCategory)));
            UpdateResult result = collection.updateOne(searchQuery,setQuery);
            if (result.getModifiedCount() == 0) {
                return "404";
            }
            return "200";
        } catch (Exception e) {
            e.printStackTrace();
            return "500";
        }
    }
    public static String getNomWiki(int idWiki) {
        return WikiRepository.getWikiById(idWiki).getString("nom");
    }
}