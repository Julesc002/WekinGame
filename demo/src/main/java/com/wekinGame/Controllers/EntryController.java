package com.wekinGame.Controllers;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wekinGame.Model.Entry;
import com.wekinGame.Repository.EntryRepository;

@RestController
public class EntryController {

    @GetMapping("/entry/{idEntry}")
    public Document getEntry(@PathVariable("idEntry")final String idEntry) {
        return EntryRepository.getEntry(Integer.parseInt(idEntry));
    }
    
    @GetMapping("/searchEntry")
    public List<Document> searchEntriesByName(@RequestParam(value = "nom", defaultValue = "")final String nom) {
        List<Document> results = new ArrayList<Document>();
        if (nom.length() == 0) {
            return results;
        } else {
            return EntryRepository.searchEntryByName(results,nom);
        }
    }

    @GetMapping("/searchEntryByDescription")
    public List<Document> searchEntriesByDescription(@RequestParam(value = "donnees", defaultValue = "") final String donnees) {
        List<Document> results = new ArrayList<Document>();
        if(donnees.length() != 0){
            results = EntryRepository.searchEntryByDesc(results, donnees);
        }
        return results;
    }

    @PostMapping("/create/entry")
    public ResponseEntity<String> createEntry(@RequestBody final Entry entry) {
        try {
            if (entry.getCategories().size() == 0
            && entry.getDonnees().size() == 0
            && entry.getId_wiki() < 0
            && entry.getNom() == null) {
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
            }
            List<Document> donnees = new ArrayList<Document>();
            for (int i = 0; i < entry.getDonnees().size(); i++) {
                donnees.add(new Document()
                        .append("titre", entry.getDonnees().get(i).getTitre())
                        .append("contenu", entry.getDonnees().get(i).getContenu()));
            }
            Document newEntry = new Document("_id", EntryRepository.getIdMax() + 1)
                    .append("nom", entry.getNom())
                    .append("id_wiki", entry.getId_wiki())
                    .append("categories", entry.getCategories())
                    .append("donnees", donnees);
            EntryRepository.createEntry(newEntry);
            return new ResponseEntity<>("200 OK", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // Affichez l'erreur dans la console pour le débogage.
            return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/delete/entry/{_id}")
    public void deleteEntry(@PathVariable final String idEntry) {
        EntryRepository.deleteEntry(Integer.parseInt(idEntry));
    }
    
    @PutMapping("/modify/entry/{_id}")
    public ResponseEntity<String> modifyEntry(@RequestBody final Entry entry, @PathVariable final String idEntry) {
        try {
            if (entry.getCategories().size() == 0
            && entry.getDonnees().size() == 0
            && entry.getId_wiki() < 0
            && entry.getNom() == null) {
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
            }
            List<Document> donnees = new ArrayList<Document>();
            for (int i = 0; i < entry.getDonnees().size(); i++) {
                donnees.add(new Document()
                        .append("titre", entry.getDonnees().get(i).getTitre())
                        .append("contenu", entry.getDonnees().get(i).getContenu()));
            }
            Document modifiedEntry = new Document("$set", new Document()
                    .append("nom", entry.getNom())
                    .append("id_wiki", entry.getId_wiki())
                    .append("categories", entry.getCategories())
                    .append("donnees", donnees));
            return EntryRepository.modifyEntry(Integer.parseInt(idEntry), modifiedEntry);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
