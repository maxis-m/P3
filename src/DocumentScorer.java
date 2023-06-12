import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentScorer {

    // Compute the term frequency (TF) of a word in a document
    public static double getTermFrequency(String word, String document, HashMap<String, HashMap<String, Integer>> index, HashMap<String, Integer> wordCounts) {
        //System.out.println("TF- WORD: " + word+ ", DOC: "+document );
        int topScore = 0;
        int bottomScore = wordCounts.get(document);
        
        if(index.containsKey(word) && index.get(word).containsKey(document)){
            topScore = index.get(word).get(document);
        }
        //else{
            //System.out.println("Word not found:" + word + " Document: " + document + " Results: " + index.get(word).keySet());
        //}
       
        double frequency = (double)topScore/(double)bottomScore;
        //System.out.println(frequency);
        return frequency;
    }

    // Compute the inverse document frequency (IDF) of a word in a collection of documents
    public static double getInverseDocumentFrequency(String word, HashMap<String, HashMap<String, Integer>> index, HashMap<String, Integer> wordCounts) {
        //System.out.println("IDF- WORD: " + word);
        double score = 0;
        if(index.keySet().contains(word)){
            score = Math.log((double) wordCounts.keySet().size() / index.get(word).keySet().size());
        }
        //System.out.println("IDF, WORD: " + word+ " SCORE: " + score);
        return score;
    }

    // Score a document based on a query using TF-IDF scoring
    public static HashMap<String, Double> getScore(String query, HashMap<String, HashMap<String, Integer>> index, HashMap<String, Integer> wordCounts) {
        HashMap <String, Double> idf = new HashMap<>();
        for (String word : query.split("\\s+")) {
            idf.put(word, getInverseDocumentFrequency(word, index, wordCounts));
        }
        for(String word : idf.keySet()){
            //System.out.println(word + ": "+idf.get(word));
        }
        HashMap<String, Double> allScores = new HashMap<>();
        for(String document : wordCounts.keySet()){
            double docScore = 0;
            for (String word : query.split("\\s+")) {
                double tf = getTermFrequency(word, document, index, wordCounts);
                //System.out.println("TF, WORD: " + word + " SCORE: " + tf);
                
            
                docScore += tf*idf.get(word);
            }
            allScores.put(document, docScore);
        }

        return allScores;
    }
}
