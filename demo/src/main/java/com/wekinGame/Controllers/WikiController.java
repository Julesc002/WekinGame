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
    public Document getWikiById(@PathVariable("id") final String id) {
        return WikiRepository.getById(Integer.parseInt(id));
    }

    @GetMapping("/search/wiki")
    public List<Document> getTenWikisByPrefix(@RequestParam(value = "game") final String gameNamePrefix) {
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
    
    @GetMapping("wiki/{id}/admin")
    public List<Document> getAdmins(@PathVariable int id){
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
            }
            return new ResponseEntity<>("200 OK", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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
        }
        if (isAdmin(wiki.getInteger("_id"), idUser)) {
            categorizedEntries = addCategoriesWithoutEntry(wiki, categorizedEntries);
        }
        return categorizedEntries.entrySet();
    }

    private boolean isAdmin(
        final int idWiki,
        final String idUser
    ) {
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
    }

    private int verifyParametersAndGetIdAdmin(Map<String,String> admin, String id) throws Exception {
        String pseudo = admin.get("pseudo");
        if (pseudo.isEmpty() && id.isEmpty()) {
            throw new Exception("400 bad request");
        }
        return WikiRepository.getIdAdminByPseudo(pseudo);
    }
}