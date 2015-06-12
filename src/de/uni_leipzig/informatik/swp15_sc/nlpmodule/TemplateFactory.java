package de.uni_leipzig.informatik.swp15_sc.nlpmodule;

import org.json.*;
import java.util.LinkedList;
import de.uni_leipzig.informatik.swp15_sc.utils.TemplateUtils;

// import utils.JsonTableUtils;

/**
 * This class is used to generate the query templates based on previously added
 * query fragments, which are merged into a template in a second step.
 * 
 * @author stephan
 *
 */
public class TemplateFactory {
	/**
	 * Creates a new TemplateFactory
	 */
	public TemplateFactory() {
		fragments = new LinkedList<JSONObject>();
		remainingWords = new LinkedList<TaggedWord>();
		properties = new LinkedList<JSONObject>();
		classes = new LinkedList<JSONObject>();
	}

	/**
	 * clears the list of template fragments
	 */
	public void clear() {
		fragments.clear();
		remainingWords.clear();
		classes.clear();
		properties.clear();
	}

	public JSONObject generate() {
		for (JSONObject fragment : fragments) {
			try {
				System.out.println("[DEBUG] TemplateFactory: " + TemplateUtils.makePretty(fragment));
			} catch (Exception e) {
				System.err.println(e);
			}
		}
		/*
		 * Idea: 1. merge the fragments from the fragments list into one
		 * template with blanks (variables that need to be replaced) 2. fill the
		 * blanks with variables from classes, properties or generated variables
		 * based on remainingwords 3. As there may be multiple results, set a
		 * score value for each template.
		 */
		String skel = merge();
		// LinkedList<JSONObject> result = fillBlanks(skel);
		this.clear();
		return new JSONObject(skel);
	}

