import java.io.*;
import java.util.Scanner;
import java.util.*;
import java.util.Map.Entry;

public class Search {
    public Search(){
        
    }
    public static void query(String input) throws IOException {
        // Creating a File object for directory
        File directoryPath = new File("./docs");
        // List of all files and directories
        File filesList[] = directoryPath.listFiles();
        //System.out.println("List of files and directories in the specified directory:");
        HashMap<String, HashMap<String, Integer>> index = new HashMap<>();
        HashMap<String, Integer> wordCounts = new HashMap<>();
        StringBuilder documents = new StringBuilder();

        for (File file : filesList) {
            documents.append(" " + file.getName());
            // Instantiating the Scanner class
            BufferedReader in = new BufferedReader(new FileReader(file)); // THIS IS THE FILE THAT CONTAINS THE
                                                                          // STOPWORDS
            StringBuffer str = new StringBuffer();
            String nextLine = "";
            while ((nextLine = in.readLine()) != null)
                str.append(nextLine + "\n");
            in.close();
            String processedText = new Processor().process(str.toString());
            StopWords stopWords = new StopWords();
            String textWithoutStops = stopWords.parseStopWords(processedText);
            String[] words = textWithoutStops.split(" ");
            //wordCounts.put(file.getName(), 0);
            PorterStemmer stemmer = new PorterStemmer();
            HashMap<String, Integer> docVocab = new HashMap<>();
            int max = 1;
            for (String word : words) {
                word = stemmer.stem(word);
                if(word=="")
                    continue;
                if (!index.containsKey(word)) {
                    //wordCounts.put(file.getName(), wordCounts.get(file.getName()) + 1);
                    // If not, add it with an empty HashMap as its value
                    index.put(word, new HashMap<String, Integer>());
                    
                }
    
                // Check if the current document is already in the word's HashMap
                if (!index.get(word).containsKey(file.getName())) {
                    //wordCounts.put(file.getName(), wordCounts.get(file.getName()) + 1);
                    // If not, add it with a frequency of 1
                    index.get(word).put(file.getName(), 1);
                    docVocab.put(word, 1);
                } else {
                    // If yes, increment its frequency by 1
                    int frequency = index.get(word).get(file.getName());
                    index.get(word).put(file.getName(), frequency + 1);
                    int newVal = docVocab.get(word) + 1;
                    docVocab.put(word, newVal);
                    if(newVal>max){
                        max = docVocab.get(word);
                    }
                }
                
                //finalText.append(word);
                //finalText.append(" ");
            }
            wordCounts.put(file.getName(), max);
            /** 
            //String finalText = new PorterStemmer().stem(textWithoutStops);
            System.out.println(finalText.toString());
            // Instantiating the FileOutputStream class
            FileOutputStream fileOut = new FileOutputStream("./output/" + file.getName());
            // Instantiating the DataOutputStream class
            DataOutputStream outputStream = new DataOutputStream(fileOut);
            // Writing UTF data to the output stream
            outputStream.write(finalText.toString().getBytes());
            outputStream.flush();
            outputStream.close();
            fileOut.close();
            return;*/
        }
        /* 
        for (String word : index.keySet()) {
            System.out.print(word + " -> ");
            for (String document : index.get(word).keySet()) {
                System.out.print("(" + document + ", " + index.get(word).get(document) + ") ");
            }
            System.out.println();
        }*/
        doQuery(index, wordCounts, input);
    }
    public static void doQuery(HashMap<String, HashMap<String, Integer>> index, HashMap<String, Integer> wordCounts, String input) throws FileNotFoundException, IOException{
        Scanner scanner = new Scanner(System.in);
        DocumentScorer scorer = new DocumentScorer();
            String processed = new Processor().process(input);
            StringBuilder parsedInput= new StringBuilder();
            StopWords stopWords = new StopWords();
                String textWithoutStops = stopWords.parseStopWords(processed);
                String[] words = textWithoutStops.split(" ");
                PorterStemmer stemmer = new PorterStemmer();
                for (String word : words) {
                    parsedInput.append(stemmer.stem(word));
                    parsedInput.append(" ");
                }
    
                System.out.println("Parsed Input: "+parsedInput.toString());
            HashMap<String, Double> scores = scorer.getScore(parsedInput.toString(), index, wordCounts);
            Set<Entry<String, Double>> set = scores.entrySet();
            List<Entry<String, Double>> list = new ArrayList<Entry<String,Double>>(set);
            Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
                public int compare(Map.Entry<String, Double> o1,
                        Map.Entry<String, Double> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            int i=1;
            System.out.println(input);
            for (Entry<String, Double> entry : list) {
                String fileName = ("./docs/"+entry.getKey());
                //File file = new File(fileName);
                //BufferedReader in = new BufferedReader(new FileReader(file));
                if(entry.getValue()>0 && i<10){
                    System.out.println("Rank: " + i + " -> Document: " + entry.getKey());
                    System.out.println("Snippet: " + SnippetGenerator.generateSnippet(fileName, input));
                }
                i++;
            }

        
    }
}
