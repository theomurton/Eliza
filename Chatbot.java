import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;

public class Chatbot {

    public static void main(String args[]){
        try {
            boolean isQuit = false;
            List<String> script = Files.readAllLines(Paths.get(args[0]));
            List<String> greeting = textRetriever(script, "GREETING");
            List<String> goodbye = textRetriever(script, "GOODBYE");
            List<String> presubstitutions = textRetriever(script, "PRESUBSTITUTIONS");
            List<String> postsubstitutions = textRetriever(script, "POSTSUBSTITUTIONS");
	        List<String> keywords = textRetriever(script, "KEYWORDS");
	        System.out.println(greeting);
	        while (isQuit == false){
		        List<String> initialSentence = makeTokens(getInput());
		        List<String> finalSentence = initialSentence;
		        finalSentence = substitute(finalSentence, presubstitutions);
			System.out.println("presubbed");
			System.out.println(finalSentence);
		        finalSentence = substitute(finalSentence, postsubstitutions);
        	    System.out.println(finalSentence);
	        }
	        System.out.println(goodbye);
        } catch (Exception e){
            System.out.println("No supported script supplied");
            System.exit(0);
        }
    }

    public static String getInput(){
        Scanner scan = new Scanner(System.in);
        String text = scan.nextLine();
        return text;
    }

    public static List<String> makeTokens(String input){
        List<String> tokens = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(input, " ");
        while (st.hasMoreElements()){
             tokens.add(st.nextToken().toLowerCase());
        }
        return tokens;
    }
    public static List<String> scanKeywords(List<String> input){
        HashMap<String, Integer> keywordsDictionary= new HashMap<String, Integer>();
        List<String> keywords = new ArrayList<>();

        for (String i : keywordsDictionary.keySet()){
            for (int j = 0; j < input.size(); j++){
                if (input.get(j).equals(i)){
                    keywords.add(input.get(j));
                }
            }
        }
        return keywords;

    }

    public static List<String> textRetriever(List<String> input, String section){
            List<String> result = new ArrayList<String>();
            HashMap<String, String> keys = new HashMap<String, String>();
            keys.put("GREETING", "GOODBYE");
            keys.put("GOODBYE", "PRESUBSTITUTIONS");
            keys.put("PRESUBSTITUTIONS", "POSTSUBSTITUTIONS");
            keys.put("POSTSUBSTITUTIONS", "KEYWORDS");
	    keys.put("KEYWORDS", "END");
            int i = 0;
            while (!input.get(i).equals(section)){
                i++;
            }
            i++;
            while (!input.get(i).equals(keys.get(section))){
                result.add(input.get(i));
                i++;
            }
            return result;
    }

    public static List<String> substitute(List<String> input, List<String> substitutions){
	List<String> result = input;
	List<String> provisional = new ArrayList<>(input);
    for (int i = 0; i < substitutions.size(); i++){
        List<String> wordsToBeAdded = makeTokens(substitutions.get(i));
        while (!wordsToBeAdded.get(0).equals(">")){
            wordsToBeAdded.remove(0);
        }
        wordsToBeAdded.remove(0);
	Collections.reverse(wordsToBeAdded);
        List<String> wordsToBeSwapped = makeTokens(substitutions.get(i));
        int k = 0;
        while (!wordsToBeSwapped.get(k).equals(">")){
            k++;
        }
        int p = makeTokens(substitutions.get(i)).size() - k;
        while (p > 0){
            wordsToBeSwapped.remove(k);
            p--;
        }
	System.out.println("got here");
        int index = compareWords(wordsToBeSwapped, provisional);
	if (index > -1){
		for (int r = 0; r < wordsToBeSwapped.size(); r++){
			result.remove(index);
			provisional.remove(index);
		}
		for (int t = 0; t < wordsToBeAdded.size(); t++){
			result.add(index, wordsToBeAdded.get(t));
		}
	}
    }
	return result;
    }

    public static int compareWords(List<String> wordsToBeSwapped, List<String> input){
        int result = -1;
        int j = 0;
        for (int i = 0; i < input.size(); i++){
            int k = i;
		System.out.println(wordsToBeSwapped);
            while (j < wordsToBeSwapped.size()){
                if (!input.get(i).equals(wordsToBeSwapped.get(j))){
                    break;
                }
		else if (input.get(i).equals(wordsToBeSwapped.get(j)) &&  i == input.size() - 1 && j < wordsToBeSwapped.size() - 1) {
		return -1;
		}
                else {
                    if (j == wordsToBeSwapped.size() - 1){
			System.out.println(input.get(k));
                        return k;
                    } else {
                    i++;
                    j++;
                    }
                }
            }
        }
        return result;
    }

    public static List<String> scanKeywords(List<String> keywords, List<String> input){
        List<String> result = new ArrayList<>();
        return result;
    }

    public static List<String> decompose(List<String> keywords, List<String> input){
        List<String> fragment = new ArrayList<>(input);
        return fragment;
    }
}
