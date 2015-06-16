package de.uni_leipzig.informatik.swp15_sc.utils;

import org.json.*;
import java.util.LinkedList;

/**
 * This class contains functions to handle query templates (JSON)
 * 
 * @author Stephan Suessmaier
 *
 */
public class TemplateUtils {
	/**
	 * creates a more beautiful representation of a given SPARQL template
	 * 
	 * @param template
	 *            SPARQL template as ugly JSON
	 * @return SPARQL template as beautiful String
	 */
	public static String makePretty(JSONObject template) throws Exception {
		String result = "select ";

		// SELECT
		JSONArray select = template.getJSONArray("select");

		// test if select is empty
		if (select.length() == 0) {
			// if no variable selected: select *
			result += "* ";
		} else {
			for (int i = 0; i < select.length(); i++) {
				JSONObject var = select.getJSONObject(i);
				if (template.getBoolean("countSelect") && var.has("count") && var.getBoolean("count")) {
					result += "count(" + varToString(select.getJSONObject(i)) + ") ";
					if (var.has("alias"))
						result += "as ?" + var.getString("alias") + " ";
				} else {
					result += varToString(select.getJSONObject(i)) + " ";
				}
			}
		}

		// WHERE
		result += "where { ";
		JSONArray where = template.getJSONArray("where");

		boolean union = false;
		if (where.length() > 1)
			union = true;

		for (int j = 0; j < where.length(); j++) {
			if (union) {
				result += "{ ";
			}
			JSONArray triples = where.getJSONObject(j).getJSONArray("triples");
			for (int i = 0; i < triples.length(); i++) {
				JSONObject triple = triples.getJSONObject(i);
				result += varToString(triple.getJSONObject("subject")) + " "
						+ varToString(triple.getJSONObject("property")) + " "
						+ varToString(triple.getJSONObject("object")) + ". ";
			}

			if (union)
				result += " } ";
			if (j < where.length() - 1)
				result += " union ";
		}
		result += "} ";
		// ORDERBY, LIMIT
		// if (template.getBoolean("setOptions")) {
		if (template.has("options")) {
			JSONObject options = template.getJSONObject("options");
			result += "order by ";
			// if (options.has("orderbycount")
			// && options.getBoolean("orderbycount")) {
			// // IMPORTANT! this needs to be improved, too.
			// result += "count("
			// + varToString(where.getJSONObject(0)
			// .getJSONArray("triples").getJSONObject(0)
			// .getJSONObject("object")) + ") ";
			// }
			if (options.has("orderby")) {
				result += options.getString("orderby");
			}
			if (options.has("limit")) {
				result += " limit " + options.getInt("limit");
			}
		}

		return result;
	}

	/**
	 * generates a string representation of a given variable
	 * 
	 * @param var
	 *            variable
	 * @return String representation of var
	 */
	public static String varToString(JSONObject var) {
		String result = "";

		// constants are represented in ""
		if (var.getString("vartype").equals("const")) {
			result += "\"" + var.getString("word") + "\"";
		}
		// slot variables start with a :
		else if (var.getString("vartype").equals("slot")) {
			result += ":" + var.getString("word");
		}
		// other variables start with a ?
		else {
			result += "?" + var.getString("word").toLowerCase();
		}

		return result;
	}

