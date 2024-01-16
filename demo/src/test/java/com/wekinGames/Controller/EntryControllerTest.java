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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wekinGame.Controllers.EntryController;
import com.wekinGame.Model.Donnees;
import com.wekinGame.Model.Entry;
import com.wekinGame.Repository.EntryRepository;

@ExtendWith(MockitoExtension.class)
public class EntryControllerTest {

    private static MockedStatic<EntryRepository> entryMock = mockStatic(EntryRepository.class);

    @InjectMocks
	private EntryController entryController;

    @AfterAll
    public static void close() {
        entryMock.close();
    }
	
    @Test
    public void testGetEntry() {
        // GIVEN
        String idWiki = "1";
        String nomEntry = "test";
        Document expectedEntry = new Document();
        expectedEntry.append("_id", Integer.parseInt(idWiki));
        expectedEntry.append("nom", nomEntry);
        entryMock.when(() -> EntryRepository.getEntry(Integer.parseInt(idWiki))).thenReturn(expectedEntry);
        
        // WHEN
        Document obtainedEntry = entryController.getEntry(idWiki);
        
        // THEN
        assertEquals(expectedEntry, obtainedEntry);
    }

    @Test
    public void testGetEntryCaseNoneFound() {
        // GIVEN
        String idWiki = "1";
        Document expectedEntry = new Document();
        entryMock.when(() -> EntryRepository.getEntry(Integer.parseInt(idWiki))).thenReturn(expectedEntry);
        
        // WHEN
        Document obtainedEntry = entryController.getEntry(idWiki);
        
        // THEN
        assertEquals(expectedEntry, obtainedEntry);
    }

    @Test
    public void testSearchEntriesByName(){
        //GIVEN
        String nom = "test";
        List<Document> usableList = new ArrayList<Document>();
        List<Document> expectedEntries = new ArrayList<Document>();
        Document entry = new Document();
        for(int idEntry = 0; idEntry <= 3; idEntry++){
            entry.append("_id",idEntry);
            entry.append("nom",String.format("test",idEntry));
            expectedEntries.add(entry);
        }
        entryMock.when(() -> EntryRepository.searchEntriesByName(usableList, nom)).thenReturn(expectedEntries);

        //WHEN
        List<Document> obtainedEntries = entryController.searchEntriesByName(nom);

        //THEN
        assertEquals(expectedEntries, obtainedEntries);
    }

    @Test
    public void testSearchEntriesByNameCaseNoneFound(){
        //GIVEN
        String nom = "test";
        List<Document> expectedEntries = new ArrayList<Document>();
        entryMock.when(() -> EntryRepository.searchEntriesByName(expectedEntries, nom)).thenReturn(expectedEntries);

        //WHEN
        List<Document> obtainedEntries = entryController.searchEntriesByName(nom);

        //THEN
        assertEquals(expectedEntries, obtainedEntries);
    }

    @Test
    public void testSearchEntriesByDescription(){
        //GIVEN
        String data = "test1";
        List<Document> result = new ArrayList<Document>();
        List<Document> expectedEntries = new ArrayList<Document>();
        Document entry = new Document();
        List<Document> donnees = new ArrayList<Document>();
        Document donnee = new Document();
        for(int idEntry = 1;idEntry <= 3; idEntry++){
            entry.append("_id",idEntry);
            entry.append("nom",String.format("test",idEntry));
            donnee.append("titre",String.format("titre",idEntry));
            donnee.append("contenu",String.format("contenu",idEntry));
            donnees.add(donnee);
            expectedEntries.add(entry);
            expectedEntries.addAll(donnees);
        }
        entryMock.when(() -> EntryRepository.searchEntryByDesc(result, data)).thenReturn(expectedEntries);

        //WHEN
        List<Document> obtainedEntries = entryController.searchEntriesByDescription(data);

        //THEN
        assertEquals(expectedEntries, obtainedEntries);
    }

    @Test
    public void testSearchEntriesByDescriptionCaseNoneFound(){
        //GIVEN
        String data = "test";
        List<Document> expectedEntries = new ArrayList<Document>();
        entryMock.when(() -> EntryRepository.searchEntryByDesc(expectedEntries, data)).thenReturn(expectedEntries);

        //WHEN
        List<Document> obtainedEntries = entryController.searchEntriesByDescription(data);

        //THEN
        assertEquals(expectedEntries, obtainedEntries);
    }

