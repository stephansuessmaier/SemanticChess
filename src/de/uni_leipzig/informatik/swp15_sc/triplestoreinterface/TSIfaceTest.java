package de.uni_leipzig.informatik.swp15_sc.triplestoreinterface;

import java.util.List;

import de.uni_leipzig.informatik.swp15_sc.utils.IOUtils;

import com.hp.hpl.jena.query.*;

/**
 * This class is for testing the TurtleQueryInterface and will be removed in the
 * final release.
 * @author Stephan Suessmaier
 *
 */
public class TSIfaceTest {

	public static void main(String[] args) {
		String query = "prefix onto:<http://pcai042.informatik.uni-leipzig.de/~swp13-sc/ChessOntology#> ";
		query += "select * where { ?x onto:black ?y } limit 10";
		test2(query);
	}
	
	/**
	 * Test #1 uses the TurtleQueryInterface to execute the query over a file.
	 * @param query SPARQL-query
	 */
	private static void test1 (String query, String turtlePath) {
		TripleStoreInterface iface = new TurtleQueryInterface (turtlePath);
		ResultSet result = iface.executeQuery(query);
		//JSONObject json = new JSONObject (result);
		
		//System.out.println (json.get("head"));
		//System.out.println (json.get("results"));
//		de.uni_leipzig.informatik.swp15_sc.utils.JsonTableUtils.printJsonTable(result);
//		System.out.println (de.uni_leipzig.informatik.swp15_sc.utils.JsonTableUtils.createHtmlTable(result));
		while (result.hasNext()) {
			System.out.println(result.next());
		}
	}
	
	/**
	 * Test #2 uses the VirtuosoQueryInterface to execute the Query over a Virtuoso
	 * triple store.
	 * @param query SPARQL-query
	 */
	private static void test2 (String query) {
		TripleStoreInterface iface = new VirtuosoQueryInterface ("http://localhost:8890/chess");
		ResultSet result = iface.executeQuery(query);
		System.out.println("[INFO] got out " + result.getRowNumber() + " results.");
		List<String> varnames = result.getResultVars();
		for (String varname: varnames)
			System.out.println(varname);
		while (result.hasNext()) {
			QuerySolution sol = result.next();
			for (String varname: varnames) {
				System.out.print (varname + ": " + sol.get(varname) + ", ");
			}
			System.out.println();
		}
	}

}
