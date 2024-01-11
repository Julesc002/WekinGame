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
    public Document getEntry(final @PathVariable("idEntry") String idEntry) {
        return EntryRepository.getEntry(Integer.parseInt(idEntry));
    }
    
    @GetMapping("/searchEntry")
    public List<Document> searchEntriesByName(
        final @RequestParam(value = "nom", defaultValue = "") String nom
    ) {
        List<Document> results = new ArrayList<Document>();
        if (nom.length() == 0) {
            return results;
        } else {
            return EntryRepository.searchEntriesByName(results,nom);
        }
    }

    @GetMapping("/searchEntryByDescription")
    public List<Document> searchEntriesByDescription(
        final @RequestParam(value = "donnees", defaultValue = "") String donnees
    ) {
        List<Document> results = new ArrayList<Document>();
        if(donnees.length() != 0){
            results = EntryRepository.searchEntryByDesc(results, donnees);
        }
        return results;
    }

    @PostMapping("/create/entry")
    public ResponseEntity<String> createEntry(final @RequestBody Entry entry) {
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

    @GetMapping("/delete/entry/{idEntry}")
    public void deleteEntry(final @PathVariable String idEntry) {
        EntryRepository.deleteEntry(Integer.parseInt(idEntry));
    }
    
    @PutMapping("/modify/entry/{idEntry}")
    public ResponseEntity<String> modifyEntry(
        final @RequestBody Entry entry,
        final @PathVariable String idEntry
    ) {
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
                    .append("contenu", entry.getDonnees().get(i).getContenu())
                );
            }
            Document modifiedEntry = new Document("$set", new Document()
                .append("nom", entry.getNom())
                .append("id_wiki", entry.getId_wiki())
                .append("categories", entry.getCategories())
                .append("donnees", donnees)
            );
            return EntryRepository.modifyEntry(Integer.parseInt(idEntry), modifiedEntry);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/switch/entry/{_id}/{_idData}/{_upOrDown}")
    public ResponseEntity<String> switchEntry(
        final @RequestBody Entry entry,
        final @PathVariable("_id") String idEntry,
        final @PathVariable("_idData") String idData,
        final @PathVariable("_upOrDown") String upOrDown) {
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
            Document echangDocument = donnees.get(Integer.parseInt(idData));
            if (upOrDown.equals("up") && Integer.parseInt(idData)>0) {
                System.out.println("salut");
                donnees.set(Integer.parseInt(idData), donnees.get(Integer.parseInt(idData)-1));
                donnees.set(Integer.parseInt(idData)-1, echangDocument);
            } else if (upOrDown.equals("down") && Integer.parseInt(idData)<(donnees.size()-1)) {
                System.out.println("salot");
                donnees.set(Integer.parseInt(idData), donnees.get(Integer.parseInt(idData)+1));
                donnees.set(Integer.parseInt(idData)+1, echangDocument);
            } else {
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
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
