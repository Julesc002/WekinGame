package com.wekinGame.Controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

@RestController
public class WikiController {

    MongoClient mongoClient = MongoClients.create("mongodb+srv://gamer:ratio@bdwekingame.decr9eq.mongodb.net/");
    MongoDatabase database = mongoClient.getDatabase("WekinGame");
    MongoCollection<Document> collection = database.getCollection("wikis");

    @GetMapping("/wiki/{id}")
    public Document getWikiWithId(@PathVariable("id") String id) {
        Document searchQuery = new Document();
        searchQuery.put("_id", Integer.parseInt(id));

        Document result = collection.find(searchQuery).first();

        return result;
    }

    @GetMapping("/search/wiki")
    public List<Document> getWikisByPrefix(@RequestParam(value = "game") String game) {
        List<Document> results = searchWikisByPrefix(game);
        if (results.size() > 10) {
            results = results.subList(0, 10);
        }
        return results;
    }

    private List<Document> searchWikisByPrefix(String prefix) {
        List<Document> results = new ArrayList<>();
        if (prefix.length() == 0) {
            return results;
        } else {

            Document searchQuery = new Document();
            searchQuery.put("nom", new Document("$regex", prefix).append("$options", "i"));

            FindIterable<Document> cursor = collection.find(searchQuery);

            try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
                while (cursorIterator.hasNext()) {
                    results.add(cursorIterator.next());
                }
            }

            return results;
        }
    }

    @GetMapping("/wiki/{id}/content")
    public Document getContentForOneWiki(@PathVariable("id") String id) {
        Document wiki = getWikiWithId(id);

        Document searchQuery = new Document();
        searchQuery.put("id_wiki", Integer.parseInt(id));

        MongoCollection<Document> collectionEntrees = database.getCollection("entrees");
        FindIterable<Document> cursor = collectionEntrees.find(searchQuery);

        List<Document> entries = new ArrayList<>();
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                entries.add(cursorIterator.next());
            }
        }

        // Créer une structure pour organiser les entrées par catégorie
        Map<String, List<Document>> categorizedEntries = new HashMap<>();

        for (Document entry : entries) {
            List<String> entryCategories = (List<String>) entry.get("categories");
            for (String category : entryCategories) {
                if (!categorizedEntries.containsKey(category)) {
                    categorizedEntries.put(category, new ArrayList<>());
                }
                categorizedEntries.get(category).add(entry);
            }
        }

        // Créer la liste d'objets pour la clé "categories"
        List<Document> categoryList = new ArrayList<>();
        for (Map.Entry<String, List<Document>> entry : categorizedEntries.entrySet()) {
            Document categoryObject = new Document();
            String categoryName = entry.getKey();
            categoryObject.put("nom", categoryName);
            categoryObject.put("entrees", entry.getValue());
            categoryList.add(categoryObject);
        }

        // Créer le résultat final
        Document result = new Document();
        result.put("_id", wiki.getInteger("_id"));
        result.put("nom", wiki.getString("nom"));
        result.put("date_creation", wiki.getString("date_creation"));
        result.put("description", wiki.getString("description"));
        result.put("admins", wiki.get("admins"));
        result.put("categories", categoryList);

        return result;
    }

    @GetMapping("/wikis")
    public List<Document> getAllWikis() {
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

    @PostMapping("/wiki/create")
    public Document createWiki(@RequestBody Map<String,String> newWikiData) {
        try {
            MongoCollection<Document> collection = database.getCollection("wikis");
            List<Integer> admins = new ArrayList<Integer>();
            admins.add(Integer.valueOf(newWikiData.get("adminId")));
            List<String> categories = new ArrayList<String>();
            DateTimeFormatter patternJour = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String date =""+LocalDate.now().format(patternJour);
            int id =getIdMax()+1;

            Document dataToTransfer = new Document("_id",id)
                    .append("nom", newWikiData.get("nom"))
                    .append("description", newWikiData.get("description"))
                    .append("admins", admins)
                    .append("categories", categories)
                    .append("date_creation", date);

            System.out.println(dataToTransfer);

            collection.insertOne(dataToTransfer);
            // return new ResponseEntity<>("200 OK "+id, HttpStatus.OK);
            return new Document("_id",id);

        } catch (Exception e) {
            e.printStackTrace(); // Affichez l'erreur dans la console pour le débogage.
            // return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
            return new Document("error",500);
        }
    }

    public Integer getIdMax() {

        MongoCollection<Document> collectionEntrees = database.getCollection("wikis");

        List<Document> sortedEntries = collectionEntrees.find()
                .projection(new Document("_id", 1))
                .sort(Sorts.descending("_id"))
                .into(new ArrayList<>());
        return (Integer) sortedEntries.get(0).get("_id");

    }
    
    @GetMapping("wiki/{id}/admin")
    public List<Document> GetAdmins(@PathVariable String id){
        List<Document> results = new ArrayList<>();
        List<Bson> pipeline = Arrays.asList(
            Aggregates.match(new Document("_id",Integer.parseInt(id))),
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
}