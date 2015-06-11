package de.uni_leipzig.informatik.swp15_sc.triplestoreinterface;

import com.hp.hpl.jena.query.*;

/**
 * This interface defines a method to execute SPARQL-query Strings.
 * @author Stephan Suessmaier
 *
 */
public interface TripleStoreInterface {
	/**
	 * Method to execute a SPARQL-query.
	 * IMPORTANT: The query is required to be a SELECT query.
	 * @param query SPARQL-query
	 * @return Result as String
	 */
	public abstract ResultSet executeQuery (String query);
}
