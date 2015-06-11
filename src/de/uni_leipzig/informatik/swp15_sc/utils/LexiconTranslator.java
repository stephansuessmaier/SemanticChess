package de.uni_leipzig.informatik.swp15_sc.utils;

import org.json.*;

/**
 * This class is for translating the lexicon from the old into the new format.
 * @author Stephan Suessmaier
 *
 */
public class LexiconTranslator {
	/**
	 * Main method
	 * @param args command line arguments
	 */
	public static void main (String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: java LexiconTranslator <infile> <outfile>");
			return;
		}
		JSONObject lexicon = new JSONObject(IOUtils.readFile(args[0]));
		String result = "{ ";
		String[] keys = JSONObject.getNames(lexicon);
		for (String key: keys) {
			try {
				JSONObject value = lexicon.getJSONObject(key);
				String newValue = TemplateUtils.makePretty(value);
				result += '"' + key + "\":\"" + newValue + "\",\n";
			}
			catch (Exception e) {
				continue;
			}
		}
		
		byte[] data = result.getBytes();
		IOUtils.saveData(data, args[1]);
	}
}
