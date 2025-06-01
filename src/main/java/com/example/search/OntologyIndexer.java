package com.example.search;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class OntologyIndexer {

    private static final String ONTOLOGY_PATH = "src/main/resources/food.ttl";
    private static final String INDEX_DIR = "lucene-index";

    public static void main(String[] args) throws IOException {
        OntModel model = ModelFactory.createOntologyModel();
        model.read(new File(ONTOLOGY_PATH).toURI().toString(), "TURTLE");

        FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(dir, config);

        String NS = "http://www.semanticweb.org/assaf/ontologies/food";
        Property nameProp = model.getProperty(NS + "/Name");
        Property imageProp = model.getProperty(NS + "/imageUrl");
        Property subclassProp = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

        AtomicInteger count = new AtomicInteger(0);

        model.listIndividuals().forEachRemaining(ind -> {
            System.out.println(nameProp);
            StmtIterator nameIt = ind.listProperties(nameProp);
            StmtIterator imageIt = ind.listProperties(imageProp);
            StmtIterator subclassIt = ind.listProperties(subclassProp);
            System.out.println("🔎 Individu trouvé : " + ind.getURI());

            ind.listProperties().forEachRemaining(stmt -> {
                System.out.println("   → " + stmt.getPredicate().getURI() + " = " + stmt.getObject());
            });

            if (nameIt.hasNext()) {
                
                String name = nameIt.nextStatement().getObject().toString();
                String image = imageIt.hasNext() ? imageIt.nextStatement().getObject().toString() : "";
                String subclass = subclassIt.hasNext() ? subclassIt.nextStatement().getObject().toString() : "";

                System.out.println("✔ Indexing: " + name);

                Document doc = new Document();
                doc.add(new TextField("name", name, Field.Store.YES));
                doc.add(new StringField("uri", ind.getURI(), Field.Store.YES));
                doc.add(new StoredField("image", image));
                doc.add(new StoredField("category", subclass));

                try {
                    writer.addDocument(doc);
                } catch (IOException e) {
                    System.err.println("❌ Erreur Lucene : " + e.getMessage());
                }

                count.incrementAndGet();
            } else {
                System.out.println("⚠️ Aucun nom trouvé pour : " + ind.getURI());
            }
        });

        writer.close();
        System.out.println("✅ Indexation terminée avec succès.");
        System.out.println("📄 Nombre total de documents indexés : " + count.get());
    }
}
