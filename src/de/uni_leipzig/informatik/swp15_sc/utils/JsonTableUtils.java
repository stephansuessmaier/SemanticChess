package de.uni_leipzig.informatik.swp15_sc.utils;

import org.json.*;

/**
 * This class contains static functions to extract a table out of a JSON String.
 * @author Stephan Suessmaier
 *
 */
public class JsonTableUtils {
	/**
	 * This function creates a 2-dimensional String array out of a JSON String
	 * as it is returned by the Jena Framework as result of a SPARQL select
	 * query.
	 * 
	 * @param jsonStr
	 *            JSON-formatted String containing the table.
	 * @return table as String[i][j], where i specifies the column and j the
	 *         row.
	 */
	public static String[][] createStringTable(String jsonStr) {
		try {
			// get the arrays out of that object
			JSONObject json = new JSONObject(jsonStr);
			JSONArray head = json.getJSONObject("head").getJSONArray("vars");
			JSONArray values = json.getJSONObject("results").getJSONArray(
					"bindings");

			// get size of resulting table
			int varCount = head.length();
			int lines = values.length();

			// important: variables need a line, too.
			lines++;

			// create result table and fill it with values
			String[][] result = new String[varCount][lines];
			for (int j = 0; j < lines; j++) {
				for (int i = 0; i < varCount; i++) {
					if (j == 0) {
						result[i][j] = head.getString(i);
					} else {
						result[i][j] = values.getJSONObject(j - 1)
								.getJSONObject(result[i][0]).getString("value");
					}
				}
			}

			return result;
		} catch (JSONException e) {
			System.err.println(e);
			return null;
		}
	}

	/**
	 * works like createStringTable(), but returns only the first (maybe only)
	 * column.
	 * 
	 * @param jsonStr
	 *            JSON-formatted String containing the table.
	 * @return the List (column)
	 */
	public static String[] createList(String jsonStr) {
		try {
			JSONObject json = new JSONObject(jsonStr);
			JSONArray values = json.getJSONObject("results").getJSONArray(
					"bindings");
			// get name of variable for first column
			String var = json.getJSONObject("head").getJSONArray("vars")
					.getString(0);
			int size = values.length();
			String[] result = new String[size];

			for (int i = 0; i < size; i++) {
				result[i] = values.getJSONObject(i).getJSONObject(var)
						.getString("value");
			}

			return result;
		} catch (JSONException e) {
			System.err.println(e);
			return null;
		}
	}

	/**
	 * This function creates a html-table out of a JSON String as it is returned
	 * by the Jena Framework as result of a SPARQL select query.
	 * 
	 * @param jsonStr
	 *            JSON-formatted String containing the table.
	 * @return table as String[i][j], where i specifies the column and j the
	 *         row.
	 */
	public static String createHtmlTable(String jsonStr) {
		/*
		 * Result looks like this: <table> <tr> <td>...</td> ... </tr> </table>
		 * it doesn't return a full html document, only the table.
		 */
		try {
			String result = "<table border=\"1\">";
			// get the arrays out of that object
			JSONObject json = new JSONObject(jsonStr);
			JSONArray head = json.getJSONObject("head").getJSONArray("vars");
			JSONArray values = json.getJSONObject("results").getJSONArray(
					"bindings");

			// get table size
			int varCount = head.length();
			int lines = values.length();
			lines++; // for variable names

			// 1 loop-round = 1 table row
			for (int j = 0; j < lines; j++) {
				result += "<tr>";

				// 1 loop-round = 1 column in that row = 1 cell
				for (int i = 0; i < varCount; i++) {
					if (j == 0) {
						result += "<th>" + head.getString(i) + "</th>";
					} else {
						String val = values.getJSONObject(j - 1)
								.getJSONObject(head.getString(i))
								.getString("value");
						String type = values.getJSONObject(j - 1)
								.getJSONObject(head.getString(i))
								.getString("type");

						// if the result is an URI, create a link
						if (type.equals("uri")) {
							result += "<td><a href=\"" + val + "\">" + val
									+ "</a></td>";
						} else {
							result += "<td>" + val + "</td>";
						}
					}
				}

				result += "</tr>";
			}
			return result;
		} catch (JSONException e) {
			System.err.println(e);
			return "<table><tr><td>NO RESULTS FOUND</td></tr></table>";
		}
	}

	/**
	 * This procedure prints a JSON-table on console in a human-readable format.
	 * 
	 * @param jsonStr
	 *            JSON-formatted String containing the table.
	 */
	public static void printJsonTable(String jsonStr) {
		String[][] table = createStringTable(jsonStr);
		if (table == null) {
			System.out.println("NO RESULTS FOUND");
		} else {
			for (int i = 0; i < table[0].length; i++) {
				for (int j = 0; j < table.length; j++) {
					System.out.print(table[j][i] + " | ");
				}
				System.out.println();
			}
		}
	}
}
