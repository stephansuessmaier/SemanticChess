package de.uni_leipzig.informatik.swp15_sc.nlpmodule;

import java.util.LinkedList;

import org.json.*;
import com.hp.hpl.jena.query.*;

import de.uni_leipzig.informatik.swp15_sc.utils.IOUtils;
import de.uni_leipzig.informatik.swp15_sc.utils.JsonTableUtils;
import de.uni_leipzig.informatik.swp15_sc.utils.TemplateUtils;

/**
 * This is a simple test program for the command line. Usage: execute, simply
 * enter your question and hit enter.
 * It implements an integration test for the packages de.uni_leipzig.informatik.swp15_sc.nlpmodule and de.uni_leipzig.informatik.swp15_sc.triplestoreinterface.
 * @author Stephan Suessmaier
 *
 */
public class CliTester {
	/**
	 * Main method.
	 * @param args Command line arguments
	 */
	public static void main (String[] args) {
		//test1();
		//test2();
		//testQuestionTagger();
		//test3();
		test4();
	}
	
	/**
	 * Test method, uses turtle file
	 */
	public static void test1 () {
		String path = System.getProperty ("user.home") + System.getProperty("file.separator");
		//erzeuge Konfiguration
		JSONObject config = new JSONObject();
		config.put("questionFile", path + "questions.txt");
		config.put("useVirtuoso", false);
		config.put("turtleFile", path + "complete.ttl");
		//erzeuge de.uni_leipzig.informatik.swp15_sc.nlpmodule mit de.uni_leipzig.informatik.swp15_sc.config, dieses erzeugt alles andere
		NLPModule nlp = new NLPModule(config.toString());
		
		try {
			// read question from console and process it.
			String question = IOUtils.readln ("ask");
			ResultSet answer = nlp.process(question);
			System.out.println(answer);
//			JsonTableUtils.printJsonTable(answer);
		}
		catch (Exception e) {
			System.err.println (e);
		}
	}
	
	/**
	 * Test method, uses virtuoso
	 */
	public static void test2 () {
		String path = System.getProperty ("user.home") + System.getProperty("file.separator");
		//erzeuge Konfiguration
		JSONObject config = new JSONObject();
		config.put("questionFile", path + "questions.txt");
		config.put("useVirtuoso", true);
		config.put("setJdbcUri", true);
		config.put("jdbcUri", "jdbc:virtuoso://srv.vpn:1111");
		config.put("graphName", "http://localhost:8890/metagraph");
		//erzeuge de.uni_leipzig.informatik.swp15_sc.nlpmodule mit de.uni_leipzig.informatik.swp15_sc.config, dieses erzeugt alles andere
		NLPModule nlp = new NLPModule(config.toString());
		
		try {
			// read question from console and process it.
			String question = IOUtils.readln ("ask");
			ResultSet answer = nlp.process(question);
			System.out.println(answer);
//			JsonTableUtils.printJsonTable(answer);
		}
		catch (Exception e) {
			System.err.println (e);
		}
	}
	
	public static void testQuestionTagger () {
		QuestionTagger tagger = new QuestionTagger ();
		TaggedWord[] words = tagger.tagQuestion("which games were played at the nordic championships?");
		for (TaggedWord word: words) {
			System.out.println (word);
		}
	}
	
	public static void test3 () {
		String path = System.getProperty ("user.home") + System.getProperty("file.separator");
		//erzeuge Konfiguration
		JSONObject config = new JSONObject();
		config.put("questionFile", path + "questions.txt");
		config.put("useVirtuoso", true);
		config.put("setJdbcUri", true);
		config.put("jdbcUri", "jdbc:virtuoso://srv.vpn:1111");
		config.put("graphName", "http://localhost:8890/metagraph");
		//erzeuge de.uni_leipzig.informatik.swp15_sc.nlpmodule mit de.uni_leipzig.informatik.swp15_sc.config, dieses erzeugt alles andere
		NLPModule nlp = new NLPModule(config.toString());
		
		String[] props = nlp.fetchProperties();
		for (String prop: props) {
			System.out.println (prop);
		}
	}
	
	public static void test4 () {
		JSONObject json = new JSONObject(IOUtils.readFile("/home/stephan/lexicon.json"));
		JSONObject uniontest = json.getJSONObject("average");
		JSONObject test2 = json.getJSONObject("good");
		try {
			System.out.println(TemplateUtils.makePretty(uniontest));
			System.out.println(TemplateUtils.makePretty(test2));
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
