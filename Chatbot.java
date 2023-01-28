import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;

public class Chatbot {

    public static void main(String args[])throws IOException {
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
		        finalSentence = substituter(finalSentence, presubstitutions);
		        finalSentence = substituter(finalSentence, postsubstitutions);
			List<String> fragment = decompose(keywords, finalSentence);
        	    System.out.println(finalSentence);
	        }
	        System.out.println(goodbye);
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

    public static int compareWords(List<String> wordsToBeSwapped, List<String> input, HashSet<Integer> banned){
        int result = -1;
        for (int i = 0; i < input.size(); i++){
	        int j = 0;
	        int k = i;
            if(banned.contains(i) == false){
		while (j < wordsToBeSwapped.size()){
                	if (!input.get(i).equals(wordsToBeSwapped.get(j))){

                    		break;
                	}
			else if (input.get(i).equals(wordsToBeSwapped.get(j)) &&  i == input.size() - 1 && j < wordsToBeSwapped.size() - 1) {
				return -1;
			}
                	else {
                    		if (j == wordsToBeSwapped.size() - 1){
                        		return k;
                    		} else {
                    			i++;
                    			j++;
                    		}
                	}
            	}
            }
	}
        return result;
    }

    public static List<String> substituter(List<String> input, List<String> substitutions){
	List<String> provisional = new ArrayList<>(input);
	HashMap<Integer, Integer> differences = new HashMap<>();
	List<String> result = new ArrayList<>(input);
	HashSet<Integer> banned = new HashSet<>();
	int totalDifference = 0;
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
		// this is the new loop that makes it multiple runs for each rule
		for (int j = 0; j < input.size(); j ++){
        		int index = compareWords(wordsToBeSwapped, provisional, banned);
			if (index > -1){
				int relevantDifferences = 0;
				for (int ind : differences.keySet()){
					if (ind < index){
						relevantDifferences += differences.get(ind);
					}
				}
				for (int y = 0; y < wordsToBeSwapped.size(); y++){
					banned.add(index + y);
					result.remove(index + relevantDifferences);
				}
				for (int t = 0; t < wordsToBeAdded.size(); t++){
					result.add(index + relevantDifferences, wordsToBeAdded.get(t));
				}
				totalDifference += (wordsToBeAdded.size() - wordsToBeSwapped.size());
				differences.put(index, (wordsToBeAdded.size())- (wordsToBeSwapped.size()));
			}
		}
    	}
        return result;
    }


    public static List<String> decompose(List<String> keywordLines, List<String> input){
	boolean match = false;
	int index = -1;
	int x = -1;
	for (int i = 0; i < keywordLines.size(); i++){
		HashSet<Integer> banned = new HashSet<>();
		List<String> keywords = makeTokens(keywordLines.get(i));
        	int k = 0;
        	while (!keywords.get(k).equals(">")){
            		k++;
        	}
        	int p = makeTokens(keywordLines.get(i)).size() - k;
        	while (p > 0){
            		keywords.remove(k);
            		p--;
        	}
		System.out.println(keywords);
		index = compareWords(keywords, input, banned);
		if (index > -1){
			match = true;
			x = i;
			break;
		}
	}
	if (match == false){
		return null;
	}
	System.out.println(index);
	List<String> rule = new ArrayList<>();
        List<String> fragment = new ArrayList<>(input);
        return fragment;
    }
}
