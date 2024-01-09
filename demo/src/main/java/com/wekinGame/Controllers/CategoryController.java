package com.wekinGame.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.wekinGame.Repository.EntryRepository;
import com.wekinGame.Repository.WikiRepository;

@RestController
public class CategoryController {

    @GetMapping("/category/{idWiki}")
    public List<String> getCategoriesWithIdWiki(@PathVariable("idWiki") String idWiki) {
        return (List<String>) WikiRepository.getById(Integer.parseInt(idWiki)).get("categories");
    }
    
    @GetMapping("/category/{idWiki}/{nameCategory}")
    public List<Document> getEntriesNameWithWikiIdAndCategoryName(
        final @PathVariable("idWiki") String idWiki,
        final @PathVariable("nameCategory") String nameCategory
    ) {
        return EntryRepository.getEntriesByWikiAndCategory(Integer.parseInt(idWiki), nameCategory);
    }
    
    @PatchMapping("/wiki/{idWiki}/category/create")
    public Map<String, String> addCategory(
        final @PathVariable("id") String idWiki,
        final @RequestBody Map<String, String> newCategory
    ) {
        try{
            Document wiki = WikiRepository.getById(Integer.parseInt(idWiki));
            String nameNewCategory = newCategory.get("nom");
            List<String> wikiCategories = (List<String>) wiki.get("categories");
            Map<String, String> response = new HashMap<>();
            if (!wikiCategories.contains(nameNewCategory)) {
                wikiCategories.add(nameNewCategory);
                wiki.put("categories", wikiCategories);
                WikiRepository.addCategory(Integer.parseInt(idWiki),wiki);
                response.put("code", "200");
                response.put("message", "Catégorie ajoutée avec succès");
            } else {
                response.put("code", "409");
                response.put("message", "La catégorie existe déjà");
            }
            return response;
        }catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    @PatchMapping("/wiki/{idWiki}/{nameCategory}/delete")
    public void deleteCategory(
        final @PathVariable("idWiki") String idWiki,
        final @PathVariable("nameCategory") String nameCategory
    ) {
        WikiRepository.deleteCategory(Integer.parseInt(idWiki), nameCategory);
    }
    

    @PutMapping("/modify/category/{newCategoryName}")
    public ResponseEntity<String> modifyCategoryName(
        final @RequestBody Map<String, Object> oldCategoryName,
        final @PathVariable String newCategoryName
    ) {
        if (newCategoryName.isEmpty() && oldCategoryName.isEmpty()) {
            return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
        }else{
            Document setOldCategoryNameWithNew = new Document("$set", new Document("categories.$", newCategoryName));
            String resultModifyCategoryWikis = WikiRepository.modifyCategoryNameForWikis(
                (String) oldCategoryName.get("categories"),
                (Integer) oldCategoryName.get("id"), setOldCategoryNameWithNew);
            String resultModifyCategoryEntries = EntryRepository.modifyCategoriesNameForEntries(
                (String) oldCategoryName.get("categories"),
                (Integer) oldCategoryName.get("id"), setOldCategoryNameWithNew);
            if (resultModifyCategoryEntries.equals("404") && resultModifyCategoryWikis.equals("404")) {
                return new ResponseEntity<>("404 Not Found", HttpStatus.NOT_FOUND);
            } else if (resultModifyCategoryEntries.equals("200") && resultModifyCategoryWikis.equals("200")) {
                return new ResponseEntity<>("200 OK", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
