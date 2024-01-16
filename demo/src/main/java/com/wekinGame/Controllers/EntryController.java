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
import com.wekinGame.ressources.HTTPCodes;

@RestController
public class EntryController {

    @GetMapping("/entry/{idEntry}")
    public Document getEntry(final @PathVariable("idEntry") String idEntry) {
        return EntryRepository.getEntry(Integer.parseInt(idEntry));
    }

    @GetMapping("/searchEntry")
    public List<Document> searchEntriesByName(
            final @RequestParam(value = "nom", defaultValue = "") String nom) {
        List<Document> results = new ArrayList<Document>();
        if (nom.length() == 0) {
            return results;
        } else {
            return EntryRepository.searchEntriesByName(results, nom);
        }
    }

    @GetMapping("/searchEntryByDescription")
    public List<Document> searchEntriesByDescription(
            final @RequestParam(value = "donnees", defaultValue = "") String donnees) {
        List<Document> results = new ArrayList<Document>();
        if (donnees.length() != 0) {
            results = EntryRepository.searchEntryByDesc(results, donnees);
        }
        return results;
    }

    @PostMapping("/create/entry")
    public ResponseEntity<HTTPCodes> createEntry(final @RequestBody Entry entry) {
        try {
            if (entry.getCategories().size() == 0
                    && entry.getDonnees().size() == 0
                    && entry.getId_wiki() < 0
                    && entry.getNom() == null) {
                return new ResponseEntity<HTTPCodes>(HTTPCodes.BAD_REQUEST, HttpStatus.BAD_REQUEST);
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
            return new ResponseEntity<HTTPCodes>(HTTPCodes.OK, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<HTTPCodes>(HTTPCodes.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/delete/entry/{idEntry}")
    public void deleteEntry(final @PathVariable String idEntry) {
        EntryRepository.deleteEntry(Integer.parseInt(idEntry));
    }

    @PutMapping("/modify/entry/{idEntry}")
    public ResponseEntity<HTTPCodes> modifyEntry(
            final @RequestBody Entry entry,
            final @PathVariable String idEntry) {
        try {
            if (entry.getCategories().size() == 0
                    && entry.getDonnees().size() == 0
                    && entry.getId_wiki() < 0
                    && entry.getNom() == null) {
                return new ResponseEntity<HTTPCodes>(HTTPCodes.BAD_REQUEST, HttpStatus.BAD_REQUEST);
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
            return new ResponseEntity<HTTPCodes>(HTTPCodes.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
