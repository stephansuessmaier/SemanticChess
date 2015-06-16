package de.uni_leipzig.informatik.swp15_sc.nlpmodule;

import java.util.LinkedList;

import org.json.*;

import com.hp.hpl.jena.query.*;

import de.uni_leipzig.informatik.swp15_sc.triplestoreinterface.*;
import de.uni_leipzig.informatik.swp15_sc.utils.Timer;
import de.uni_leipzig.informatik.swp15_sc.utils.TemplateUtils;

/**
 * This class is the "Interface" for using the de.uni_leipzig.informatik.swp15_sc.nlpmodule and the layers below to
 * answer questions in natural language.
 * 
 * @author Stephan Suessmaier
 *
 */
public class NLPModule {
	/**
	 * Creates and configures a new de.uni_leipzig.informatik.swp15_sc.nlpmodule.
	 * 
	 * @param configAsJson
	 *            content of a JSON-formatted de.uni_leipzig.informatik.swp15_sc.config file.
	 */
	public NLPModule(String configAsJson) {
		config = new JSONObject(configAsJson);
		/*
		 * First, create the objects that rely on parameters from the json
		 * de.uni_leipzig.informatik.swp15_sc.config file. The program terminates if the de.uni_leipzig.informatik.swp15_sc.config file is invalid.
		 */
		try {
			// decide which interface to create
			boolean useVirtuoso = config.getBoolean("useVirtuoso");
			if (useVirtuoso) {
				if (config.getBoolean("setJdbcUri")) {
					iface = new VirtuosoQueryInterface(
							config.getString("graphName"),
							config.getString("jdbcUri"));
				} else {
					iface = new VirtuosoQueryInterface(
							config.getString("graphName"));
				}
			} else {
				iface = new TurtleQueryInterface(config.getString("turtleFile"));
			}
			
			System.setProperty("wordnet.database.dir", config.getString("wordnetDatabaseDir"));

			String[] classes = fetchClasses();
			String[] properties = fetchProperties();
			System.out.println("[INFO] NLPModule: fetched " + classes.length + " classes, " + properties.length + " properties.");
			// second, create the QuestionParser
			parser = new QuestionParser(config.getString("lexiconFile"), classes, properties);
			
			// then create the QuestionTagger and QueryGenerator
			tagger = new QuestionTagger();
			generator = new QueryGenerator(properties, classes);
		} catch (JSONException e) {
			System.err.println(e);
			System.err
					.println("[ERROR] configuration invalid, check de.uni_leipzig.informatik.swp15_sc.config file.");
			System.exit(-1);
		}
	}

	/**
	 * Method to process (and answer) a natural language question
	 * 
	 * @param question
	 *            question as "natural format"
	 * @return answer table as JSON-formatted String
	 */
	public ResultSet process(String question) {
		timer = new Timer();
		timer.start();
		TaggedWord[] sentence = tagger.tagQuestion(question);
		JSONObject template = parser.parseQuestion(sentence);
		try {
			generated_template = TemplateUtils.makePretty(template);
			System.out.println("[INFO] " + generated_template);
		} catch (Exception e) {
			System.err.println(e);
		}
		String sparql = generator.generateQuery(template);
		timer.stop();
		generating_time = timer.getTime();
		timer.reset();
		timer.start();
		ResultSet executed = iface.executeQuery(sparql);
		timer.stop();
		execution_time = timer.getTime();
		return executed;
	}
	
	public String getTemplate () {
		return generated_template;
	}
	
	public long getGeneratingTime () {
		return generating_time;
	}
	
	public long getExecutionTime () {
		return execution_time;
	}

	/**
	 * Gets a list with available properties from the de.uni_leipzig.informatik.swp15_sc.triplestoreinterface
	 * @return list with properties (full URIs)
	 */
	public String[] fetchProperties() {
		// create query string to find out available properties
		String query = "select distinct ?y";
		if (config.getBoolean("useVirtuoso")) {
			query += " from <" + config.getString("graphName") + ">";
		}
		query += " where { ?x ?y ?z. }";

		ResultSet results = iface.executeQuery(query);
		LinkedList<String> uris = new LinkedList<String>();
		while (results.hasNext()) {
			uris.add(results.next().get("y").toString());
		}
		
		String[] result = new String[uris.size()];
		uris.toArray(result);
		return result;
//		String queryResult = iface.executeQuery(query);
//		return JsonTableUtils.createList(queryResult);
	}
	
	/**
	 * Gets a list with available classes from the de.uni_leipzig.informatik.swp15_sc.triplestoreinterface
	 * @return list with classes (full URIs)
	 */
	public String[] fetchClasses() {
		String query = "select distinct ?y";
		if (config.getBoolean("useVirtuoso")) {
			query += " from <" + config.getString("graphName") + ">";
		}
		query += " where { [] a ?y. }";
		ResultSet results = iface.executeQuery(query);
		LinkedList<String> uris = new LinkedList<String>();
		while (results.hasNext()) {
			uris.add(results.next().get("y").toString());
		}
		
		String[] result = new String[uris.size()];
		uris.toArray(result);
		return result;
		
//		String queryResult = iface.executeQuery(query);
//		return JsonTableUtils.createList(queryResult);
	}

	/**
	 * Checks if given String (after "at" or "in")is an Event
	 * @param locationOrEvent String which has to be checked
	 * @return true, if given String is an Event, else, false
	 */
	public boolean isEvent(String locationOrEvent) {
		String query = "select distinct ?game";
		if (config.getBoolean("useVirtuoso")) {
			query += " from <" + config.getString("graphName") + ">";
		}
		query += " where { ?game <http;//examplecom/prop/event> \"" + locationOrEvent + "\" }";

		ResultSet queryResult = iface.executeQuery(query);
		
		if (queryResult.hasNext()) {
			return true;
		} else {
			return false;
		}
	}
	
	private TripleStoreInterface iface;
	private QuestionTagger tagger;
	private QuestionParser parser;
	private QueryGenerator generator;
	private String generated_template;
	private final JSONObject config;
	private Timer timer;
	private long generating_time;
	private long execution_time;
}
