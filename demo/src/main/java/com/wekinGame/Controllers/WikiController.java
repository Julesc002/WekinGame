package com.wekinGame.Controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.result.UpdateResult;
import com.wekinGame.Repository.EntryRepository;
import com.wekinGame.Repository.WikiRepository;

@RestController
public class WikiController {

    @GetMapping("/wikis")
    public List<Document> getAllWikis() {
        return WikiRepository.getAll();
    }

    @GetMapping("/wiki/{id}")
    public Document getWikiById(final @PathVariable("id") String id) {
        return WikiRepository.getById(Integer.parseInt(id));
    }

    @GetMapping("/search/wiki")
    public List<Document> getTenWikisByPrefix(
        final @RequestParam(value = "game") String gameNamePrefix
    ) {
        int desiredAmount = 10;
        List<Document> results = searchWikisByPrefix(gameNamePrefix);
        if (results.size() > desiredAmount) {
            results = results.subList(0, desiredAmount);
        }
        return results;
    }

    @GetMapping("/wiki/{idWiki}/content/{idUser}")
    public Document getContentForOneWiki(
        @PathVariable("idWiki") final String idWiki,
        @PathVariable("idUser") final String idUser
    ) {
        Document wiki = getWikiById(idWiki);
        List<Document> categoriesWithEntries = getCategoriesWithEntries(wiki, idUser);
        // Créer le résultat final
        Document result = new Document();
        result.put("_id", wiki.getInteger("_id"));
        result.put("nom", wiki.getString("nom"));
        result.put("date_creation", wiki.getString("date_creation"));
        result.put("description", wiki.getString("description"));
        result.put("owner", wiki.get("owner"));
        result.put("admins", wiki.get("admins"));
        result.put("categories", categoriesWithEntries);
        return result;
    }

    @PostMapping("/wiki/create")
    public Document createWiki(final @RequestBody Map<String, String> newWikiData) {
        try {
            List<Integer> admins = new ArrayList<Integer>();
            admins.add(Integer.valueOf(newWikiData.get("adminId")));
            List<String> categories = new ArrayList<String>();
            DateTimeFormatter patternJour = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String date = "" + LocalDate.now().format(patternJour);
            int id = WikiRepository.getMaxId() + 1;
            Document newWiki = new Document("_id", id)
                .append("nom", newWikiData.get("nom"))
                .append("description", newWikiData.get("description"))
                .append("owner", Integer.valueOf(newWikiData.get("adminId")))
                .append("admins", admins)
                .append("categories", categories)
                .append("date_creation", date);
            WikiRepository.push(newWiki);
            return new Document("_id", id);
        } catch (Exception e) {
            e.printStackTrace();
            return new Document("error", 500);
        }
    }
<<<<<<< demo/src/main/java/com/wekinGame/Controllers/WikiController.java
    
    @GetMapping("wiki/{id}/admin")
    public List<Document> getAdmins(final @PathVariable int id){
        return WikiRepository.getAdminsByWikiId(id);
    }

