package com.wekinGame.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    public static Map<String,String> addCategory(int id,Map<String,String> request){
        Document wiki = WikiRepository.getWikiById(id);
        Document searchQuery = new Document();
        searchQuery.put("_id", id);
        String newCategory = request.get("nom");
        List<String> categories = (List<String>) wiki.get("categories");
        Map<String, String> response = new HashMap<>();
        if (!categories.contains(newCategory)) {
            categories.add(newCategory);
            wiki.put("categories", categories);
            collection.replaceOne(searchQuery, wiki);
            response.put("code", "200");
            response.put("message", "Catégorie ajoutée avec succès");
        } else {
            response.put("code", "409");
            response.put("message", "La catégorie existe déjà");
        }
        return response;
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

    public static ResponseEntity<String> ModifyCategoryName(Map<String, Object> oldStringCategory,String newCategory){
        Document setQuery = new Document("$set", new Document("categories.$", newCategory));
        String resultWikis = WikiRepository.modifyCategoryNameForWikis(
            (String) oldStringCategory.get("categories"),
            (Integer) oldStringCategory.get("id"), setQuery);
        String resultEntries = EntryRepository.modifyCategoriesNameForEntries(
            (String) oldStringCategory.get("categories"),
            (Integer) oldStringCategory.get("id"), setQuery);
        if (resultEntries.equals("404") && resultWikis.equals("404")) {
            return new ResponseEntity<>("404 Not Found", HttpStatus.NOT_FOUND);
        } else if (resultEntries.equals("200") && resultWikis.equals("200")) {
            return new ResponseEntity<>("200 OK", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}