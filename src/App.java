import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {
    
    public static void main(String[] args) throws Exception {
        Map<String, String> dictionaryMap = new HashMap<>();
        Map<String, Integer> libraryMap = getLibraryMap();
        try (BufferedReader reader = new BufferedReader(new FileReader("lib/dictionary.txt"))) {
            String word;
            while ((word = reader.readLine()) != null) {
                dictionaryMap.put(word, soundex(word));
            }
        }
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter a query or q to quit:");
            String userQuery = scanner.nextLine();
            while(!userQuery.equals("q")){
                String[] queryWords = userQuery.split("\\s+");
                boolean isCorrect = true;
                StringBuilder qBuilder = new StringBuilder();

                for (String queryWord : queryWords) {
                    if (!isSpellingCorrect(queryWord, dictionaryMap)) {
                        isCorrect = false;
                        List<String> suggestions = getSuggestions(queryWord, dictionaryMap);
                        String replacement = findBestCorrection(queryWord, suggestions, libraryMap);
                        System.out.println("Misspelled word: " + queryWord);
                        //System.out.println("Soundex of Word: " + soundex(queryWord));
                        //System.out.println("Suggestions: " + suggestions);
                        System.out.println("Best Suggestion: " + replacement);
                        qBuilder.append(replacement + " ");
                    }
                    else{
                        qBuilder.append(queryWord + " ");
                    }
                }
                
                Search.query(qBuilder.toString());
                System.out.println("Enter a query or q to quit:");
                userQuery = scanner.nextLine();
            }
            scanner.close();
        }
        
    }

    private static String soundex(String word) {
        char[] chars = word.toUpperCase().toCharArray();
        char firstChar = chars[0];

        // Convert the word to its Soundex representation
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case 'B':
                case 'F':
                case 'P':
                case 'V':
                    chars[i] = '1';
                    break;
                case 'C':
                case 'G':
                case 'J':
                case 'K':
                case 'Q':
                case 'S':
                case 'X':
                case 'Z':
                    chars[i] = '2';
                    break;
                case 'D':
                case 'T':
                    chars[i] = '3';
                    break;
                case 'L':
                    chars[i] = '4';
                    break;
                case 'M':
                case 'N':
                    chars[i] = '5';
                    break;
                case 'R':
                    chars[i] = '6';
                    break;
                default:
                    chars[i] = '0';
            }
        }

        // Remove consecutive duplicates
        StringBuilder soundexCode = new StringBuilder();
        soundexCode.append(firstChar);
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] != chars[i - 1] && chars[i] != '0') {
                soundexCode.append(chars[i]);
            }
        }

        // Pad or truncate to a length of 4 characters
        if (soundexCode.length() < 4) {
            soundexCode.append("000");
        } else if (soundexCode.length() > 4) {
            soundexCode.setLength(4);
        }

        return soundexCode.toString();
    }

    public static boolean isSpellingCorrect(String query, Map<String, String> dictionaryMap) {
        
        String[] queryWords = query.split("\\s+");
        for (String queryWord : queryWords) {
            if(dictionaryMap.keySet().contains(queryWord.toLowerCase())){
                return true;
            }
        }
        return false; 
    }
    public static List<String> getSuggestions(String word, Map<String, String> dictionaryMap) {
        String soundexQueryWord = soundex(word);
        List<String> suggestions = new ArrayList<>();
        for(String dictionaryWord : dictionaryMap.keySet()){
            if(soundexQueryWord.equals(dictionaryMap.get(dictionaryWord))){
                suggestions.add(dictionaryWord);
            }
            
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader("lib/dictionary.txt"))) {
            String dictWord;
            while ((dictWord = reader.readLine()) != null) {
                if (levenshteinDistance(word.toLowerCase(), dictWord.toLowerCase()) <= 2) {
                    suggestions.add(dictWord);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return suggestions;
    }
    public static int levenshteinDistance(String word1, String word2) {
        int m = word1.length();
        int n = word2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i][j - 1], dp[i - 1][j]));
                }
            }
        }

        return dp[m][n];
    }
    public static String findBestCorrection(String misspelledWord, List<String> suggestions, Map<String, Integer> libraryMap) throws IOException {
        double maxProbability = 0.0;
        String bestCorrection = misspelledWord;

        for (String suggestion : suggestions) {
            double editProb = calculateEditProbability(misspelledWord, suggestion);
            double wordProb = calculateWordProbability(suggestion, libraryMap);
            double probability = editProb * wordProb;

            if (probability > maxProbability) {
                maxProbability = probability;
                bestCorrection = suggestion;
            }
        }

        return bestCorrection;
    }
    public static Map<String, Integer> getLibraryMap() throws IOException{
        int totalWords = 0;
        File directoryPath = new File("./docs");
        File filesList[] = directoryPath.listFiles();
        StringBuilder documents = new StringBuilder();
        HashMap<String, Integer> wordCounts = new HashMap<>();
        for (File file : filesList) {
            documents.append(" " + file.getName());
            BufferedReader in = new BufferedReader(new FileReader(file));
            StringBuffer str = new StringBuffer();
            String nextLine = "";
            while ((nextLine = in.readLine()) != null)
                str.append(nextLine + "\n");
            in.close();
            String processedText = new Processor().process(str.toString());
            StopWords stopWords = new StopWords();
            String textWithoutStops = stopWords.parseStopWords(processedText);
            String[] words = textWithoutStops.split(" ");
            totalWords += words.length;
            for(String word: words){
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
        }
            wordCounts.put("TOTALWORDSINLIBRARY", totalWords);
            return wordCounts;
    }
    public static double calculateWordProbability(String suggestion, Map<String, Integer> libraryMap) throws IOException{
            return (double) libraryMap.getOrDefault(suggestion, 0) / libraryMap.get("TOTALWORDSINLIBRARY");
        }
    
    public static Map<String, List<List<String>>> getQueryMap() throws FileNotFoundException, IOException{
        Map<String, List<List<String>>> queryMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("lib/queryLog.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 2) {
                    List<String> query = new LinkedList<String>(Arrays.asList(parts[1].split(" ")));
                    if(queryMap.containsKey(parts[0])){
                        queryMap.get(parts[0]).add(query);
                    }
                    else{
                        queryMap.put(parts[0], new ArrayList<List<String>>());
                        queryMap.get(parts[0]).add(query);
                    }
                }
            }
        }
        return queryMap;
    }
    
    public static double calculateEditProbability(String word, String suggestion) throws FileNotFoundException, IOException {
        //Map<String, Integer> wordCorrections = new HashMap<>();
        //Map<String, Integer> totalWordCorrections = new HashMap<>();
        int wordCorrections = 0;
        int totalCorrections = 0;
        if(suggestion.equals("court")){
            System.out.println();
        }
        Map<String, List<List<String>>> queryMap = getQueryMap();
        for(String session : queryMap.keySet()){
            boolean containsTarget = false;
            boolean containsError = false;
            for(List<String> query : queryMap.get(session)){
                if(query.contains(suggestion) && containsError){
                    wordCorrections += 1;
                    //totalCorrections += 1;
                    containsError = false;
                }
                else if(query.contains(word) && containsTarget && !query.contains(suggestion)){
                    wordCorrections += 1;
                    //totalCorrections += 1;
                    containsTarget = false;
                }
                else if(containsTarget && !query.contains(suggestion)){
                    totalCorrections += 1;
                    
                }
                if(query.contains(suggestion) && !containsTarget){
                    containsTarget = true;
                }
                if(query.contains(word) && !containsError){
                    containsError = true;
                }
                
            }
        }
        
        if(totalCorrections == 0 || wordCorrections == 0){
            return 0;
        }
        return (double) wordCorrections / totalCorrections;
    }
}
