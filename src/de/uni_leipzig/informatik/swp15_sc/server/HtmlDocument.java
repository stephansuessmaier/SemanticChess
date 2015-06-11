package de.uni_leipzig.informatik.swp15_sc.server;

import java.util.List;

import com.hp.hpl.jena.query.*;

/**
 * This class represents an editable HTML document
 * @author Stephan Suessmaier
 *
 */
public class HtmlDocument {
	/**
	 * Creates a new HtmlDocument with the given title, which is also used as headline.
	 * @param title document title
	 */
	public HtmlDocument (String title) {
		content = "<html><head><title>" + title + "</title></head><body><h1>" + title + "</h1><hr>";
	}
	
	/**
	 * Creates a new HtmlDocument with the given Title and stylesheet.
	 * @param title document title
	 * @param css stylesheet
	 */
	public HtmlDocument (String title, String css) {
		content = "<html><head><title>" + title + "</title>"
				+ "<style type=\"text/css\">" + css + "</style>"
				+ "</head><body><h1>" + title + "</h1><hr>";
	}
	
	/**
	 * Appends plaintext to the document.
	 * @param text text to append
	 */
	public void appendText (String text) {
		content += "<p>" + text + "</p>";
	}
	
	/**
	 * Appends a HTML-fragment to the document
	 * @param html HTML-fragment to append
	 */
	public void appendHtml (String html) {
		content += html;
	}
	
	/**
	 * Appends an input form (text field) to the document.
	 * @param name name of the form (and of the variable it represents)
	 * @param type type of input data
	 */
	public void appendInputForm (String name, String type) {
		content += "<form action=\"\"><p>" + name + ":<br><input name=\"" + name 
				+ "\" type=\"" + type + "\" size=\"50\">"
						+ "<input type=\"submit\"/></p></form>";
	}
	
	/**
	 * Appends a drop down menu and a submit button to the document.
	 * @param options options for the drop down menu
	 * @param name variable name (for submission in GET-request)
	 */
	public void appendDropDownForm (String[] options, String name) {
		content += "<form action=\"\"><select name=\"" + name + "\">";
		for (String option: options) {
			content += "<option>" + option + "</option>";
		}
		content += "<input type=\"submit\"></input></select></form>";
	}
	
	/**
	 * Appends a button to navigate to the next page
	 */
	public void appendNextButton () {
		content += "<form action=\"\"><button type=\"submit\" name=\"resultpage\" value=\"1\">next</button></form>";
	}
	
	/**
	 * Appends a button to navigate to the previous page
	 */
	public void appendPreviousButton () {
		content += "<form action=\"\"><button type=\"submit\" name=\"resultpage\" value=\"0\">previous</button></form>";
	}
	
	public void appendResultTable (ResultSet rs) {
		if (rs.hasNext()) {
    		
    		// IMPORTANT: this needs to be changed in a future release !!!
    		
    		//response.appendHtml(JsonTableUtils.createHtmlTable(jsontable));
    		List<String> varnames = rs.getResultVars();
    		content += "<table border=\"1\"><tr>";
    		for (String varname: varnames) {
    			content += "<th>" + varname + "</th>";
    		}
    		content += "</tr>";
    		int currentRowNumber = rs.getRowNumber();
    		while (rs.hasNext() && rs.getRowNumber() < currentRowNumber + increment) {
    			QuerySolution sol = rs.next();
    			content += "<tr>";
    			for (String varname: varnames) {
    				String value = sol.get(varname).toString();
    				if (value.contains("http://")) {
    					content += "<td><a href=\"" + value + "\">" + value + "</a></td>";
    				} else {
    					content += "<td>" + value + "</td>";
    				}
    			}
    			content += "</tr>";
    		}
    		content += "</table>";
    		if (currentRowNumber > increment)
    			appendPreviousButton();
    		if (rs.hasNext())
    			appendNextButton();
    	}
    	else {
    		content += "[ERROR] Query execution returned no results.";
    	}
	}
	
	/**
	 * Returns the document as byte[].
	 * @return document as byte[]
	 */
	public byte[] getData () {
		return (content + "</body></html>").getBytes();
	}
	
	/**
	 * Computes the size of the document.
	 * @return size of the document
	 */
	public long length () {
		return "</body></html>".length() + content.length();
	}
	
	/**
	 * returns the document as HTML-String.
	 */
	public String toString () {
		return content + "</body></html>";
	}
	
	private String content;
	private static final int increment = 100;
}
