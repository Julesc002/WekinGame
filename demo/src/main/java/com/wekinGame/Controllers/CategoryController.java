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

@RestController
public class CategoryController {

    @GetMapping("/category/{idWiki}")
    public List<String> getCategoryWithIdWiki(@PathVariable("idWiki") String idWiki) {
        return (List<String>) WikiRepository.getWikiById(Integer.parseInt(idWiki)).get("categories");
    }
    
    @GetMapping("/category/{idWiki}/{nameCategory}")
    public List<Document> getEntryNameWithWikiIdAndCategoryName(
        @PathVariable("idWiki") String idWiki,
        @PathVariable("nameCategory") String nameCategory
        ) {
        return EntryRepository.getEntryByWikiAndCategory(Integer.parseInt(idWiki), nameCategory);
    }
    
    @PatchMapping("/wiki/{id}/category/create")
    public Map<String, String> createCategory(@PathVariable("id") String id, @RequestBody Map<String, String> request) {
        return WikiRepository.addCategory(Integer.parseInt(id), request);
    }
    
    @PatchMapping("/wiki/{idWiki}/{nameCategory}/delete")
    public void getDeleteCategory(
        @PathVariable("idWiki") String idWiki,
        @PathVariable("nameCategory") String nameCategory
        ) {
        WikiRepository.getDeleteCategory(Integer.parseInt(idWiki), nameCategory);
    }
    

    @PutMapping("/modify/category/{newCategory}")
    public ResponseEntity<String> modifyCategoryName(
        @RequestBody Map<String, Object> oldStringCategory,
        @PathVariable String newCategory
        ) {
        if (newCategory.isEmpty() && oldStringCategory.isEmpty()) {
            return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
        }
        return WikiRepository.ModifyCategoryName(oldStringCategory, newCategory);
    }
}
