package com.wekinGame.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mongodb.client.AggregateIterable;0
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.UpdateResult;

public class EntryRepository {

    private static MongoDatabase database = DatabaseRepository.getDatabase();
    private static MongoCollection<Document> collection = database.getCollection("entrees");

    public static List<Document> getEntriesByIdWiki(final int id) {
        Document searchQuery = new Document();
        searchQuery.put("id_wiki", id);
        List<Document> entries = new ArrayList<>();
        List<Bson> pipeline = Arrays.asList(
                Aggregates.match(searchQuery),
                Aggregates.sort(Sorts.ascending("nom")));
        AggregateIterable<Document> cursor = collection.aggregate(pipeline);
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                entries.add(cursorIterator.next());
            }
        }
        return entries;
    }

    public static List<Document> getEntriesByWikiAndCategory(final int id_wiki, final String nameCategory) {
        List<Document> entries = new ArrayList<Document>();
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
                entries.add(cursorIterator.next());
            }
        }
        return entries;
    }

    public static void removeCategoryFromWikiEntries(final Integer idWiki, final String category) {
        Document searchQuery = new Document();
        searchQuery.put("id_wiki", idWiki);
        searchQuery.put("categories", category);
        Document updateQuery = new Document("$pull", new Document("categories", category));
        collection.updateMany(searchQuery, updateQuery);
    }

    public static void removeEntriesWithNoCategories() {
        collection.deleteMany(new Document("categories", new Document("$size", 0)));
    }

    public static String modifyCategoriesNameForEntries(final String oldStringCategory, final int idWiki,
            final Document setQuery) {
        try {
            Document searchQuery = new Document("$and", Arrays.asList(
                    Filters.eq("id_wiki", idWiki),
                    Filters.eq("categories", oldStringCategory)));
            UpdateResult result = collection.updateMany(searchQuery, setQuery);
            if (result.getModifiedCount() == 0) {
                return "404";
            }
            return "200";
        } catch (Exception e) {
            e.printStackTrace();
            return "500";
        }
    }

    public static Document getEntry(final int idEntry) {
        Document entry = getEntryById(idEntry);
        String nomWiki = WikiRepository.getNomWiki(entry.getInteger("id_wiki"));
        entry.put("nom_wiki", nomWiki);

        return entry;
    }

    private static Document getEntryById(final int idEntry) {
        Document searchQuery = new Document();
        searchQuery.put("_id", idEntry);
        Document entry = collection.find(searchQuery).first();
        return entry;
    }

    public static List<Document> searchEntryByName(final List<Document> results, final String data) {
        Document searchQuery = new Document();
        searchQuery.put("nom", new Document("$regex", data).append("$options", "i"));
        List<Bson> pipeline = Arrays.asList(
                Aggregates.match(searchQuery),
                Aggregates.lookup("wikis", "id_wiki", "_id", "wiki"),
                Aggregates.unwind("$wiki"),
                Aggregates.project(Projections.fields(
                        Projections.include("_id", "nom", "categories", "wiki.nom", "wiki._id"))));
        AggregateIterable<Document> cursor = collection.aggregate(pipeline);
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                results.add(cursorIterator.next());
            }
        }
        return results;
    }

    public static List<Document> searchEntryByDesc(final List<Document> results, final String data) {
        Document criteria1 = new Document("donnees.titre", new Document("$regex", data).append("$options", "i"));
        Document criteria2 = new Document("donnees.contenu", new Document("$regex", data).append("$options", "i"));
        List<Document> searchParameters = new ArrayList<>();
        searchParameters.add(criteria1);
        searchParameters.add(criteria2);
        Document searchQuery = new Document("$or", searchParameters);
        List<Bson> pipeline = Arrays.asList(
                Aggregates.match(searchQuery),
                Aggregates.lookup("wikis", "id_wiki", "_id", "wiki"),
                Aggregates.unwind("$wiki"),
                Aggregates.project(Projections.fields(
                        Projections.include("_id", "nom", "categories", "wiki.nom", "wiki._id"))));
        AggregateIterable<Document> cursor = collection.aggregate(pipeline);
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                results.add(cursorIterator.next());
            }
        }
        return results;
    }

    public static void createEntry(final Document entry) {
        collection.insertOne(entry);
    }

    public static Integer getIdMax() {
        List<Document> sortedEntries = collection.find()
                .projection(new Document("_id", 1))
                .sort(Sorts.descending("_id"))
                .into(new ArrayList<>());
        return (Integer) sortedEntries.get(0).get("_id");
    }

    public static void deleteEntry(final int _id) {
        collection.deleteOne(Filters.eq("_id", _id));
    }

    public static void deleteAllEntriesForOneWiki(final int id_wiki) {
        collection.deleteMany(Filters.eq("id_wiki", id_wiki));
    }

    public static ResponseEntity<String> modifyEntry(final int _id, final Document modifyEntry) {
        UpdateResult result = collection.updateOne(Filters.eq("_id", _id), modifyEntry);
        if (result.getModifiedCount() == 0) {
            return new ResponseEntity<>("404 Not Found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("200 OK", HttpStatus.OK);
    }
}