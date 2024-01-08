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

    public static Document getById(final int idWiki) {
        Document searchQuery = new Document();
        searchQuery.put("_id", idWiki);
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

    public static List<Document> getAdminsByWikiId(final int idWiki) {
        List<Document> results = new ArrayList<>();
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(new Document("_id", idWiki)),
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
        final int idWiki,
        final Document wiki
    ){
        Document searchQuery = new Document();
        searchQuery.put("_id", idWiki);
        collection.replaceOne(searchQuery, wiki);
    }

    public static void deleteCategory(
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
        final String oldNameCategory,
        final int idWiki,
        final Document setQuery
    ) {
        try {
            Document searchQuery = new Document("$and", Arrays.asList(
            Filters.eq("_id", idWiki),
            Filters.eq("categories", oldNameCategory)));
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
}