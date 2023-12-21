package com.wekinGame.Controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wekinGame.Repository.EntryRepository;
import com.wekinGame.Repository.WikiRepository;

@RestController
public class WikiController {

    @GetMapping("/wiki/{id}")
    public Document getWikiById(@PathVariable("id") String id) {
        return WikiRepository.getWikiById(Integer.parseInt(id));
    }

    @GetMapping("/search/wiki")
    public List<Document> getTenWikisByPrefix(@RequestParam(value = "game") String gameNamePrefix) {
        int desiredAmount = 10;
        List<Document> results = searchWikisByPrefix(gameNamePrefix);
        if (results.size() > desiredAmount) {
            results = results.subList(0, desiredAmount);
        }
        return results;
    }

    private List<Document> searchWikisByPrefix(String gameNamePrefix) {
        if (gameNamePrefix.length() != 0) {
            return WikiRepository.getWikisByNamePrefix(gameNamePrefix);
        } else {
            return new ArrayList<>();
        }
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
        result.put("admins", wiki.get("admins"));
        result.put("categories", categoriesWithEntries);
        return result;
    }

    private List<Document> getCategoriesWithEntries(final Document wiki, final String idUser) {
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
        }
        if (isAdmin(wiki.getInteger("_id"), idUser)) {
            categorizedEntries = addCategoriesWithoutEntry(wiki, categorizedEntries);
        }
        return categorizedEntries.entrySet();
    }

    private boolean isAdmin(final int idWiki, final String idUser) {
        for (Document admin : getAdmins(idWiki)) {
            if (admin.get("adminsdata._id") == idUser) {
                return true;
            }
        }
        return false;
    }

    private Map<String, List<Document>> addCategoriesWithoutEntry(
        final Document wiki,
        final Map<String, List<Document>> categoriesWithEntryOnly
    ) {
        Map<String, List<Document>> categories = categoriesWithEntryOnly;
        for (String category : (List<String>) wiki.get("categories")) {
            if (!categoriesWithEntryOnly.containsKey(category)) {
                categories.put(category, new ArrayList<>());
            }
        }
        return categories;
    } /*

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
    public Document createWiki(@RequestBody Map<String, String> newWikiData) {
        try {
            MongoCollection<Document> collection = database.getCollection("wikis");
            List<Integer> admins = new ArrayList<Integer>();
            admins.add(Integer.valueOf(newWikiData.get("adminId")));
            List<String> categories = new ArrayList<String>();
            DateTimeFormatter patternJour = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String date = "" + LocalDate.now().format(patternJour);
            int id = getIdMax() + 1;
            Document dataToTransfer = new Document("_id", id)
                    .append("nom", newWikiData.get("nom"))
                    .append("description", newWikiData.get("description"))
                    .append("admins", admins)
                    .append("categories", categories)
                    .append("date_creation", date);
            System.out.println(dataToTransfer);
            collection.insertOne(dataToTransfer);
            // return new ResponseEntity<>("200 OK "+id, HttpStatus.OK);
            return new Document("_id", id);
        } catch (Exception e) {
            e.printStackTrace(); // Affichez l'erreur dans la console pour le débogage.
            // return new ResponseEntity<>("500 Internal Server Error",
            // HttpStatus.INTERNAL_SERVER_ERROR);
            return new Document("error", 500);
        }
    }

    public Integer getIdMax() {
        MongoCollection<Document> collectionEntrees = database.getCollection("wikis");
        List<Document> sortedEntries = collectionEntrees.find()
                .projection(new Document("_id", 1))
                .sort(Sorts.descending("_id"))
                .into(new ArrayList<>());
        return (Integer) sortedEntries.get(0).get("_id");
    } */
    
    @GetMapping("wiki/{id}/admin")
    public List<Document> getAdmins(@PathVariable int id){
        return WikiRepository.getAdminsByWikiId(id);
    } /*

    @PutMapping("/wiki/{id}/admin/add")
    public ResponseEntity<String> addAdminOnWikis(@RequestBody Map<String,String> admin , @PathVariable String id) {
        try{
            if (admin.get("pseudo").isEmpty() && id.isEmpty()) {
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
            }
            int idAdmin = getIdAdminByNom(admin.get("pseudo"));
            if (idAdmin == 400){
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
            }else if(idAdmin == 500){
                return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            else{
                MongoCollection<Document> collection = database.getCollection("wikis");
                Document setQuery = new Document("$addToSet",new Document("admins",idAdmin));
                
                UpdateResult result = collection.updateOne(Filters.eq("_id",Integer.parseInt((id))),setQuery);
                if (result.getModifiedCount() == 0) {
                    return new ResponseEntity<>("404 Not Found", HttpStatus.NOT_FOUND);
                }
                return new ResponseEntity<>("200 OK", HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Integer getIdAdminByNom(String pseudo){
        try{
            if (pseudo.isEmpty()) {
                return 400;
            }
            Document searchQuery = new Document("pseudo",pseudo);
            MongoCollection<Document> collection = database.getCollection("users");
            FindIterable<Document> cursor = collection.find(searchQuery);
            Document resultsQuery = new Document();
            try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
                while (cursorIterator.hasNext()) {
                    resultsQuery = cursorIterator.next();
                }
            }
        return (Integer) resultsQuery.get("_id");
        } catch (Exception e) {
            e.printStackTrace();
            return 500;
        }
    }

    @PutMapping("/wiki/{id}/admin/delete")
    public ResponseEntity<String> deleteEntry(@RequestBody Map<String,String> admin, @PathVariable String id) {
        try{
            if (admin.get("pseudo").isEmpty() && id.isEmpty()) {
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
            }
            int idAdmin = getIdAdminByNom(admin.get("pseudo"));
            if (idAdmin == 400){
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
            }else if(idAdmin == 500){
                return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            else{
                MongoCollection<Document> collection = database.getCollection("wikis");
                collection.updateOne(Filters.eq("_id", Integer.parseInt(id)), Updates.pull("admins",idAdmin));
                
                return new ResponseEntity<>("200 OK", HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    } */
}