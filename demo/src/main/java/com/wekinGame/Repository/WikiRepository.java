package com.wekinGame.Repository;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

public class WikiRepository {

    private static MongoCollection<Document> collection = DatabaseRepository.getDatabase().getCollection("wikis");

    public static List<Document> getAll() {
        List<Document> results = new ArrayList<>();
        List<Bson> pipeline = Arrays.asList(
                Aggregates.project(Projections.fields(
                        Projections.include("_id", "nom"))),
                Aggregates.sort(Sorts.ascending("nom")));
        AggregateIterable<Document> cursor = collection.aggregate(pipeline);
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                results.add(cursorIterator.next());
            }
        }
        return results;
    }

    public static Document getById(final int id) {
        Document searchQuery = new Document();
        searchQuery.put("_id", id);
        return collection.find(searchQuery).first();
    }

    public static String getNomWiki(int idWiki) {
        return WikiRepository.getById(idWiki).getString("nom");
    }

    public static List<Document> getByNamePrefix(final String prefix) {
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

    public static List<Document> getAdminsByWikiId(final int id) {
        List<Document> results = new ArrayList<>();
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(new Document("_id", id)),
            Aggregates.lookup("users","admins","_id","adminsdata"),
            Aggregates.unwind("$adminsdata"),
            Aggregates.project(Projections.fields(
                Projections.include("adminsdata.pseudo", "adminsdata._id")
            ))
        );
        AggregateIterable<Document> cursor = collection.aggregate(pipeline);
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                results.add(cursorIterator.next());
            }
        }
        return results;
    }

    public static int getMaxId() {
        List<Document> sortedWiki = collection.find()
                .projection(new Document("_id", 1))
                .sort(Sorts.descending("_id"))
                .into(new ArrayList<>());
        return (Integer) sortedWiki.get(0).get("_id");
    }
    public static void push(final Document wiki) {
        collection.insertOne(wiki);
    }

    public static void addCategory(
        final int id,
        final Document wiki
    ){
        Document searchQuery = new Document();
        searchQuery.put("_id", id);
        collection.replaceOne(searchQuery, wiki);
    }

    public static void getDeleteCategory(
        final int idWiki,
        final String nameCategory
    ){
        collection.updateOne(Filters.eq(
            "_id", idWiki),
            Updates.pull("categories", nameCategory
        ));
        EntryRepository.removeCategoryFromWikiEntries(idWiki, nameCategory);
        EntryRepository.removeEntriesWithNoCategories();
    }

    public static Integer getIdAdminByPseudo(final String pseudo){
        Document searchQuery = new Document("pseudo", pseudo);
        return (Integer) collection.find(searchQuery).first().get("_id");
    }

    public static UpdateResult addAdminToWiki(
        final int idAdmin,
        final int idWiki
    ) {
        Document setQuery = new Document("$addToSet",new Document("admins", idAdmin));
        return collection.updateOne(Filters.eq("_id", idWiki), setQuery);
    }

    public static void removeAdminFromWiki(
        final int idAdmin,
        final int idWiki
    ) {
        collection.updateOne(Filters.eq("_id", idWiki), Updates.pull("admins",idAdmin));
    }

    public static String modifyCategoryNameForWikis(
        final String oldStringCategory,
        final int idWiki,
        final Document setQuery
    ) {
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

    public static ResponseEntity<String> ModifyCategoryName(
        final Map<String, Object> oldStringCategory,
        final String newCategory
    ){
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