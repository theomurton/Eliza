import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;

public class Chatbot {

    public static void main(String args[]) throws IOException {
		if (args.length == 0){
			System.out.println("No file supplied");
			System.exit(0);
		}
		List<String> script = Files.readAllLines(Paths.get(args[0]));
		String punctuation = "?.!,";
        boolean isQuit = false;
        List<String> greeting = textRetriever(script, "GREETING");
        List<String> goodbye = textRetriever(script, "GOODBYE");
        List<String> presubstitutions = textRetriever(script, "PRESUBSTITUTIONS");
        List<String> postsubstitutions = textRetriever(script, "POSTSUBSTITUTIONS");
	    List<String> keywords = textRetriever(script, "KEYWORDS");
	    List<String> idle = textRetriever(script, "IDLE");
	    List<String> memory = textRetriever(script, "MEMORY");
	    List<String> quit = textRetriever(script, "QUIT");
	    Random random = new Random();
	    HashSet<Integer> banned = new HashSet<>();
	    List<List<String>> memories = new ArrayList<>();
	    int tempRandom = -1;
	    int idleRandom = -1;
	    System.out.println(greeting.get(random.nextInt(greeting.size())));
	    while (isQuit == false){
		//temporarily stored random value ensures idle responses are not done twice in a row
		tempRandom = idleRandom;
		idleRandom = random.nextInt(idle.size());
		while (idleRandom == tempRandom){
			tempRandom = idleRandom;
			idleRandom = random.nextInt(idle.size());
		}
	    	List<String> initialSentence = makeTokens(getInput());
		int comparison = -1;
		//this little loop checks first for the quit keywords. By default the value of comparison is -1, no match. If this is altered by the Comapre words method then the program breaks to the quit section.
		for (int i = 0; i < quit.size(); i ++){
			List<String> quitWords = makeTokens(quit.get(i));
			comparison = compareWords(quitWords, initialSentence, banned);
			if (comparison != -1){
				break;
			}
		}
		//in the event of no quit keywords found
		if (comparison == -1) {
			List<String> finalSentence = initialSentence;
			finalSentence = substituter(finalSentence, presubstitutions);
			List<String> fragment = decompose(keywords, finalSentence);
			//the fragment is null only if there are no keyword matches.
			if (fragment == null){
				int chance = random.nextInt(11);
				//if there are no keyword matches there is a 2/5 chance of a generic idle message and a 3/5 chance of a specific message that relates to something the user previously said
				if (chance < 5){
					System.out.println(idle.get(idleRandom));
				}
				if (chance >= 5){
				//if the specific route is taken but there are no 'memories' in data then it defaults to giving a generic idle message instead
					if (recall(memories, memory) == null){
						System.out.println(idle.get(idleRandom));
					} else {
					List<String> sentence = recall(memories, memory);
					int code = Integer.parseInt(sentence.get(sentence.size() - 1));
					// the code is an appended integer to the end of the returned sentence. It denotes which memory was randomly picked. This memory is removed as we don't want repeats
					memories.remove(code);
					sentence.remove(sentence.size() - 1);
					System.out.println(makeFinalSentence(sentence, punctuation));
					}
				}
			} else {
				//in the case a keyword is found
				fragment = substituter(fragment, postsubstitutions);
				List<String> thought = new ArrayList<>(fragment);
				int lineIndex = Integer.parseInt(fragment.get(fragment.size() - 1));
				fragment.remove(fragment.size() -1);
				finalSentence = recompose(fragment, keywords, lineIndex);
				// this checks if the last element of the returned list is 'true' denoting that there is a stored memory. If so the memory is added. In either case this last element is then deleted.
				if (finalSentence.get(finalSentence.size() - 1).equals("true")){
					memories.add(thought);
				}
				finalSentence.remove(finalSentence.size() - 1);
				System.out.println(makeFinalSentence(finalSentence, punctuation));
			}
		} else {
			//if a quit keyword is found this is jumped to straight away
			isQuit = true;
		}
            }
	//once the while loop is broken a random goodbye message displays
	    System.out.println(goodbye.get(random.nextInt(goodbye.size())));
    }

// below method recalls earlier fragments from keywords and recomposes them
    public static List<String> recall(List<List<String>> memories, List<String> memory){
	// if there are no memories null is returned and dealt with appropriately
	if (memories.isEmpty()){
		return null;
	}
	Random rand = new Random();
	// need to randomly choose a memory to seem more lifelike
	int number = rand.nextInt(memories.size());
	int size = memories.get(number).size();
	String index = memories.get(number).get(size - 1);
	int x = -1;
	//this method takes the end of the memory list, which is a number denoting which memory method is should take
	for (int i = 0; i < memory.size(); i++){
		List<String> words = makeTokens(memory.get(i));
		if (words.get(0).equals(index)){
			x = i;
			break;
		}
	}
	List<String> words = makeTokens(memory.get(x));
	words.remove(0);
	words.remove(0);
	int k = 0;
	while (!words.get(k).equals("()")){
		k++;
	}
	words.remove(k);
	List<String> thisMemory = new ArrayList<>(memories.get(number));
	thisMemory.remove(size - 1);
	Collections.reverse(thisMemory);
	for (int e = 0; e < size - 1; e++){
		words.add(k, thisMemory.get(e));
	}
	// the random index is returned so the memory can be deleted so it is not reused, breaking the illusion
	String code = Integer.toString(number);
	words.add(code);
	return words;
    }

