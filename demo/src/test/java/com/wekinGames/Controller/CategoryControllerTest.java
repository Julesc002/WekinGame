package com.wekinGames.Controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wekinGame.Controllers.CategoryController;
import com.wekinGame.Repository.EntryRepository;
import com.wekinGame.Repository.WikiRepository;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

	private static MockedStatic<WikiRepository> wikiMock = mockStatic(WikiRepository.class);
    private static MockedStatic<EntryRepository> entryMock = mockStatic(EntryRepository.class);

    @InjectMocks
	private CategoryController categoryController;


    @AfterAll
    public static void close() {
        wikiMock.close();
    }
	
    @Test
    public void testGetCategoriesWithIdWiki() {
        // GIVEN
        String idWiki = "1";
        List<String> expectedCategories = Arrays.asList("categorie1", "categorie2");
        Document expectedWiki = new Document();
        expectedWiki.append("_id", idWiki);
        expectedWiki.append("categories", expectedCategories);
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(expectedWiki);
        
        // WHEN
        List<String> obtainedCategories = categoryController.getCategoriesWithIdWiki(idWiki);
        
        // THEN
        assertEquals(expectedCategories, obtainedCategories);
    }
	
    @Test
    public void testGetCategoriesWithIdWikiCaseNoneFound() {
        // GIVEN
        String idWiki = "1";
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(null);

        // THEN
        assertThrows(NullPointerException.class, () -> categoryController.getCategoriesWithIdWiki(idWiki));
    }

    @Test
    public void testGetEntriesByCategoryAndWiki() {
        // GIVEN
        String idWiki = "1";
        String categoryName = "test";
        List<Document> expectedEntry = new ArrayList<Document>();
        Document entry = new Document();
        entry.append("_id", idWiki);
        entry.append("categories", categoryName);
        expectedEntry.add(entry);
        entryMock.when(() -> EntryRepository.getEntriesByWikiAndCategory(Integer.parseInt(idWiki), categoryName)).thenReturn(expectedEntry);
        
        // WHEN
        List<Document> obtainedEntry = categoryController.getEntriesNameWithWikiIdAndCategoryName(idWiki, categoryName);
        
        // THEN
        assertEquals(expectedEntry, obtainedEntry);
    }

    @Test
    public void testGetEntriesByCategoryAndWikiCaseNoneFound() {
        // GIVEN
        String idWiki = "1";
        String categoryName = "test";
        List<Document> expectedEntry = new ArrayList<Document>();
        entryMock.when(() -> EntryRepository.getEntriesByWikiAndCategory(Integer.parseInt(idWiki), categoryName)).thenReturn(expectedEntry);
        
        // WHEN
        List<Document> obtainedEntry = categoryController.getEntriesNameWithWikiIdAndCategoryName(idWiki, categoryName);
        
        // THEN
        assertEquals(expectedEntry, obtainedEntry);
    }
    
    @Test
    public void testAddCategory(){
        //GIVEN
        String idWiki = "1";
        Map<String,String> categoryToAdd = new HashMap<String,String>();
        categoryToAdd.put("nom", "category2");
        List<String> givenCategories = new ArrayList<String>();
        String givenCategory = "category1";
        givenCategories.add(givenCategory);
        Document givenWiki = new Document("_id", Integer.parseInt(idWiki))
                    .append("categories", givenCategories);
        List<String> expectedCategories = new ArrayList<String>();
        expectedCategories.add(givenCategory);
        expectedCategories.add(categoryToAdd.get("nom"));
        Document expectedWiki = new Document("_id", Integer.parseInt(idWiki))
                    .append("categories", expectedCategories);
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(givenWiki);

        //WHEN
        categoryController.addCategory(idWiki, categoryToAdd);

        //THEN
        wikiMock.verify(() -> WikiRepository.addCategory(Integer.parseInt(idWiki), expectedWiki), Mockito.times(1));
    }

    @Test
    public void testAddCategoryCaseAlreadyExist(){
        //GIVEN
        String idWiki = "1";
        Map<String,String> categoryToAdd = new HashMap<String,String>();
        categoryToAdd.put("nom", "category2");
        List<String> givenCategories = new ArrayList<String>();
        String givenCategory = "category1";
        givenCategories.add(givenCategory);
        givenCategories.add(categoryToAdd.get("nom"));
        Document givenWiki = new Document("_id", Integer.parseInt(idWiki))
                    .append("categories", givenCategories);
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(givenWiki);

        //WHEN
        categoryController.addCategory(idWiki, categoryToAdd);

        //THEN
        wikiMock.verify(() -> WikiRepository.addCategory(Integer.parseInt(idWiki), givenWiki), Mockito.never());
    }

    @Test
    public void deleteCategory(){
        //GIVEN
        String idWiki = "1";
        String givenCategoryName = "test";

        //WHEN
        categoryController.deleteCategory(idWiki,givenCategoryName);

        //THEN
        wikiMock.verify(() -> WikiRepository.deleteCategory(Integer.parseInt(idWiki),givenCategoryName), Mockito.times(1));
    }

    @Test
    public void testmodifyCategoryName(){
        //GIVEN
        String givenNewCategoryName = "test";
        Map<String,Object> givenOldCategoryName = new HashMap<String,Object>();
        givenOldCategoryName.put("id",1);
        givenOldCategoryName.put("categories","category1");
        String response = "200";
        Document setQuery = new Document("$set", new Document("categories.$", givenNewCategoryName));
        wikiMock.when(() -> WikiRepository.modifyCategoryNameForWikis((String) givenOldCategoryName.get("categories")
        , (Integer) givenOldCategoryName.get("id"), setQuery)).thenReturn(response);
        entryMock.when(() -> EntryRepository.modifyCategoriesNameForEntries((String) givenOldCategoryName.get("categories"),
        (Integer) givenOldCategoryName.get("id"), setQuery)).thenReturn(response);

        //WHEN
        categoryController.modifyCategoryName(givenOldCategoryName, givenNewCategoryName);

        //THEN
        wikiMock.verify(() -> WikiRepository.modifyCategoryNameForWikis((String) givenOldCategoryName.get("categories")
        , (Integer) givenOldCategoryName.get("id"), setQuery), Mockito.times(1));
        entryMock.verify(() -> EntryRepository.modifyCategoriesNameForEntries((String) givenOldCategoryName.get("categories"),
        (Integer) givenOldCategoryName.get("id"), setQuery),Mockito.times(1));
    }
}