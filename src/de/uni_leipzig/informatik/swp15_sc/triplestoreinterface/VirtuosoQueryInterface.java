package de.uni_leipzig.informatik.swp15_sc.triplestoreinterface;

import java.io.ByteArrayOutputStream;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.resultset.JSONOutput;

import virtuoso.jena.driver.*;

/**
 * This class implements the de.uni_leipzig.informatik.swp15_sc.triplestoreinterface to query a Virtuoso triple store
 * by using the Jena framework and the Virtuoso Jena driver.
 * @author Stephan Suessmaier
 *
 */
public class VirtuosoQueryInterface implements TripleStoreInterface {
	/**
	 * Creates a new VirtuosoQueryInterface connected to virtuoso, which can be
	 * reached using the given URI.
	 * @param jdbcUri URI like "jdbc:virtuoso://[host]:[port]"
	 */
	public VirtuosoQueryInterface (String graphName, String jdbcUri) {
		//in virtuoso, dba is the default-user with password dba
		graph = new VirtGraph (graphName, jdbcUri, "dba", "dba");
		//graph.clear();
	}
	
	/**
	 * Creates a new VirtuosoQueryInterface connected to virtuoso, which must be
	 * listening at localhost:1111.
	 */
	public VirtuosoQueryInterface (String graphName) {
		graph = new VirtGraph (graphName, "jdbc:virtuoso://localhost:1111", "dba", "dba");
	}
	
	/**
	 * See "de.uni_leipzig.informatik.swp15_sc.triplestoreinterface".
	 */
	public ResultSet executeQuery (String query) {
		// Create query object and initialize "executor" with query and data model
		Query sparql = QueryFactory.create (query);
		VirtuosoQueryExecution qexec = VirtuosoQueryExecutionFactory.create (sparql, graph);
		
		//...then execute.
		ResultSet results = qexec.execSelect ();
		return results;
//		// format the resulting table as JSON:
//		JSONOutput output = new JSONOutput ();
//		ByteArrayOutputStream out = new ByteArrayOutputStream ();
//		output.format(out, results);
//		
//		return out.toString();
	}
	
	/**
	 * This represents the RDF-graph stored in virtuoso.
	 */
	private final VirtGraph graph;
}