    @PutMapping("/wiki/{id}/admin/add")
    public ResponseEntity<String> addAdminOnWikis(
        final @RequestBody Map<String,String> admin ,
        final @PathVariable String idWiki
    ) {
        try{
            int idAdmin = verifyParametersAndGetIdAdmin(admin, idWiki);
            UpdateResult result = WikiRepository.addAdminToWiki(idAdmin, Integer.parseInt(idWiki));
            if (result.getModifiedCount() == 0) {
                return new ResponseEntity<>("404 Not Found", HttpStatus.NOT_FOUND);
=======

    public Integer getIdMax() {

        MongoCollection<Document> collectionEntrees = database.getCollection("wikis");

        List<Document> sortedEntries = collectionEntrees.find()
                .projection(new Document("_id", 1))
                .sort(Sorts.descending("_id"))
                .into(new ArrayList<>());
        return (Integer) sortedEntries.get(0).get("_id");

    }

    @GetMapping("wiki/{id}/admin")
    public List<Document> GetAdmins(@PathVariable String id) {
        List<Document> results = new ArrayList<>();
        List<Bson> pipeline = Arrays.asList(
                Aggregates.match(new Document("_id", Integer.parseInt(id))),
                Aggregates.lookup("users", "admins", "_id", "adminsdata"),
                Aggregates.unwind("$adminsdata"),
                Aggregates.project(Projections.fields(
                        Projections.include("adminsdata.pseudo", "adminsdata._id"))));
        MongoCollection<Document> collection = database.getCollection("wikis");
        AggregateIterable<Document> cursor = collection.aggregate(pipeline);
        try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
            while (cursorIterator.hasNext()) {
                results.add(cursorIterator.next());
            }
        }
        return results;
    }

    @PutMapping("/wiki/{id}/admin/add")
    public ResponseEntity<String> addAdminOnWikis(@RequestBody Map<String, String> admin, @PathVariable String id) {
        try {
            if (admin.get("pseudo").isEmpty() && id.isEmpty()) {
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
            }

            int idAdmin = getIdAdminByNom(admin.get("pseudo"));
            if (idAdmin == 400) {
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
            } else if (idAdmin == 500) {
                return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                MongoCollection<Document> collection = database.getCollection("wikis");
                Document setQuery = new Document("$addToSet", new Document("admins", idAdmin));

                UpdateResult result = collection.updateOne(Filters.eq("_id", Integer.parseInt((id))), setQuery);
                if (result.getModifiedCount() == 0) {
                    return new ResponseEntity<>("404 Not Found", HttpStatus.NOT_FOUND);
                }
                return new ResponseEntity<>("200 OK", HttpStatus.OK);
>>>>>>> demo/src/main/java/com/wekinGame/Controllers/WikiController.java
            }
            return new ResponseEntity<>("200 OK", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

<<<<<<< demo/src/main/java/com/wekinGame/Controllers/WikiController.java
    @PutMapping("/wiki/{id}/admin/delete")
    public ResponseEntity<String> removeAdmin(
        final @RequestBody Map<String,String> admin,
        final @PathVariable String idWiki
    ) {
        try{
            int idAdmin = verifyParametersAndGetIdAdmin(admin, idWiki);
            WikiRepository.removeAdminFromWiki(idAdmin, Integer.parseInt(idWiki));
            return new ResponseEntity<>("200 OK", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
=======
    private Integer getIdAdminByNom(String pseudo) {
        try {
            if (pseudo.isEmpty()) {
                return 400;
            }
            Document searchQuery = new Document("pseudo", pseudo);
>>>>>>> demo/src/main/java/com/wekinGame/Controllers/WikiController.java

    private List<Document> searchWikisByPrefix(final String gameNamePrefix) {
        if (gameNamePrefix.length() != 0) {
            return WikiRepository.getByNamePrefix(gameNamePrefix);
        } else {
            return new ArrayList<>();
        }
    }

    private List<Document> getCategoriesWithEntries(
        final Document wiki,
        final String idUser
    ) {
        List<Document> categories = new ArrayList<>();
        for (Map.Entry<String, List<Document>> categoryWithEntries : getCategoriesWithEntriesAsMap(wiki, idUser)) {
            Document category = new Document();
            category.put("nom", categoryWithEntries.getKey());
            category.put("entrees", categoryWithEntries.getValue());
            categories.add(category);
        }
        return categories;
    }

    private Set<Map.Entry<String, List<Document>>> getCategoriesWithEntriesAsMap(
        final Document wiki,
        final String idUser
    ) {
        List<Document> entries = EntryRepository.getEntriesByIdWiki(wiki.getInteger("_id"));
        Map<String, List<Document>> categorizedEntries = new HashMap<>();
        for (Document entry : entries) {
            List<String> entryCategories = (List<String>) entry.get("categories");
            for (String category : entryCategories) {
                if (!categorizedEntries.containsKey(category)) {
                    categorizedEntries.put(category, new ArrayList<>());
                }
                categorizedEntries.get(category).add(entry);
            }
<<<<<<< demo/src/main/java/com/wekinGame/Controllers/WikiController.java
=======

            return (Integer) resultsQuery.get("_id");

        } catch (Exception e) {
            e.printStackTrace();
            return 500;
>>>>>>> demo/src/main/java/com/wekinGame/Controllers/WikiController.java
        }
        if (isAdmin(wiki.getInteger("_id"), idUser)) {
            categorizedEntries = addCategoriesWithoutEntry(wiki, categorizedEntries);
        }
        return categorizedEntries.entrySet();
    }

<<<<<<< demo/src/main/java/com/wekinGame/Controllers/WikiController.java
    private boolean isAdmin(
        final int idWiki,
        final String idUser
    ) {
        for (Document admin : getAdmins(idWiki)) {
            if (admin.get("adminsdata._id") == idUser) {
                return true;
=======
    @PutMapping("/wiki/{id}/admin/delete")
    public ResponseEntity<String> deleteEntry(@RequestBody Map<String, String> admin, @PathVariable String id) {
        try {
            if (admin.get("pseudo").isEmpty() && id.isEmpty()) {
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
>>>>>>> demo/src/main/java/com/wekinGame/Controllers/WikiController.java
            }
        }
        return false;
    }

<<<<<<< demo/src/main/java/com/wekinGame/Controllers/WikiController.java
    private Map<String, List<Document>> addCategoriesWithoutEntry(
        final Document wiki,
        final Map<String, List<Document>> categoriesWithEntryOnly
    ) {
        Map<String, List<Document>> categories = categoriesWithEntryOnly;
        for (String category : (List<String>) wiki.get("categories")) {
            if (!categoriesWithEntryOnly.containsKey(category)) {
                categories.put(category, new ArrayList<>());
=======
            int idAdmin = getIdAdminByNom(admin.get("pseudo"));
            if (idAdmin == 400) {
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
            } else if (idAdmin == 500) {
                return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                MongoCollection<Document> collection = database.getCollection("wikis");
                collection.updateOne(Filters.eq("_id", Integer.parseInt(id)), Updates.pull("admins", idAdmin));

                return new ResponseEntity<>("200 OK", HttpStatus.OK);
>>>>>>> demo/src/main/java/com/wekinGame/Controllers/WikiController.java
            }
        }
        return categories;
    }

    private int verifyParametersAndGetIdAdmin(
        final Map<String,String> admin,
        final String id
    ) throws Exception {
        String pseudo = admin.get("pseudo");
        if (pseudo.isEmpty() && id.isEmpty()) {
            throw new Exception("400 bad request");
        }
        return WikiRepository.getIdAdminByPseudo(pseudo);
    }
}