	/**
	 * merges the template fragments into one query template
	 * 
	 * @return query template
	 */
	private String merge() {
		// String a = "{}";
		JSONObject result = new JSONObject();

		// initialize result as empty template
		result.put("select", new JSONArray());
		result.put("countSelect", false);
		result.put("from", "default");
		// result.put("where", new JSONArray());
		result.put("setOptions", false);
		JSONArray where = new JSONArray();

		JSONObject options = null;
		// boolean selectCount = false;
		boolean setOptions = false;

		// System.out.println("Adding Fragments");

		for (JSONObject fragment : fragments) {
			try {
				// selectCount |= fragment.getBoolean("selectCount");

				if (fragment.getBoolean("setOptions")) {
					setOptions = true;

					if (options == null) {
						options = new JSONObject();
					}

					if (fragment.getJSONObject("options").has("orderby")) {
						options.put("orderby", fragment.getJSONObject("options").getString("orderby"));
					}
					if (fragment.getJSONObject("options").has("limit")) {
						options.put("limit", fragment.getJSONObject("options").getInt("limit"));
					}

					options.put("orderbycount", fragment.getJSONObject("options").has("orderbycount"));
				}

				// test if we should count the select variable
				if (fragment.getBoolean("countSelect")) {
					result.put("countSelect", true);
				}

				JSONArray where_temp = fragment.getJSONArray("where");
				// append triples to and

				for (int i = 0; i < where_temp.length(); i++) {
					where.put(where_temp.getJSONObject(i));
				}
			} catch (JSONException e) {
				System.err.println("[ERROR] in TemplateFactory: merge(): " + e);
				continue;
			}
		}

		// addPersons(where);

		// DO MERGE TEST HERE
		if (fragments.size() > 1) {
			// with only one fragment, this doesn't make sense
			JSONArray where_merged = new JSONArray();
			for (int i = 0; i < where.length(); i++) {
				for (int j = i + 1; j < where.length(); j++) {
					if (mergeRelations(where.getJSONObject(i), where.getJSONObject(j))) {
						JSONObject merged = mergeRels(where.getJSONObject(i), where.getJSONObject(j));
						where_merged.put(merged);
						where.remove(j);
						break;
					} else if (j + 1 == where.length()) {
						// if loop ends and nothing can be merged to where[i],
						// add where[i] to where_merged
						where_merged.put(where.getJSONObject(i));
					}
				}
			}
			where = where_merged;
		}

		LinkedList<String> person = new LinkedList<String>();
		LinkedList<String> eco = new LinkedList<String>();
		LinkedList<String> location = new LinkedList<String>();
		for (TaggedWord word : remainingWords) {
			if (word.getNe().equals("LOCATION")) {
				location.add(word.getWord());
			}
		}

		for (TaggedWord remainingWord : remainingWords) {
			if (remainingWord.getNe().equals("PERSON")) {
				// Füge den Namen zur Liste hinzu
				if ((remainingWords.get(remainingWords.indexOf(remainingWord) - 1).getPos().equals("NN") || remainingWords
						.get(remainingWords.indexOf(remainingWord) - 1).getPos().equals("NNP"))
						&& remainingWords.get(remainingWords.indexOf(remainingWord) - 1).getWord().length() == 1) {
					person.add(remainingWords.get(remainingWords.indexOf(remainingWord) - 1).getWord() + " "
							+ remainingWord.getWord());
				} else {
					person.add(remainingWord.getWord());
				}
				// person.add(remainingWord.getWord());

			}
			if (remainingWord.getNe().equals("ECO")) {
				// Füge den Eco zur Liste hinzu
				eco.add(remainingWord.getWord());
			}
			/*
			 * if(remainingWord.getNe() = "DATE"){ person.add(remainingWord);
			 * //Füge den Eco zur Liste hinzu, beachte evtl. DATES die direkt
			 * //vor diesem Wort kommen, dann evtl. ein Datum mit Monat oder Tag
			 * //Dann vorheriges DATE aus liste Löschen und zusammenfügen. Evtl.
			 * externe Klasse? }
			 */
		}

		// System.out.println("Groeße von eco " + eco.size());

		// Eco einfügen
		JSONObject empty_triple = new JSONObject();
		empty_triple.put("subject", new JSONObject());
		empty_triple.getJSONObject("subject").put("vartype", "var");
		empty_triple.getJSONObject("subject").put("word", "x");
		empty_triple.getJSONObject("subject").put("count", false);
		empty_triple.put("property", new JSONObject());
		empty_triple.getJSONObject("property").put("vartype", "slot");
		empty_triple.getJSONObject("property").put("type", "property");
		empty_triple.getJSONObject("property").put("word", "placeholder");
		empty_triple.getJSONObject("property").put("count", false);
		empty_triple.put("object", new JSONObject());
		empty_triple.getJSONObject("object").put("vartype", "const");
		empty_triple.getJSONObject("object").put("word", "placeholder");
		empty_triple.getJSONObject("object").put("count", false);

		// System.out.println("Adding Eco");

		if (eco.size() == 1) {
			JSONObject triple_temp = new JSONObject(empty_triple.toString());
			triple_temp.getJSONObject("property").put("word", "eco");
			// System.out.println("test" + eco.peek());
			triple_temp.getJSONObject("object").put("word", eco.pop());
			// empty_triple.getJSONObject("object").put("vartype", "const");
			triple_temp.getJSONObject("object").put("vartype", "const");
			for (int i = 0; i < where.length(); i++) {
				// System.out.println("[DEBUG] " +
				// result.getJSONArray("where").length());
				where.getJSONObject(i).getJSONArray("triples").put(triple_temp);
			}
		}

		// System.out.println("Adding Person");

		if (person.size() == 1) {
			JSONObject copyA = new JSONObject(where.getJSONObject(0).toString());
			JSONObject tripleA = new JSONObject(empty_triple.toString());
			JSONObject tripleB = new JSONObject(empty_triple.toString());
			JSONObject copyB;
			if (where.length() <= 1) {
				copyB = new JSONObject(where.getJSONObject(0).toString());
			} else {
				copyB = new JSONObject(where.getJSONObject(1).toString());
			}
			tripleA.getJSONObject("property").put("word", "black");
			tripleA.getJSONObject("object").put("word", person.peek());
			tripleA.getJSONObject("object").put("vartype", "const");
			copyA.getJSONArray("triples").put(tripleA);
			where.put(0, copyA);
			tripleB.getJSONObject("property").put("word", "white");
			tripleB.getJSONObject("object").put("word", person.pop());
			tripleB.getJSONObject("object").put("vartype", "const");
			copyB.getJSONArray("triples").put(tripleB);
			where.put(1, copyB);

		}

		if (location.size() == 1) {
			JSONObject copyA = new JSONObject(where.getJSONObject(0).toString());
			JSONObject tripleA = new JSONObject(empty_triple.toString());
			JSONObject tripleB = new JSONObject(empty_triple.toString());
			JSONObject copyB;
			if (where.length() <= 1) {
				copyB = new JSONObject(where.getJSONObject(0).toString());
			} else {
				copyB = new JSONObject(where.getJSONObject(1).toString());
			}
			tripleA.getJSONObject("property").put("word", "site");
			tripleA.getJSONObject("object").put("word", location.peek());
			tripleA.getJSONObject("object").put("vartype", "const");
			copyA.getJSONArray("triples").put(tripleA);
			where.put(0, copyA);
			tripleB.getJSONObject("property").put("word", "site");
			tripleB.getJSONObject("object").put("word", location.pop());
			tripleB.getJSONObject("object").put("vartype", "const");
			copyB.getJSONArray("triples").put(tripleB);
			where.put(1, copyB);

		}

		eco.clear();
		person.clear();
		location.clear();

		// if(person.size() < 2) {
		// //rotiere mittels Union durch alle möglichen Kombinationen oder tue
		// nichts.
		// }
		// add triples to result
		if (where.length() == 0) {
			JSONArray and = new JSONArray();
			JSONObject rel = new JSONObject();
			rel.put("type", "AND");
			rel.put("triples", and);
			where.put(rel);
			result.put("where", where);
		}

		else {
			result.put("where", where);
		}

		if (setOptions) {
			result.put("setOptions", true);
			result.put("options", options);
		}
		
		return result.toString();

	}

