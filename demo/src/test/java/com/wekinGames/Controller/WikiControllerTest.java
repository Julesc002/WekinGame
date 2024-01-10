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
    public void testGetContentForOneWiki() {
        // GIVEN
        String idUser = "1";
        String idWiki = "1";
        String idAdmin = "2";
        
        Document expectedWiki = new Document();
        expectedWiki.append("_id", Integer.parseInt(idWiki));
        expectedWiki.append("nom","nom");
        expectedWiki.append("date_creation","09/01/2024");
        expectedWiki.append("description", "test");
        expectedWiki.append("owner",Integer.parseInt(idUser));
        expectedWiki.append("admins", Integer.parseInt(idAdmin));

        Document testWiki = new Document();
        testWiki.append("_id", 3);
        testWiki.append("nom", "test");

        List<Document> expectedContent = new ArrayList<>();
        Document category = new Document();
        category.append("nom","nom");
        category.append("entrees",testWiki);

        expectedContent.add(category);

        expectedWiki.append("categories",expectedContent);
        
        wikiMock.when(() -> EntryRepository.getEntriesByIdWiki(Integer.parseInt(idWiki))).thenReturn(expectedWiki);
        
        // WHEN
        Document obtainedWiki = wikiController.getContentForOneWiki(idWiki,idUser);

        // THEN
        assertEquals(expectedWiki, obtainedWiki);
    }
}