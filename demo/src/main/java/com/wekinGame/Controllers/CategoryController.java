package com.wekinGame.Controllers;

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
import com.wekinGame.ressources.HTTPCodes;

@RestController
public class CategoryController {

    @GetMapping("/category/{idWiki}")
    public List<String> getCategoriesWithIdWiki(@PathVariable("idWiki") String idWiki) {
        return (List<String>) WikiRepository.getById(Integer.parseInt(idWiki)).get("categories");
    }

    @GetMapping("/category/{idWiki}/{nameCategory}")
    public List<Document> getEntriesNameWithWikiIdAndCategoryName(
            final @PathVariable("idWiki") String idWiki,
            final @PathVariable("nameCategory") String nameCategory) {
        return EntryRepository.getEntriesByWikiAndCategory(Integer.parseInt(idWiki), nameCategory);
    }

    @PatchMapping("/wiki/{id}/category/create")
    public ResponseEntity<HTTPCodes> addCategory(
            final @PathVariable("id") String idWiki,
            final @RequestBody Map<String, String> newCategory) {
        try {
            Document wiki = WikiRepository.getById(Integer.parseInt(idWiki));
            String nameNewCategory = newCategory.get("nom");
            List<String> wikiCategories = (List<String>) wiki.get("categories");
            if (!wikiCategories.contains(nameNewCategory)) {
                wikiCategories.add(nameNewCategory);
                wiki.put("categories", wikiCategories);
                WikiRepository.addCategory(Integer.parseInt(idWiki), wiki);
                return new ResponseEntity<HTTPCodes>(HTTPCodes.OK, HttpStatus.OK);
            } else {
                return new ResponseEntity<HTTPCodes>(HTTPCodes.CONFLICT, HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<HTTPCodes>(HTTPCodes.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/wiki/{idWiki}/{nameCategory}/delete")
    public void deleteCategory(
            final @PathVariable("idWiki") String idWiki,
            final @PathVariable("nameCategory") String nameCategory) {
        WikiRepository.deleteCategory(Integer.parseInt(idWiki), nameCategory);
    }

    @PutMapping("/modify/category/{newCategoryName}")
    public ResponseEntity<HTTPCodes> modifyCategoryName(
            final @RequestBody Map<String, Object> oldCategoryName,
            final @PathVariable String newCategoryName) {
        if (newCategoryName.isEmpty() && oldCategoryName.isEmpty()) {
            return new ResponseEntity<HTTPCodes>(HTTPCodes.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        } else {
            Document setOldCategoryNameWithNew = new Document("$set", new Document("categories.$", newCategoryName));
            String resultModifyCategoryWikis = WikiRepository.modifyCategoryNameForWikis(
                    (String) oldCategoryName.get("categories"),
                    (Integer) oldCategoryName.get("id"),
                    setOldCategoryNameWithNew);
            String resultModifyCategoryEntries = EntryRepository.modifyCategoriesNameForEntries(
                    (String) oldCategoryName.get("categories"),
                    (Integer) oldCategoryName.get("id"),
                    setOldCategoryNameWithNew);
            return getResponseEntity(resultModifyCategoryWikis, resultModifyCategoryEntries);
        }
    }

    private ResponseEntity<HTTPCodes> getResponseEntity(String resultModifyCategoryWikis,
            String resultModifyCategoryEntries) {
        if (resultModifyCategoryEntries.equals("404")
                && resultModifyCategoryWikis.equals("404")) {
            return new ResponseEntity<HTTPCodes>(HTTPCodes.NOT_FOUND, HttpStatus.NOT_FOUND);
        } else if (resultModifyCategoryEntries.equals("200")
                && resultModifyCategoryWikis.equals("200")) {
            return new ResponseEntity<HTTPCodes>(HTTPCodes.OK, HttpStatus.OK);
        } else {
            return new ResponseEntity<HTTPCodes>(HTTPCodes.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
