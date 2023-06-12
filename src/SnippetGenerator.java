import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SnippetGenerator {
    public static String generateSnippet(String filePath, String query) {
        Map<String, Double> sentenceScores = new HashMap<>();
        String sentence1 = "";
        String sentence2 = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder textBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                textBuilder.append(line);
                textBuilder.append(" ");
            }

            String text = textBuilder.toString().trim();
            String[] sentences = text.split("\\.");

            int lineCount = 1;
            for (String sentence : sentences) {
                double score = calculateSentenceScore(sentence, query, lineCount);
                sentenceScores.put(sentence.trim(), score);

                if (score > sentenceScores.getOrDefault(sentence1, 0.0)) {
                    sentence2 = sentence1;
                    sentence1 = sentence.trim();
                } else if (score > sentenceScores.getOrDefault(sentence2, 0.0)) {
                    sentence2 = sentence.trim();
                }

                lineCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sentence1 + ". " + sentence2 + ".";
    }

    private static double calculateSentenceScore(String sentence, String query, int lineCount) {
        double score = 0.0;
        double densityMeasure = calculateQueryDensityMeasure(sentence, query);
        score += densityMeasure;
        int longestRun = calculateLongestContiguousRun(sentence, query);
        score += longestRun;
        int uniqueTerms = calculateUniqueQueryTerms(sentence, query);
        score += uniqueTerms;
        int totalTerms = calculateTotalQueryTerms(sentence, query);
        score += totalTerms;
        if (lineCount <= 2) {
            score += 1.0;
        }
        if (isHeading(sentence)) {
            score += 1.0;
        }
        int sentenceLength = calculateSentenceLength(sentence);
        score += sentenceLength;
        int keywordCoOccurrence = calculateKeywordCoOccurrence(sentence, query);
        score += keywordCoOccurrence;

        return score;
    }

    private static double calculateQueryDensityMeasure(String sentence, String query) {
        String[] words = sentence.split(" ");
        int queryWordCount = 0;
        int totalWordCount = words.length;

        for (String word : words) {
            if (wordMatchesQuery(word, query)) {
                queryWordCount++;
            }
        }

        return (double) queryWordCount / totalWordCount;
    }

    private static int calculateLongestContiguousRun(String sentence, String query) {
        String[] words = sentence.split(" ");
        int longestRun = 0;
        int currentRun = 0;

        for (String word : words) {
            if (wordMatchesQuery(word, query)) {
                currentRun++;
            } else {
                longestRun = Math.max(longestRun, currentRun);
                currentRun = 0;
            }
        }

        return Math.max(longestRun, currentRun);
    }

    private static int calculateUniqueQueryTerms(String sentence, String query) {
        String[] words = sentence.split(" ");
        Map<String, Integer> termCount = new HashMap<>();

        for (String word : words) {
            if (wordMatchesQuery(word, query)) {
                termCount.put(word.toLowerCase(), termCount.getOrDefault(word.toLowerCase(), 0) + 1);
            }
        }

        return termCount.size();
    }

    private static int calculateTotalQueryTerms(String sentence, String query) {
        String[] words = sentence.split(" ");
        int totalTermCount = 0;

        for (String word : words) {
            if (wordMatchesQuery(word, query)) {
                totalTermCount++;
            }
        }

        return totalTermCount;
    }
    private static boolean isHeading(String sentence) {
        return sentence.startsWith("#");
    }
    private static boolean wordMatchesQuery(String word, String query) {
        String[] queryWords = query.toLowerCase().split(" ");

        for (String queryWord : queryWords) {
            if (word.equalsIgnoreCase(queryWord)) {
                return true;
            }
        }

        return false;
    }
    private static int calculateSentenceLength(String sentence) {
        String[] words = sentence.split(" ");
        return words.length;
    }
    private static int calculateKeywordCoOccurrence(String sentence, String query) {
        String[] keywords = query.toLowerCase().split(" ");
        int coOccurrenceCount = 0;

        for (String keyword : keywords) {
            if (sentence.toLowerCase().contains(keyword)) {
                coOccurrenceCount++;
            }
        }

        return coOccurrenceCount;
    }

}