	/**
	 * compiles a SPARQL query-template string to the JSON-template format
	 * 
	 * @param query
	 * @return
	 */
	public static JSONObject compile(String query) throws Exception {
		JSONObject result = new JSONObject();
		// will be set to true if necessary (see else if (... matches
		// (orderbypattern))
		result.put("countSelect", false);
		JSONArray select = new JSONArray();
		JSONArray where = new JSONArray();
		JSONObject options = new JSONObject();

		// should we set options?
		if (query.contains("order by") || query.contains("limit")) {
			result.put("setOptions", true);
		} else {
			result.put("setOptions", false);
		}

		// single expressions in query are usually seperated by whitespace
		String[] parts = query.split(" ");

		/*------------------------SELECT------------------------*/

		// the first expression must be select (in a template)
		if (!parts[0].toLowerCase().equals("select"))
			throw new Exception(errmessage + parts[0]);

		// iterate over variable list that the select-expression is followed by
		boolean cont = true;
		int index = 1;
		while (cont && index < parts.length) {
			if (parts[index].matches(varpattern)) {
				JSONObject variable = new JSONObject();
				int lastindex = parts[index].length();

				variable.put("word", parts[index].substring(1, lastindex));
				variable.put("vartype", "var");
				select.put(variable);
				index++;
			} else if (parts[index].equals("*")) {
				index++;
				cont = false;
			} else if (parts[index].equals("")) {
				index++;
			} else if (parts[index].matches(countpattern)) {
				if (index + 2 < parts.length && parts[index + 1].toLowerCase().equals("as")
						&& parts[index + 2].matches(varpattern)) {
					result.put("countSelect", true);
					JSONObject variable = new JSONObject();
					variable.put("vartype", "var");
					variable.put("count", true);

					int start = parts[index].indexOf("(");
					int end = parts[index].indexOf(")");
					String varname = parts[index].substring(start + 2, end);
					variable.put("word", varname);

					String aliasname = parts[index + 2].substring(1, parts[index + 2].length());
					variable.put("alias", aliasname);

					select.put(variable);
					index = index + 3;
				} else {
					String error = "";
					if (index + 2 >= parts.length)
						error = "index+2 >= parts.length";
					else if (!parts[index + 1].toLowerCase().equals("as"))
						error = "parts[index+1] not \"as\"";
					else
						error = "parts[index+2] does not match varpattern";
					throw new Exception(errmessage + error);
				}
			} else {
				cont = false;
			}
		}

		/*------------------------WHERE------------------------*/

		if (!parts[index].toLowerCase().equals("where")) {
			throw new Exception(errmessage + "template does not contain a where (at " + parts[index] + ")");
		}

		LinkedList<JSONObject> variables = new LinkedList<JSONObject>();
		JSONArray triples = new JSONArray();

		// for opening {-brackets
		int open = 0;
		int varcount = 0;
		cont = true;
		while (cont && index < parts.length) {
			if (parts[index].equals("{")) {
				open++;
			} else if (parts[index].equals("}")) {
				// insert triples into where, but only if not empty
				if (triples.length() != 0) {
					JSONObject relation = new JSONObject();
					relation.put("type", "AND");
					relation.put("triples", triples);
					if (open == 2) {
						// UNION is used -> create a new, empty triples array
						triples = new JSONArray();
					}
					where.put(relation);
				}
				open--;
			}
			// else if (!parts[index].equals("") && open == 0) {
			// cont = false;
			// }
			else if (parts[index].matches(varpattern) || parts[index].matches(slotpattern)) {
				JSONObject var = new JSONObject();

				if (parts[index].charAt(0) == ':')
					var.put("vartype", "slot");
				else
					var.put("vartype", "var");

				int lastindex = parts[index].length();
				if (parts[index].charAt(lastindex - 1) == '.')
					lastindex--;
				var.put("word", parts[index].substring(1, lastindex));
				switch (varcount % 3) {
				case 0:
					var.put("type", "subject");
					break;
				case 1:
					var.put("type", "property");
					break;
				case 2:
					var.put("type", "object");
					break;
				}
				variables.add(var);
				varcount++;

				if (variables.size() == 3) {
					JSONObject triple = new JSONObject();
					for (JSONObject jo : variables) {
						triple.put(jo.getString("type"), jo);
					}
					variables.clear();
					triples.put(triple);
				}
			} else if (parts[index].length() > 3 && (parts[index].matches(constpattern)
					|| parts[index].substring(0, parts[index].length() - 1).matches(constpattern))) {
				if (varcount % 3 != 2)
					throw new Exception(errmessage + "constants only allowed for object variables");
				JSONObject var = new JSONObject();
				var.put("type", "object");
				var.put("vartype", "const");
				int lastindex = parts[index].length()-1;
				if (parts[index].charAt(lastindex) == '.')
					lastindex--;
				var.put("word", parts[index].substring(1, lastindex));
				variables.add(var);
				varcount++;

				// const variables are always object variables, so
				// variables.size() is always 3
				// at this point
				JSONObject triple = new JSONObject();
				for (JSONObject jo : variables) {
					triple.put(jo.getString("type"), jo);
				}
				variables.clear();
				triples.put(triple);
			} else if (result.getBoolean("setOptions")) {
				if (parts[index].matches(number)) {
					options.put("limit", Integer.parseInt(parts[index]));
				} else if (parts[index].matches(orderbycountpattern)) {
					options.put("orderby", parts[index]);
					options.put("orderbycount", true);
				} else if (parts[index].matches(orderbypattern)) {
					options.put("orderby", parts[index]);
				}
			}
			index++;
		}

		// put result parts together
		result.put("select", select);
		result.put("where", where);

		if (result.getBoolean("setOptions")) {
			result.put("options", options);
		}

		return result;
	}

	// 3F is hex-value of ? (ASCII), 3A of :
	private static final String varpattern = "\\x3F\\S+";
	private static final String slotpattern = "\\x3A\\S+";
	// 0x27: '
	private static final String constpattern = "(\\x27)(\\S*\\s*)*(\\x27)";
	private static final String countpattern = "count\\x28" + varpattern + "\\x29";
	private static final String orderbycountpattern = "asc\\x28" + countpattern + "\\x29|desc\\x28" + countpattern
			+ "\\x29";
	private static final String number = "\\d+";
	private static final String orderbypattern = "asc\\x28" + varpattern + "\\x29|desc\\x28" + varpattern + "\\x29";
	private static final String errmessage = "[ERROR] compile(String) failed: query string format invalid: ";
}
