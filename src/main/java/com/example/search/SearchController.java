package com.example.search;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/search")
public class SearchController {

    @GetMapping
    public List<Map<String, String>> search(@RequestParam(required = false) String q) {
        List<Map<String, String>> results = new ArrayList<>();

        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("lucene-index")))) {
            IndexSearcher searcher = new IndexSearcher(reader);

            TopDocs topDocs;

            if (q != null && !q.isEmpty()) {
                QueryParser parser = new QueryParser("name", new StandardAnalyzer());
                Query query = parser.parse(q);
                topDocs = searcher.search(query, 10);
            } else {
                // MatchAllDocsQuery renvoie tous les documents de l'index
                Query query = new MatchAllDocsQuery();
                topDocs = searcher.search(query, 100); // Tu peux ajuster la limite ici
            }

            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                Map<String, String> result = new HashMap<>();
                result.put("name", doc.get("name"));
                result.put("uri", doc.get("uri"));
                result.put("image", doc.get("image"));
                result.put("category", doc.get("category"));
                results.add(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}
