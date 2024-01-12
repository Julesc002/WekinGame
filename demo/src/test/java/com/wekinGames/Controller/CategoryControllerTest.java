package com.wekinGames.Controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
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
    


}