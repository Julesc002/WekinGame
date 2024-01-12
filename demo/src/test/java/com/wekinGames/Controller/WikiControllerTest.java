package com.wekinGames.Controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wekinGame.Controllers.WikiController;
import com.wekinGame.Repository.EntryRepository;
import com.wekinGame.Repository.WikiRepository;

@ExtendWith(MockitoExtension.class)
public class WikiControllerTest {

	static MockedStatic<WikiRepository> wikiMock = mockStatic(WikiRepository.class);
    static MockedStatic<EntryRepository> entryMock = mockStatic(EntryRepository.class);
    private final int NB_CATEGORIES = 3; // 2 categories with different entries in them, one empty
    private final int NB_ENTRIES = 3;

    @InjectMocks
	WikiController wikiController;

	//@BeforeEach
	//public void init() {
	//	classUnderTest = new CalculatorServiceImpl(calculator, solutionFormatter);
	//}
	
    @AfterEach
    public void clearInvocations() {
        wikiMock.clearInvocations();
        entryMock.clearInvocations();
    }
	
    @AfterAll
    public static void close() {
        wikiMock.close();
        entryMock.close();
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
    public void testGetAllWikisCaseNotFound() {
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
    public void testGetWikiByIdCaseNotFound() {
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
    public void testGetTenWikisByPrefixCaseNotFound() {
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
    public void testGetContentForOneWikiAsAdmin() throws Exception { // TODO one test as admin and one test as non-admin
        // GIVEN
        String idUser = "1";
        String idWiki = "1";
        List<Document> admins = setupAdmins(Integer.parseInt(idUser));
        List<String> categories = setupCategories();
        List<Document> entries = setupEntries(categories);
        Document wiki = setupWiki(idWiki, idUser, admins, categories);
        Document expectedContent = setUpExpectedContent(idWiki, idUser, admins, categories, entries, true);
        entryMock.when(() -> EntryRepository.getEntriesByIdWiki(Integer.parseInt(idWiki))).thenReturn(entries);
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(wiki);
        wikiMock.when(() -> WikiRepository.getAdminsByWikiId(Integer.parseInt(idWiki))).thenReturn(admins);
        
        // WHEN
        Document obtainedWiki = wikiController.getContentForOneWiki(idWiki,idUser);
        
        // THEN
        assertEquals(expectedContent, obtainedWiki);
    }

    @Test
    public void testGetContentForOneWikiNotAdmin() throws Exception { // TODO one test as admin and one test as non-admin
        // GIVEN
        String idUser = "1";
        String idWiki = "1";
        List<Document> admins = setupAdmins(2);
        List<String> categories = setupCategories();
        List<Document> entries = setupEntries(categories);
        Document wiki = setupWiki(idWiki, idUser, admins, categories);
        Document expectedContent = setUpExpectedContent(idWiki, idUser, admins, categories, entries, false);
        entryMock.when(() -> EntryRepository.getEntriesByIdWiki(Integer.parseInt(idWiki))).thenReturn(entries);
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(wiki);
        wikiMock.when(() -> WikiRepository.getAdminsByWikiId(Integer.parseInt(idWiki))).thenReturn(admins);
        
        // WHEN
        Document obtainedWiki = wikiController.getContentForOneWiki(idWiki,idUser);
        
        // THEN
        assertEquals(expectedContent, obtainedWiki);
    }

    @Test
    public void testGetContentForOneWikiCaseNotFound() { // TODO one test as admin and one test as non-admin
        // GIVEN
        String idUser = "1";
        String idWiki = "1";
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(null);
        
        // WHEN
        assertThrows(Exception.class, () -> wikiController.getContentForOneWiki(idWiki,idUser));
        
        // THEN
        entryMock.verify(() -> EntryRepository.getEntriesByIdWiki(Integer.parseInt(idWiki)), Mockito.never());
        wikiMock.verify(() -> WikiRepository.getAdminsByWikiId(Integer.parseInt(idWiki)), Mockito.never());
    }

    @Test
    public void testGetBackGroundImage(){
        //GIVEN
        String idWiki = "1";
        Document expectedImage = new Document();
        expectedImage.append("url", "img");
        Document sentImage = new Document();
        sentImage.append("imageBackground", "img");
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(sentImage);
        
        // WHEN
        Document obtainedImage = wikiController.getBackgroundImage(idWiki);

        // THEN
        assertEquals(expectedImage, obtainedImage);
    }

    @Test
    public void testGetBackGroundImageCaseNotFound(){
        //GIVEN
        String idWiki = "1";
        Document expectedImage = new Document();
        expectedImage.append("url", "");
        Document sentImage = new Document();
        sentImage.append("imageBackground", "");
        wikiMock.when(() -> WikiRepository.getById(Integer.parseInt(idWiki))).thenReturn(sentImage);
        
        // WHEN
        Document obtainedImage = wikiController.getBackgroundImage(idWiki);

        // THEN
        assertEquals(expectedImage, obtainedImage);
    }

    @Test
    public void testGetAdmins(){
        //GIVEN
        int idWiki = 1;
        List<Document> expectedAdmins = new ArrayList<Document>();
        Document expectedAdmin = new Document();
        for(int idAdmin = 1; idAdmin <= 3;idAdmin++){
            expectedAdmin.append("pseudo", String.format("test",idAdmin));
            expectedAdmins.add(expectedAdmin);
        }
        wikiMock.when(() -> WikiRepository.getAdminsByWikiId(idWiki)).thenReturn(expectedAdmins);

        //WHEN
        List<Document> obtainedAdmins = wikiController.getAdmins(idWiki);

        //THEN
        assertEquals(expectedAdmins, obtainedAdmins);
    }

    @Test
    public void testGetAdminsCaseNotFound(){
        //GIVEN
        int idWiki = 1;
        List<Document> expectedAdmins = new ArrayList<Document>();
        wikiMock.when(() -> WikiRepository.getAdminsByWikiId(idWiki)).thenReturn(expectedAdmins);

        //WHEN
        List<Document> obtainedAdmins = wikiController.getAdmins(idWiki);

        //THEN
        assertEquals(expectedAdmins, obtainedAdmins);
    }

    @Test
    public void testPatchBackGroundImage(){
        //GIVEN
        String idWiki = "1";
        Map<String,String> image = new HashMap<String,String>();
        image.put("image", "test");
        Document imageSent = new Document("$set",new Document("imageBackground", image.get("image")));

        //WHEN
        wikiController.patchBackgroundImage(idWiki, image);

        //THEN
        wikiMock.verify(() -> WikiRepository.updateBackgroundImage(Integer.parseInt(idWiki), imageSent), Mockito.times(1));
    }

    @Test
    public void testPatchBackGroundImageCaseWikiNotFound(){
        //GIVEN
        String idWiki = "1";
        Map<String,String> image = new HashMap<String,String>();
        Document imageSent = new Document("$set",new Document("imageBackground", image.get("image")));

        //WHEN
        wikiController.patchBackgroundImage(idWiki, image);

        //THEN
        wikiMock.verify(() -> WikiRepository.updateBackgroundImage(Integer.parseInt(idWiki), imageSent), Mockito.times(1));
    }

    @Test
    public void testCreateWiki(){
        //GIVEN
        int id = 3;
        List<Integer> admins = new ArrayList<Integer>();
            admins.add(Integer.valueOf("1"));
        List<String> categories = new ArrayList<String>();
        DateTimeFormatter patternJour = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String date = "" + LocalDate.now().format(patternJour);
        Document newWiki = new Document("_id", id+1)
                    .append("nom", "test")
                    .append("description", "test")
                    .append("owner", 1)
                    .append("admins", admins)
                    .append("categories", categories)
                    .append("imageBackground", "test")
                    .append("date_creation", date);
        Map<String,String> data = new HashMap<String,String>();
        data.put("nom","test");
        data.put("description","test");
        data.put("adminId","1");
        data.put("imageBackground","test");
        wikiMock.when(() -> WikiRepository.getMaxId()).thenReturn(id);

        //WHEN
        wikiController.createWiki(data);

        //THEN
        wikiMock.verify(() -> WikiRepository.push(newWiki), Mockito.times(1));
    }

    @Test
    public void testAddAdminOnWikis(){
        //GIVEN
        String idWiki = "3";
        int idAdmin = 3;
        Map<String,String> admin = new HashMap<String,String>();
        admin.put("pseudo","test");
        wikiMock.when(() -> WikiRepository.getIdAdminByPseudo(admin.get("pseudo"))).thenReturn(idAdmin);

        //WHEN
        wikiController.addAdminOnWikis(admin, idWiki);

        //THEN
        wikiMock.verify(() -> WikiRepository.addAdminToWiki(idAdmin,Integer.parseInt(idWiki)), Mockito.times(1));
    }

    @Test
    public void testRemoveAdmin(){
        //GIVEN
        String idWiki = "3";
        int idAdmin = 3;
        Map<String,String> admin = new HashMap<String,String>();
        admin.put("pseudo","test");
        wikiMock.when(() -> WikiRepository.getIdAdminByPseudo(admin.get("pseudo"))).thenReturn(idAdmin);

        //WHEN
        wikiController.removeAdmin(admin, idWiki);

        //THEN
        wikiMock.verify(() -> WikiRepository.removeAdminFromWiki(idAdmin,Integer.parseInt(idWiki)), Mockito.times(1));
    }

    @Test
    public void testRemoveWiki(){
        //GIVEN
        String idWiki = "3";
        Map<String,Integer> user = new HashMap<String,Integer>();
        user.put("id",3);
        wikiMock.when(() -> WikiRepository.isOwnerByWikiId(Integer.parseInt(idWiki), user.get("id"))).thenReturn(true);
        //WHEN
        wikiController.removeWiki(idWiki,user);

        //THEN
        wikiMock.verify(() -> WikiRepository.deleteWiki(Integer.parseInt(idWiki)), Mockito.times(1));
    }

    @Test
    public void testRemoveWikiCaseUnauthorized(){
        //GIVEN
        String idWiki = "3";
        Map<String,Integer> user = new HashMap<String,Integer>();
        user.put("id",3);
        wikiMock.when(() -> WikiRepository.isOwnerByWikiId(Integer.parseInt(idWiki), user.get("id"))).thenReturn(false);

        //WHEN
        wikiController.removeWiki(idWiki,user);

        //THEN
        wikiMock.verify(() -> WikiRepository.deleteWiki(Integer.parseInt(idWiki)), Mockito.never());
    }

    private List<Document> setupAdmins(int idAdmin) {
        Document adminData = new Document();
        adminData.append("_id", idAdmin);
        Document admin = new Document();
        admin.append("adminsdata", adminData);
        List<Document> admins = Arrays.asList(admin);
        return admins;
    }

    private List<String> setupCategories() {
        List<String> categories = new ArrayList<>();
        for (int i = 1; i <= NB_CATEGORIES; i++) {
            categories.add(String.format("categorie%d", i));
        }
        return categories;
    }

    private List<Document> setupEntries(List<String> categories) {
        List<Document> entries = new ArrayList<>();
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
        return entries;
    }

    private Document setupWiki(
        String idWiki,
        String idOwner,
        List<Document> admins,
        List<String> categories
    ) {
        Document wiki = new Document();
        wiki.append("_id", Integer.parseInt(idWiki));
        wiki.append("nom","nom");
        wiki.append("date_creation","09/01/2024");
        wiki.append("description", "test");
        wiki.append("owner",Integer.parseInt(idOwner));
        wiki.append("admins", admins);
        wiki.append("categories", categories);
        return wiki;
    }

    private Document setUpExpectedContent(
        String idWiki,
        String idOwner,
        List<Document> admins,
        List<String> categories,
        List<Document> entries,
        boolean addEmptyCategories
    ) {
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
            if ((!categoryEntries.isEmpty()) || (addEmptyCategories)) {
                category.append("entrees", categoryEntries);
                expectedCategoriesWithEntries.add(category);
            }
        }
        Document expectedContent = new Document();
        expectedContent.append("_id", Integer.parseInt(idWiki));
        expectedContent.append("nom","nom");
        expectedContent.append("date_creation","09/01/2024");
        expectedContent.append("description", "test");
        expectedContent.append("owner",Integer.parseInt(idOwner));
        expectedContent.append("admins", admins);
        expectedContent.append("categories", expectedCategoriesWithEntries);
        return expectedContent;
    }
}