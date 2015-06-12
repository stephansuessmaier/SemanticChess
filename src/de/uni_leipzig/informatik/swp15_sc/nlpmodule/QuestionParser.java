package de.uni_leipzig.informatik.swp15_sc.nlpmodule;

import org.json.*;
import java.util.LinkedList;

/**
 * This class is for generating SPARQL query templates.
 * 
 * @author Stephan Suessmaier
 *
 */
public class QuestionParser {
	/**
	 * Creates a new QuestionParser
	 * 
	 * @param independentEntriesFile
	 *            path to file containing the independent lexicon
	 */
	public QuestionParser(String independentEntriesFile, String[] classes, String[] properties) {
		lexicon = new Lexicon(independentEntriesFile, classes, properties);
		factory = new TemplateFactory();
	}

	/**
	 * Parses a "pre-tagged" question and generates query templates
	 * 
	 * @param words
	 *            tagged words of the question
	 * @return array of possible query templates
	 */
	public JSONObject parseQuestion(TaggedWord[] words) {
		// test sentence against lexicon, fill factory properly
		testAgainstLexicon(words);

		// let the factory generate the templates
		JSONObject template = factory.generate();

		// set limit to 100 by default if not specified
		// for (JSONObject template: templates) {
		// if (!template.has("options")) {
		// JSONObject options = new JSONObject ();
		// options.put("limit", 100);
		// template.put("options", options);
		// template.put("setOptions", true);
		// }
		// else if (!template.getJSONObject("options").has("limit")) {
		// template.getJSONObject("options").put("limit", 100);
		// }
		// }
		return template;
	}

	/**
	 * Initializes the TemplateFactory with query fragments based on the entries
	 * of the independent lexicon
	 * 
	 * @param words
	 *            tagged words of the question
	 */
	private void testAgainstLexicon(TaggedWord[] words) {
		factory.clear();

		// lookup every word, and if succesful add query fragment
		for (int i = 0; i < words.length; i++) {
			for (int j = i; j < words.length; j++) {
				try {
					String lookup = "";
					for (int k = i; k <= j; k++) {
						if (k == i)
							lookup += words[k].getWord();
						else
							lookup += " " + words[k].getWord();
					}
					//JSONObject json = lexicon.searchFor(lookup);
					JSONObject json = new JSONObject(lexicon.searchFor(lookup).toString());
					factory.addFragment(json);
				} catch (Exception e) {
					// if j-loop ends (end of current subsentence is end of
					// sentence) and no matching
					// entry was found, add the first word (i) of the
					// subsentence to remainingwords
					if (j + 1 == words.length && isRelevant(words[i]))
						factory.addWord(words[i]);
					continue;
				}
			}
		}
	}

	/**
	 * Checks the TaggedWords relevance by using the pre-defined POS-tags
	 * 
	 * @param word
	 *            Word to check
	 * @return true if the word is relevant, else false.
	 */
	private boolean isRelevant(TaggedWord word) {
		// is it a name?
		if (!word.getNe().equals("O"))
			return true;

		// otherwise, does it have a relevant pos tag?
		String pos = word.getPos();
		int i = 0;
		while (i < relevant.length) {
			if (relevant[i].equals(pos)) {
				return true;
			}
			i++;
		}
		return false;
	}

	private final Lexicon lexicon;
	private final TemplateFactory factory;
	private static final String[] relevant = { "NN", "NNS", "NNP", "NNPS", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ" };
}
