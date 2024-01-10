package com.wekinGames.Controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wekinGame.Controllers.WikiController;
import com.wekinGame.Repository.EntryRepository;
import com.wekinGame.Repository.WikiRepository;

@ExtendWith(MockitoExtension.class)
public class WikiControllerTest {

	static MockedStatic<WikiRepository> wikiMock = mockStatic(WikiRepository.class);
    static MockedStatic<EntryRepository> entryMock = mockStatic(EntryRepository.class);

    @InjectMocks
	WikiController wikiController;

	//@BeforeEach
	//public void init() {
	//	classUnderTest = new CalculatorServiceImpl(calculator, solutionFormatter);
	//}
	
    @AfterAll
    public static void close() {
        wikiMock.close();
    }

    @Test
    public void testGetAllWikis() {
        // GIVEN
        String name = "test%d";
        List<Document> expectedWikis = new ArrayList<Document>();
        for(int idWiki = 1; idWiki < 3; idWiki++){
            String nameWiki = String.format(name,idWiki);
            Document expectedWiki = new Document();
            expectedWiki.append("_id", idWiki);
            expectedWiki.append("nom",nameWiki);
        }
        wikiMock.when(() -> WikiRepository.getAll()).thenReturn(expectedWikis);
        
        // WHEN
        List<Document> obtainedWikis = wikiController.getAllWikis();

        // THEN
        assertEquals(expectedWikis, obtainedWikis);
    }

    @Test
    public void testGetAllWikisCaseNoneFound() {
        // GIVEN
        List<Document> expectedWikis = new ArrayList<Document>();
        wikiMock.when(() -> WikiRepository.getAll()).thenReturn(new ArrayList<Document>());
        
        // WHEN
        List<Document> obtainedWikis = wikiController.getAllWikis();

        // THEN
        assertEquals(expectedWikis, obtainedWikis);
    }

    @Test
    public void testGetWikiById() {
        // GIVEN
        String idWiki = "1";
        String nameWiki = "test";
        Document expectedWiki = new Document();
        expectedWiki.append("_id", Integer.parseInt(idWiki));
        expectedWiki.append("nomm",nameWiki);
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(expectedWiki);
        
        // WHEN
        Document obtainedWiki = wikiController.getWikiById(idWiki);

        // THEN
        assertEquals(expectedWiki, obtainedWiki);
    }

    @Test
    public void testGetWikiByIdCaseNoneFound() {
        // GIVEN
        String idWiki = "1";
        Document expectedWiki = new Document();
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(new Document());
        
        // WHEN
        Document obtainedWiki = wikiController.getWikiById(idWiki);

        // THEN
        assertEquals(expectedWiki, obtainedWiki);
    }

    @Test
    public void testGetTenWikisByPrefix() {
        // GIVEN
        String prefix = "t";
        String name = "test%d";
        List<Document> expectedWikis = new ArrayList<Document>();
        for(int idWiki = 1; idWiki <= 10; idWiki++){
            String nameWiki = String.format(name,idWiki);
            Document expectedWiki = new Document();
            expectedWiki.append("_id", idWiki);
            expectedWiki.append("nom",nameWiki);
        }
        wikiMock.when(() -> WikiRepository.getByNamePrefix(prefix)).thenReturn(expectedWikis);
        
        // WHEN
        List<Document> obtainedWiki = wikiController.getTenWikisByPrefix(prefix);

        // THEN
        assertEquals(expectedWikis, obtainedWiki);
    }

    @Test
    public void testGetTenWikisByPrefixCaseNoneFound() {
        // GIVEN
        String prefix = "c";
        String name = "test%d";
        List<Document> expectedWikis = new ArrayList<Document>();
        for(int idWiki = 1; idWiki <= 10; idWiki++){
            String nameWiki = String.format(name,idWiki);
            Document expectedWiki = new Document();
            expectedWiki.append("_id", idWiki);
            expectedWiki.append("nom",nameWiki);
        }
        wikiMock.when(() -> WikiRepository.getByNamePrefix(prefix)).thenReturn(new ArrayList<Document>());
        
        // WHEN
        List<Document> obtainedWiki = wikiController.getTenWikisByPrefix(prefix);

        // THEN
        assertEquals(expectedWikis, obtainedWiki);
    }
    @Test
    public void testGetContentForOneWikiAsAdmin() { // TODO one test as admin and one test as non-admin
        // GIVEN
        String idUser = "1";
        String idWiki = "1";
        String idAdmin = "1";
        final int NB_CATEGORIES = 3; // 2 categories with different entries in them, one empty
        List<String> categories = new ArrayList<>();
        for (int i = 1; i <= NB_CATEGORIES; i++) {
            categories.add(String.format("categorie%d", i));
        }
        List<Document> entries = new ArrayList<>();
        final int NB_ENTRIES = 3;
        for (int i = 1; i <= NB_ENTRIES; i++) {
            Document entry = new Document();
            entry.append("_id", i);
            entry.append("nom", String.format("entry%d", i));
            List<String> entryCategories = new ArrayList<>();
            if (i < NB_ENTRIES) {
                // We don't want to have the last category in the first entry (for variety)
                entryCategories.add(categories.get(0));
            }
            if (i > 1) {
                // We don't want to have the first category in the last entry (for variety)
                entryCategories.add(categories.get(NB_CATEGORIES - 2));
                // We take (NB_CATEGORIES - 2) so we don't assign any entries to the last category (for variety)
            }
            entry.append("categories", entryCategories);
            entries.add(entry);
        }
        List<Document> expectedCategoriesWithEntries = new ArrayList<>();
        for (String categoryName : categories) {
            Document category = new Document();
            category.append("nom", categoryName);
            List<Document> categoryEntries = new ArrayList<>();
            for (Document entry : entries) {
                if (((List<String>) entry.get("categories")).contains(categoryName)) {
                    categoryEntries.add(entry);
                }
            }
            category.append("entrees", categoryEntries);
            expectedCategoriesWithEntries.add(category);
        }
        Document expectedWiki = new Document();
        expectedWiki.append("_id", Integer.parseInt(idWiki));
        expectedWiki.append("nom","nom");
        expectedWiki.append("date_creation","09/01/2024");
        expectedWiki.append("description", "test");
        expectedWiki.append("owner",Integer.parseInt(idUser));
        expectedWiki.append("admins", Integer.parseInt(idAdmin));
        expectedWiki.append("categories",expectedCategoriesWithEntries);
        entryMock.when(() -> EntryRepository.getEntriesByIdWiki(Integer.parseInt(idWiki))).thenReturn(entries);
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(expectedWiki);
        
        // WHEN
        Document obtainedWiki = wikiController.getContentForOneWiki(idWiki,idUser);

        // THEN
        assertEquals(expectedWiki, obtainedWiki);
    }
}