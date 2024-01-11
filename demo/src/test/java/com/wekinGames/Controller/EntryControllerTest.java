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

import com.wekinGame.Controllers.EntryController;
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
}
