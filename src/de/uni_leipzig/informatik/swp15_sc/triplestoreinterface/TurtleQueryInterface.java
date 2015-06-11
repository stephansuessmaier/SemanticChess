package de.uni_leipzig.informatik.swp15_sc.triplestoreinterface;

import java.io.ByteArrayOutputStream;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.resultset.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * This class implements the de.uni_leipzig.informatik.swp15_sc.triplestoreinterface to query RDF/Turtle files by
 * using the Apache Jena framework.
 * @author Stephan Suessmaier
 *
 */
public class TurtleQueryInterface implements TripleStoreInterface {
	/**
	 * Creates a new TurtleQueryInterface, which uses the given RDF/Turtle file
	 * as data source.
	 * @param path Path to RDF/Turtle file
	 */
	public TurtleQueryInterface (String path) {
		model = ModelFactory.createDefaultModel ();
		//read data into model
		model.read (path, "TURTLE");
	}
	
	/**
	 * See "de.uni_leipzig.informatik.swp15_sc.triplestoreinterface".
	 */
	public ResultSet executeQuery (String query) {
		// create query object and a "query executor" for a Jena model
		Query q = QueryFactory.create (query);
		QueryExecution qexec = QueryExecutionFactory.create (q, model);
		
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
	 * Jena's models are an abstract representation of RDF graphs.
	 */
	private final Model model;
}