    public static List<String> recompose(List<String> fragment, List<String> keywords, int index){
	List<List<String>> keywordLine = new ArrayList<>();
	List<String> line = makeTokens(keywords.get(index));
	//keywords.get(index)
        while (!line.get(0).equals("<")){
        	line.remove(0);
        }
        line.remove(0);
	int y = 0;
	for (int j = 0; j < line.size(); j++){
		if (line.get(j).equals("|")){
			y++;
		}
	}
	for (int z = 0; z < y; z++){
		List<String> phrase = new ArrayList<>();
		while (!line.get(0).equals("|")){
			phrase.add(line.get(0));
			line.remove(0);
		}
		line.remove(0);
		keywordLine.add(phrase);
	}
	Random randy = new Random();
	int ran = randy.nextInt(y);
	List<String> result = new ArrayList<>(keywordLine.get(ran));
	int wordNumber = 0;
	// for some recomposition rules, for instance for 'yes', we just want to say something relevant but not parrot back what they said. Hence we have a boolean which tracks if the rule for out keyword contains a bracket (denoting where the stored fragment should be inserted)
	boolean bracket = true;
	while (!result.get(wordNumber).equals("()")){
		if (wordNumber + 1 == result.size()){
		bracket = false;
		break;
		}
		wordNumber ++;
	}
	//removing the brackets character itself if it is found
	if (bracket == true){
	result.remove(wordNumber);
	Collections.reverse(fragment);
	for (int i = 0; i < fragment.size(); i++){
		result.add(wordNumber, fragment.get(i));
	}
	}
	// appends a temporary true or false to the end of the list depending on whether the rule has a bracket or not. If it doesn't the main method won't add it as a memory
	String foundBracket = Boolean.toString(bracket);
	result.add(foundBracket);
	return result;
    }

