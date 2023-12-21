package com.wekinGame.Controllers;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wekinGame.Repository.EntryRepository;

@RestController
public class EntryController {
    
    @GetMapping("/entry/{idEntry}")
    public Document getEntry(@PathVariable("idEntry") String idEntry) {
        return EntryRepository.getEntry(Integer.parseInt(idEntry));
    }
    
    @GetMapping("/searchEntry")
    public List<Document> searchEntry(@RequestParam(value = "name", defaultValue = "") String data) {
        List<Document> results = new ArrayList<Document>();
        if (data.length() == 0) {
            return results;
        } else {
            return EntryRepository.searchEntry(results,data);
        }
    }
    /**
    @GetMapping("/searchEntryByDesc")
    public List<Document> searchEntryByDesc(@RequestParam(value = "name", defaultValue = "") String data) {
        List<Document> results = new ArrayList<Document>();
        if (data.length() == 0) {
            return results;
        } else {
            Document criteria1 = new Document("donnees.titre", new Document("$regex", data).append("$options", "i"));
            Document criteria2 = new Document("donnees.contenu", new Document("$regex", data).append("$options", "i"));
            List<Document> searchParameters = new ArrayList<>();

            searchParameters.add(criteria1);
            searchParameters.add(criteria2);
            Document searchQuery = new Document("$or", searchParameters);

            List<Bson> pipeline = Arrays.asList(
                    Aggregates.match(searchQuery), // Filtrer les documents dans la collection actuelle
                    Aggregates.lookup("wikis", "id_wiki", "_id", "wiki"), // Fusionner avec une autre collection
                    Aggregates.unwind("$wiki"), // "Déplier" le résultat de la fusion
                    Aggregates.project(Projections.fields(
                            Projections.include("_id", "nom", "categories", "wiki.nom", "wiki._id") // Sélectionner les
                                                                                                    // champs
                                                                                                    // nécessaires
                    )));

            MongoCollection<Document> collection = database.getCollection("entrees");
            AggregateIterable<Document> cursor = collection.aggregate(pipeline);

            try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
                while (cursorIterator.hasNext()) {
                    results.add(cursorIterator.next());
                }
            }
            return results;
        }
    }

    @PostMapping("/create/entry")
    public ResponseEntity<String> createEntry(@RequestBody Entry entry) {
        try {
            if (entry.getCategories().size() == 0 && entry.getDonnees().size() == 0 && entry.getId_wiki() < 0
                    && entry.getNom() == null) {
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
            }

            MongoCollection<Document> collection = database.getCollection("entrees");
            List<Document> donneesToTransfer = new ArrayList<Document>();
            for (int i = 0; i < entry.getDonnees().size(); i++) {
                donneesToTransfer.add(new Document()
                        .append("titre", entry.getDonnees().get(i).getTitre())
                        .append("contenu", entry.getDonnees().get(i).getContenu()));
            }
            Document dataToTransfer = new Document("_id", getIdMax() + 1)
                    .append("nom", entry.getNom())
                    .append("id_wiki", entry.getId_wiki())
                    .append("categories", entry.getCategories())
                    .append("donnees", donneesToTransfer);

            System.out.println(dataToTransfer);

            collection.insertOne(dataToTransfer);
            return new ResponseEntity<>("200 OK", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace(); // Affichez l'erreur dans la console pour le débogage.
            return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Integer getIdMax() {

        MongoCollection<Document> collectionEntrees = database.getCollection("entrees");

        List<Document> sortedEntries = collectionEntrees.find()
                .projection(new Document("_id", 1))
                .sort(Sorts.descending("_id"))
                .into(new ArrayList<>());
        return (Integer) sortedEntries.get(0).get("_id");

    }

    @GetMapping("/delete/entry/{_id}")
    public void deleteEntry(@PathVariable Integer _id) {
        MongoCollection<Document> collectionEntrees = database.getCollection("entrees");

        collectionEntrees.deleteOne(Filters.eq("_id", _id));

    }

    @PutMapping("/modify/entry/{_id}")
    public ResponseEntity<String> modifyEntry(@RequestBody Entry entry, @PathVariable Integer _id) {
        try {
            if (entry.getCategories().size() == 0 && entry.getDonnees().size() == 0 && entry.getId_wiki() < 0
                    && entry.getNom() == null) {
                return new ResponseEntity<>("400 Bad Request", HttpStatus.BAD_REQUEST);
            }

            MongoCollection<Document> collection = database.getCollection("entrees");
            List<Document> donneesToTransfer = new ArrayList<Document>();
            for (int i = 0; i < entry.getDonnees().size(); i++) {
                donneesToTransfer.add(new Document()
                        .append("titre", entry.getDonnees().get(i).getTitre())
                        .append("contenu", entry.getDonnees().get(i).getContenu()));
            }
            Document dataToTransfer = new Document("$set", new Document()
                    .append("nom", entry.getNom())
                    .append("id_wiki", entry.getId_wiki())
                    .append("categories", entry.getCategories())
                    .append("donnees", donneesToTransfer));

            UpdateResult result = collection.updateOne(Filters.eq("_id", _id), dataToTransfer);
            if (result.getModifiedCount() == 0) {
                return new ResponseEntity<>("404 Not Found", HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>("200 OK", HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace(); // Affichez l'erreur dans la console pour le débogage.
            return new ResponseEntity<>("500 Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }**/
}