	/**
	 * Adds a template fragment
	 * 
	 * @param fragment
	 *            fragment to add
	 */
	public void addFragment(JSONObject fragment) {
		try {
			String type = fragment.getString("a");
			switch (type) {
			case "classvar":
				classes.add(fragment);
				break;
			case "propvar":
				properties.add(fragment);
				break;
			case "template":
				fragments.add(fragment);
				break;
			}
		} catch (JSONException e) {
			System.err.println(e);
			System.err
					.println("[ERROR] in TemplateFactory: in addFragment(JSONObject): object doesn't have a variable \"a\"");
		}
	}

	/**
	 * Adds a word to the list of remaining words
	 * 
	 * @param word
	 */
	public void addWord(TaggedWord word) {
		remainingWords.add(word);
	}

	/**
	 * Tests if the two given relations should be merged.
	 * 
	 * @param relA
	 *            first relation
	 * @param relB
	 *            second relation
	 * @return true if they should be merged, else false.
	 */
	private boolean mergeRelations(JSONObject relA, JSONObject relB) {
		JSONArray triplesA = relA.getJSONArray("triples");
		JSONArray triplesB = relB.getJSONArray("triples");
		try {
			for (int i = 0; i < triplesA.length(); i++) {
				String propA = triplesA.getJSONObject(i).getJSONObject("property").getString("word");
				if (propA.contains("black") || propA.contains("white")) {
					for (int j = 0; j < triplesB.length(); j++) {
						String propB = triplesB.getJSONObject(j).getJSONObject("property").getString("word");
						if ((propB.contains(propA) || propA.contains(propB)) && !propB.equals(propA)) {
							return true;
						}
					}
				}
			}
		} catch (JSONException e) {
			System.err.println("[ERROR] in TemplateFactory: mergeRelations(..): " + e);
			return false;
		}
		return false;
	}

	/**
	 * Merges two relations: relB into relA
	 * 
	 * @param relA
	 * @param relB
	 */
	private JSONObject mergeRels(JSONObject relA, JSONObject relB) {
		// System.out.println("[DEBUGINFO] merging:");
		// System.out.println(relA);
		// System.out.println(relB);
		JSONObject result = new JSONObject(relA.toString());
		JSONArray result_arr = new JSONArray(relA.getJSONArray("triples").toString());
		try {
			JSONArray triplesB = relB.getJSONArray("triples");
			for (int i = 0; i < triplesB.length(); i++) {
				result_arr.put(triplesB.getJSONObject(i));
			}
			result.put("triples", result_arr);
		} catch (JSONException e) {
			System.err.println("[ERROR] in TemplateFactory: mergeRels(..): " + e);
		}
		// System.out.println("[DEBUGINFO] merge result:");
		// System.out.println(result);
		return result;
	}

	private final LinkedList<JSONObject> fragments;
	private final LinkedList<JSONObject> classes;
	private final LinkedList<JSONObject> properties;
	private final LinkedList<TaggedWord> remainingWords;
}