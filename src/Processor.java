import java.io.*;

public class Processor {
    public String process(String input) throws FileNotFoundException, IOException {
        // Scanner scanner = new Scanner(System.in);
        // System.out.println("Enter a string:");
        // String input = scanner.nextLine();
        // System.out.println(input);
        // Remove capitalization and punctuation
        String lowercase = input.toLowerCase();
        // System.out.println(lowercase);
        String noPunctuation = lowercase.replaceAll("[^a-zA-Z-. ]", "");
        noPunctuation = noPunctuation.replaceAll("[^a-zA-Z-]", " ");
        // System.out.println(noPunctuation);

        // Split the string into words
        String[] words = noPunctuation.split(" ");

        // Check if each word is in the dictionary
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.contains("-")) {
                String newWord = word.replaceAll("[^a-zA-Z]", "");
                boolean result = lookupDictionary(newWord);
                if (result) {
                    sb.append(newWord);
                } else {
                    sb.append(word);
                }
            } else {
                sb.append(word);
            }
            sb.append(" ");
        }

        // Remove the final hyphen
        String modified = sb.toString();
        modified = modified.substring(0, modified.length() - 1);
        return modified;
    }

    public static boolean lookupDictionary(String word) throws FileNotFoundException, IOException {
        // Read the unorder file in
        BufferedReader in = new BufferedReader(new FileReader("lib/dictionary.txt"));
        StringBuffer str = new StringBuffer();
        String nextLine = "";
        while ((nextLine = in.readLine()) != null)
            if (nextLine.contains(word)) {
                in.close();
                return true;
            }
        str.append(nextLine + "\n");
        in.close();
        return false;
    }
}
