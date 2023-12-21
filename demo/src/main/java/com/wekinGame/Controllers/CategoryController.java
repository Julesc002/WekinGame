package com.wekinGame.Controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

@RestController
public class CategoryController {

    MongoClient mongoClient = MongoClients.create("mongodb+srv://gamer:ratio@bdwekingame.decr9eq.mongodb.net/");
    MongoDatabase database = mongoClient.getDatabase("WekinGame");

    @GetMapping("/category/{idWiki}")
    public Object getCategoryWithIdWiki(@PathVariable("idWiki") String idWiki) {
        Document searchQuery = new Document();
        searchQuery.put("_id", Integer.parseInt(idWiki));

        MongoCollection<Document> collection = database.getCollection("wikis");
        FindIterable<Document> cursor = collection.find(searchQuery);

        Document resultsQuery = new Document();
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                resultsQuery = cursorIterator.next();
            }
        }

        return resultsQuery.get("categories");
    }

    @GetMapping("/category/{idWiki}/{nameCategory}")
    public List<Document> getEntryNameWithWikiIdAndCategoryName(
        @PathVariable("idWiki") String idWiki,
        @PathVariable("nameCategory") String nameCategory
    ) {
        List<Document> resultsQuery = new ArrayList<Document>();
        Document searchQuery = new Document();
        searchQuery.put("id_wiki", Integer.parseInt(idWiki));
        searchQuery.put("categories", nameCategory);
        List<Bson> pipeline = Arrays.asList(
                Aggregates.match(searchQuery),
                Aggregates.project(Projections.fields(
                        Projections.include("_id", "nom"))));
        MongoCollection<Document> collection = database.getCollection("entrees");
        AggregateIterable<Document> cursor = collection.aggregate(pipeline);
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                resultsQuery.add(cursorIterator.next());
            }
        }
        return resultsQuery;
    }

    @PatchMapping("/wiki/{id}/category/create")
    public Map<String, String> createCategory(@PathVariable("id") String id, @RequestBody Map<String, String> request) {
        MongoCollection<Document> collectionWiki = database.getCollection("wikis");
        Document searchQuery = new Document();
        searchQuery.put("_id", Integer.parseInt(id));
        Document wiki = collectionWiki.find(searchQuery).first();

        String newCategory = request.get("nom");
        List<String> categories = (List<String>) wiki.get("categories");
        Map<String, String> response = new HashMap<>();

        if (!categories.contains(newCategory)) {
            categories.add(request.get("nom"));
            wiki.put("categories", categories);
            collectionWiki.replaceOne(searchQuery, wiki);
            response.put("code", "200");
            response.put("message", "Catégorie ajoutée avec succès");
        } else {
            response.put("code", "409");
            response.put("message", "La catégorie existe déjà");
        }

        return response;
    }

    @PatchMapping("/wiki/{idWiki}/{nameCategory}/delete")
    public void getDeleteCategory(@PathVariable("idWiki") Integer idWiki,
            @PathVariable("nameCategory") String nameCategory) {
        MongoCollection<Document> collectionWiki = database.getCollection("wikis");
        collectionWiki.updateOne(Filters.eq("_id", idWiki), Updates.pull("categories", nameCategory));
        removeCategoryFromWikiEntries(idWiki, nameCategory);
        removeEntriesWithNoCategories();
    }

    private void removeCategoryFromWikiEntries(Integer idWiki, String category) {
        MongoCollection<Document> collectionEntrees = database.getCollection("entrees");

        Document searchQuery = new Document();
        searchQuery.put("id_wiki", idWiki);
        searchQuery.put("categories", category);

        Document updateQuery = new Document("$pull", new Document("categories", category));
        collectionEntrees.updateMany(searchQuery, updateQuery);
    }

    private void removeEntriesWithNoCategories() {
        MongoCollection<Document> collectionEntrees = database.getCollection("entrees");

        Document query = new Document("categories", new Document("$size", 0));
        collectionEntrees.deleteMany(query);
    }

    @PutMapping("/modify/category/{newCategory}")
    public ResponseEntity<String> modifyCategoryName(@RequestBody Map<String, Object> oldStringCategory,
            @PathVariable String newCategory) {

        if (newCategory.isEmpty() && oldStringCategory.isEmpty()) {
            return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
        }
        Document setQuery = new Document("$set", new Document("categories.$", newCategory));
        System.out.println(oldStringCategory);
        String resultWikis = modifyCategoryNameForWikis((String) oldStringCategory.get("categories"),
                (Integer) oldStringCategory.get("id"), setQuery);
        String resultEntries = modifyCategoriesNameForEntries((String) oldStringCategory.get("categories"),
                (Integer) oldStringCategory.get("id"), setQuery);

        if (resultEntries.equals("404") && resultWikis.equals("404")) {
            return new ResponseEntity<>("404 Not Found", HttpStatus.NOT_FOUND);
        } else if (resultEntries.equals("200") && resultWikis.equals("200")) {
            return new ResponseEntity<>("200 OK", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String modifyCategoryNameForWikis(String oldStringCategory, int idWiki, Document setQuery) {
        try {
            MongoCollection<Document> collectionWiki = database.getCollection("wikis");

            Document searchQuery = new Document("$and", Arrays.asList(

            Filters.eq("_id", idWiki),
            Filters.eq("categories", oldStringCategory)));
            
            UpdateResult result = collectionWiki.updateOne(searchQuery,setQuery);

            if (result.getModifiedCount() == 0) {
                return "404";
            }

            return "200";

        } catch (Exception e) {
            e.printStackTrace();
            return "500";
        }
    }
    private String modifyCategoriesNameForEntries(String oldStringCategory,int idWiki, Document setQuery){
    try{
        MongoCollection<Document> collectionEntry = database.getCollection("entrees");

        Document searchQuery = new Document("$and", Arrays.asList(
            Filters.eq("id_wiki", idWiki),
            Filters.eq("categories", oldStringCategory)));
            
        UpdateResult result = collectionEntry.updateMany(searchQuery,setQuery);
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