    public static String getInput(){
        Scanner scan = new Scanner(System.in);
        String text = scan.nextLine();
		text = text.replaceAll("[,?!.]", "");
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
//this method reads between the hashmap values to get the relevant text
    public static List<String> textRetriever(List<String> input, String section){
            List<String> result = new ArrayList<String>();
            HashMap<String, String> keys = new HashMap<String, String>();
            keys.put("GREETING", "GOODBYE");
            keys.put("GOODBYE", "PRESUBSTITUTIONS");
            keys.put("PRESUBSTITUTIONS", "POSTSUBSTITUTIONS");
            keys.put("POSTSUBSTITUTIONS", "KEYWORDS");
	    	keys.put("KEYWORDS", "IDLE");
	    	keys.put("IDLE", "MEMORY");
	    	keys.put("MEMORY", "QUIT");
	    	keys.put("QUIT", "END");
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
//compare words method returns the index of the first word matched if there is a match and -1 if there is not match. This method is long as it needs to be clever enough to catch mutliple word keywords
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
//substituter deals with pre and post substitutions. It has to be long as it keeps track of all the differences at certain indexes, as keywords and their substitutions often vary in length.
    public static List<String> substituter(List<String> input, List<String> substitutions){
	List<String> provisional = new ArrayList<>(input);
	HashMap<Integer, Integer> differences = new HashMap<>();
	List<String> result = new ArrayList<>(input);
	//a banned set of indexes that have been found to contain keywords is required. This stops the method looking at these keywords as they have already been verified as keywords and we don't want loops from you are to i am back to you are etc etc etc
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
		// this is the new loop that makes it multiple runs for each rule in case something like 'i' is multiple times in a line.
		for (int j = 0; j < input.size(); j ++){
        		int index = compareWords(wordsToBeSwapped, provisional, banned);
			if (index > -1){
				//relevant differences is the differences before the index we are interested in- by how much should we offset the current substitution based on previous substitutions?
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

//this method checks for keywords and then decomposes
    public static List<String> decompose(List<String> keywordLines, List<String> input){
	boolean match = false;
	int index = -1;
	Integer x = -1;
	List<String> keywords = new ArrayList<>();
	for (int i = 0; i < keywordLines.size(); i++){
		HashSet<Integer> banned = new HashSet<>();
		keywords = makeTokens(keywordLines.get(i));
        	int k = 0;
        	while (!keywords.get(k).equals(">")){
            		k++;
        	}
        	int p = makeTokens(keywordLines.get(i)).size() - k;
        	while (p > 0){
            		keywords.remove(k);
            		p--;
        	}
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
	List<String> rule = makeTokens(keywordLines.get(x));
	int k = 0;
        while (!rule.get(k).equals("<")){
        	k++;
        }
        int p = makeTokens(keywordLines.get(x)).size() - k;
        while (p > 0){
        	rule.remove(k);
                p--;
        }
	while (!rule.get(0).equals(">")){
        	rule.remove(0);
        }
        rule.remove(0);
	if (rule.get(0).equals("synonym")){
		List<String> substitutions = new ArrayList<>();
		String sub = "";
		for (int g = 0; g < keywords.size(); g++){
			sub = sub + " " + keywords.get(g);
		}
		sub += " >";
		int t = 1;
		while (!rule.get(t).equals("|")){
			sub = sub + " " + rule.get(t);
			t++;
		}
		substitutions.add(sub);
		List<String> newSynonym = substituter(input, substitutions);
		return decompose(keywordLines, newSynonym);
	}
	else if (rule.get(0).equals("*") && rule.get(1).equals("()")){
        	List<String> fragment = new ArrayList<>(input);
		int c = 0;
		while (c != index + keywords.size() - 1){
			fragment.remove(0);
			c++;
		}
		fragment.remove(0);
		String j = Integer.toString(x);
		fragment.add(j);
        	return fragment;
	} else {
		List<String> fragment = new ArrayList<>();
		for (int a = 0; a < index; a++){
			fragment.add(input.get(a));
		}
		Collections.reverse(fragment);
		String j = Integer.toString(x);
                fragment.add(j);
		return fragment;
	}
    }

	public static String makeFinalSentence(List<String> sentence, String punctuation){
		String output  = "";
        for (int i = 0; i < sentence.size(); i++){
			if (punctuation.contains(sentence.get(i))){
				output += sentence.get(i);
			}
            else if (i == 0){
                output += sentence.get(0);
            } else {
            	output = output + " " + sentence.get(i);
            }
        }
		String last = output.substring(0, 1).toUpperCase() + output.substring(1);
        return last;
	}
}