    @Test
    public void testCreateEntry(){
        //GIVEN
        int idEntry = 1;
        int idWiki = 1;
        Entry givenEntry = new Entry();
        Donnees givenDonnee = new Donnees("testDonnees","testContenu");
        ArrayList<Donnees> givenDonnees = new ArrayList<Donnees>();
        givenDonnees.add(givenDonnee);
        List<String> givenCategories = new ArrayList<String>();
        givenCategories.add("category1");
        givenCategories.add("category2");
        givenEntry.setId(idEntry);
        givenEntry.setNom("testEntry");
        givenEntry.setId_wiki(idWiki);
        givenEntry.setCategories(givenCategories);
        givenEntry.setDonnees(givenDonnees);
        List<Document> donnees = new ArrayList<Document>();
        for (int i = 0; i < givenEntry.getDonnees().size(); i++) {
            donnees.add(new Document()
                    .append("titre", givenEntry.getDonnees().get(i).getTitre())
                    .append("contenu", givenEntry.getDonnees().get(i).getContenu()));
        }
        Document expectedEntry = new Document("_id", EntryRepository.getIdMax() + 1)
                .append("nom", givenEntry.getNom())
                .append("id_wiki", givenEntry.getId_wiki())
                .append("categories", givenEntry.getCategories())
                .append("donnees", donnees);

        //WHEN
        entryController.createEntry(givenEntry);

        //THEN
        entryMock.verify(() -> EntryRepository.createEntry(expectedEntry), Mockito.times(1));
    }

    @Test
    public void testCreateEntryCaseEmptyEntry(){
        //GIVEN
        int idEntry = 1;
        int idWiki = 1;
        Entry emptyGivenEntry = new Entry();
        ArrayList<Donnees> emptyGivenDonnees = new ArrayList<Donnees>();
        List<String> emptyGivenCategories = new ArrayList<String>();
        emptyGivenEntry.setId(idEntry);
        emptyGivenEntry.setNom("testEntry");
        emptyGivenEntry.setId_wiki(idWiki);
        emptyGivenEntry.setCategories(emptyGivenCategories);
        emptyGivenEntry.setDonnees(emptyGivenDonnees);
        Document expectedEntry = new Document();

        //WHEN
        entryController.createEntry(emptyGivenEntry);

        //THEN
        entryMock.verify(() -> EntryRepository.createEntry(expectedEntry), Mockito.never());
    }

    @Test
    public void testDeleteEntry(){
        //GIVEN
        String idEntry = "1";

        //WHEN
        entryController.deleteEntry(idEntry);

        //THEN
        entryMock.verify(() -> EntryRepository.deleteEntry(Integer.parseInt(idEntry)), Mockito.times(1));
    }

    @Test
    public void testModifyEntry(){
        String idEntry = "1";
        int idWiki = 1;
        Entry givenEntry = new Entry();
        Donnees givenDonnee = new Donnees("testDonnees","testContenu");
        ArrayList<Donnees> givenDonnees = new ArrayList<Donnees>();
        givenDonnees.add(givenDonnee);
        List<String> givenCategories = new ArrayList<String>();
        givenCategories.add("category1");
        givenCategories.add("category2");
        givenEntry.setId(Integer.parseInt(idEntry));
        givenEntry.setNom("testEntry");
        givenEntry.setId_wiki(idWiki);
        givenEntry.setCategories(givenCategories);
        givenEntry.setDonnees(givenDonnees);
        List<Document> donnees = new ArrayList<Document>();
        for (int i = 0; i < givenEntry.getDonnees().size(); i++) {
            donnees.add(new Document()
                    .append("titre", givenEntry.getDonnees().get(i).getTitre())
                    .append("contenu", givenEntry.getDonnees().get(i).getContenu()));
        }
        Document modifiedEntry = new Document("$set", new Document()
                .append("nom", givenEntry.getNom())
                .append("id_wiki", givenEntry.getId_wiki())
                .append("categories", givenEntry.getCategories())
                .append("donnees", donnees)
            );

        //WHEN
        entryController.modifyEntry(givenEntry, idEntry);

        //THEN
        entryMock.verify(() -> EntryRepository.modifyEntry(Integer.parseInt(idEntry),modifiedEntry), Mockito.times(1));
    }

    @Test
    public void testModifyEntryCaseEmptyEntry(){
        String idEntry = "1";
        int idWiki = 1;
        Entry emptyGivenEntry = new Entry();
        ArrayList<Donnees> emptyGivenDonnees = new ArrayList<Donnees>();
        List<String> emptyGivenCategories = new ArrayList<String>();
        emptyGivenEntry.setId(Integer.parseInt(idEntry));
        emptyGivenEntry.setNom("testEntry");
        emptyGivenEntry.setId_wiki(idWiki);
        emptyGivenEntry.setCategories(emptyGivenCategories);
        emptyGivenEntry.setDonnees(emptyGivenDonnees);
        Document modifiedEntry = new Document();

        //WHEN
        entryController.modifyEntry(emptyGivenEntry, idEntry);

        //THEN
        entryMock.verify(() -> EntryRepository.modifyEntry(Integer.parseInt(idEntry),modifiedEntry), Mockito.never());
    }
}
