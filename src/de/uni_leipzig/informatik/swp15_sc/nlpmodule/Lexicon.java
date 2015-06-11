package de.uni_leipzig.informatik.swp15_sc.nlpmodule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.*;
import org.apache.commons.lang3.StringUtils;

import edu.smu.tspell.wordnet.*;
import de.uni_leipzig.informatik.swp15_sc.utils.IOUtils;
import de.uni_leipzig.informatik.swp15_sc.utils.URIUtils;
import de.uni_leipzig.informatik.swp15_sc.utils.TemplateUtils;

// use: StringUtils.getLevenshteinDistance(String, String)

/**
 * This class represents a lexicon.
 * @author Stephan Suessmaier
 * 
 * TO DO: Wordnet expansion: z.b. best -> good
 *
 */
public class Lexicon {
	/**
	 * Creates a new Lexicon with pre-defined entries from a file.
	 * Convention: all words in the lexicon only consist of lowercase letters.
	 * @param path path to the file with pre-defined entries.
	 */
	public Lexicon (String path, String[] classes, String[] properties) {
		entries = new HashMap<String, JSONObject> ();
		String file = de.uni_leipzig.informatik.swp15_sc.utils.IOUtils.readFile(path);
		JSONObject json = new JSONObject(file);
		
		init(path);
		
		/*Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			String key = keys.next ();
			entries.put(key, json.getJSONObject(key));
		}*/
		
		
		this.classes = URIUtils.cutOff(classes);
		this.properties = URIUtils.cutOff(properties);
	}
	
	/**
	 * Adds an entry to this Lexicon.
	 * @param word word to add.
	 * @param variable information on the word.
	 */
	public void addEntry (String word, JSONObject variable) {
		entries.put(word, variable);
	}
	
	/**
	 * Searches for the desired word.
	 * @param query word(s) to look up
	 * @return information on the desired word(s) or null if no such entry exists.
	 */
	public JSONObject searchFor (String query) throws Exception {
		JSONObject result = entries.get(query.toLowerCase());
		
		if (result == null) {
			if (isClass(query)) {
				result = new JSONObject();
				result.put("a", "classvar");
				result.put("type", "class");
				result.put("vartype", "slot");
				result.put("word", query);
			}
			else if (isProperty(query)) {
				result = new JSONObject();
				result.put("a", "propvar");
				result.put("type", "property");
				result.put("vartype", "slot");
				result.put("word", query);
			}
			else {
				// use WordNet database to get related words.
				HashSet<String> relatedWordSet = getRelatedWordSet(query);
				Iterator<String> it = relatedWordSet.iterator();
				while (result == null && it.hasNext()) {
					result = entries.get(it.next());
				}
				
				// if still no match was found, throw the exception.
				if (result == null) {
					if (query.charAt(query.length()-1) == 's') {
						result = entries.get(query.substring(0, query.length()-1));
					}
					if (result == null) {
						throw new Exception("IndependentLexicon: no such word exists in this lexicon.");
					} else {
						result.put("a", "template");
					}
					
				} else {
					result.put("a", "template");
				}
			}
		}
		else {
			result.put("a", "template");
		}
		
		return result;
	}
	
	/**
	 * initializes the entries map
	 * @param path path to lexicon file
	 */
	private void init (String path) {
		JSONObject lexicon = new JSONObject(IOUtils.readFile(path));
		String[] keys = JSONObject.getNames(lexicon);
		for (String key: keys) {
			try {
				JSONObject compiled = TemplateUtils.compile(lexicon.getString(key));
				entries.put(key, compiled);
			}
			catch (Exception e) {
				System.err.println("[ERROR] in Lexicon.init(..): " + e);
				continue;
			}
		}
	}
	
	/**
	 * This method creates a HashSet of related words by using a WordNet database
	 * @param word word to look up
	 * @return related words in a HashSet
	 */
	private HashSet<String> getRelatedWordSet (String word) {
		HashSet<String> results = new HashSet<String>();
		
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets(word);
		for (Synset synset: synsets) {
			String[] wordforms = synset.getWordForms();
			for (String s: wordforms) {
				results.add(s);
			}
		}
		
		return results;
	}
	
	/**
	 * Checks if the desired String is a property
	 * @param str String to test
	 * @return true if the String is a property, else false
	 */
	private boolean isProperty (String str) {
		for (String prop: properties) {
			if (StringUtils.getLevenshteinDistance(str.toLowerCase(), prop.toLowerCase()) < TOLERANCE)
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if the desired String is a class
	 * @param str String to test
	 * @return true if the String is a class, else false
	 */
	private boolean isClass (String str) {
		for (String cl: classes) {
			if (StringUtils.getLevenshteinDistance(str.toLowerCase(), cl.toLowerCase()) < TOLERANCE)
				return true;
		}
		return false;
	}
	
	private final HashMap<String, JSONObject> entries;
	private final String[] classes;
	private final String[] properties;
	private static final int TOLERANCE = 2;
}
