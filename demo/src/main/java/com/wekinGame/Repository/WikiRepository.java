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

    private static MongoCollection<Document> collectionWiki = DatabaseRepository.getDatabase().getCollection("wikis");
    private static MongoCollection<Document> collectionUser = DatabaseRepository.getDatabase().getCollection("users");

    public static List<Document> getAll() {
        List<Document> results = new ArrayList<>();
        List<Bson> pipeline = Arrays.asList(
                Aggregates.project(Projections.fields(
                        Projections.include("_id", "nom"))),
                Aggregates.sort(Sorts.ascending("nom")));
        AggregateIterable<Document> cursor = collectionWiki.aggregate(pipeline);
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
        return collectionWiki.find(searchQuery).first();
    }

    public static String getNomWiki(int idWiki) {
        return WikiRepository.getById(idWiki).getString("nom");
    }

    public static List<Document> getByNamePrefix(final String prefix) {
        Document searchQuery = new Document();
        searchQuery.put("nom", new Document("$regex", prefix).append("$options", "i"));
        List<Document> wikis = new ArrayList<>();
        FindIterable<Document> cursor = collectionWiki.find(searchQuery);
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
                Aggregates.lookup("users", "admins", "_id", "adminsdata"),
                Aggregates.unwind("$adminsdata"),
                Aggregates.project(Projections.fields(
                        Projections.include("adminsdata.pseudo", "adminsdata._id"))));
        AggregateIterable<Document> cursor = collectionWiki.aggregate(pipeline);
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                results.add(cursorIterator.next());
            }
        }
        return results;
    }

    public static Boolean isOwnerByWikiId(final int idWiki, final int idUser) {
        Document searchQuery = new Document();
        searchQuery.put("_id", idWiki);
        searchQuery.put("owner", idUser);
        return (collectionWiki.find(searchQuery).first() != null);
    }

    public static void deleteWiki(final int _id) {
        collectionWiki.deleteOne(Filters.eq("_id", _id));
    }

    public static int getMaxId() {
        List<Document> sortedWiki = collectionWiki.find()
                .projection(new Document("_id", 1))
                .sort(Sorts.descending("_id"))
                .into(new ArrayList<>());
        return (Integer) sortedWiki.get(0).get("_id");
    }

    public static void push(final Document wiki) {
        collectionWiki.insertOne(wiki);
    }

    public static void addCategory(
            final int idWiki,
            final Document wiki) {
        Document searchQuery = new Document();
        searchQuery.put("_id", idWiki);
        collectionWiki.replaceOne(searchQuery, wiki);
    }

    public static void deleteCategory(
            final int idWiki,
            final String nameCategory) {
        collectionWiki.updateOne(Filters.eq(
                "_id", idWiki),
                Updates.pull("categories", nameCategory));
        EntryRepository.removeCategoryFromWikiEntries(idWiki, nameCategory);
        EntryRepository.removeEntriesWithNoCategories();
    }

    public static Integer getIdAdminByPseudo(final String pseudo) {
        Document searchQuery = new Document("pseudo", pseudo);
        return (Integer) collectionUser.find(searchQuery).first().get("_id");
    }

    public static UpdateResult addAdminToWiki(
            final int idAdmin,
            final int idWiki) {
        Document setQuery = new Document("$addToSet", new Document("admins", idAdmin));
        return collectionWiki.updateOne(Filters.eq("_id", idWiki), setQuery);
    }

    public static void removeAdminFromWiki(
            final int idAdmin,
            final int idWiki) {
        collectionWiki.updateOne(Filters.eq("_id", idWiki), Updates.pull("admins", idAdmin));
    }

    public static String modifyCategoryNameForWikis(
            final String oldNameCategory,
            final int idWiki,
            final Document setQuery) {
        try {
            Document searchQuery = new Document("$and", Arrays.asList(
                    Filters.eq("_id", idWiki),
                    Filters.eq("categories", oldNameCategory)));
            UpdateResult result = collectionWiki.updateOne(searchQuery, setQuery);
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