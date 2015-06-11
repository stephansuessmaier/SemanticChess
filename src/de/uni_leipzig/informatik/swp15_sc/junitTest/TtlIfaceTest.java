package de.uni_leipzig.informatik.swp15_sc.junitTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import com.hp.hpl.jena.query.*;

import de.uni_leipzig.informatik.swp15_sc.triplestoreinterface.TurtleQueryInterface;

import java.io.InputStream;

/**
 * This is the unit test class for de.uni_leipzig.informatik.swp15_sc.triplestoreinterface.TurtleQueryInterface.
 * Hint: you need an RDF/Turtle file named complete.ttl in your home directory.
 * @author Stephan Suessmaier
 *
 */
public class TtlIfaceTest {
	
	@Before
	public void setup () throws Exception {
		// get System properties to access the user's home on any OS
		String path = System.getProperty("user.home");
		String filesep = System.getProperty("file.separator");
		iface = new TurtleQueryInterface (path + filesep + "complete.ttl");
		
		// read query from query0.txt in this package
		InputStream in = getClass().getResourceAsStream("query0.txt");
		int size = in.available();
		byte[] data = new byte[size];
		in.read(data);
		query = new String (data);
		
		// close stream
		in.close();
	}

	@Test
	public void test() {
		ResultSet result = iface.executeQuery(query);
		assertNotNull ("there should be a result.", result);
	}

	private TurtleQueryInterface iface;
	private String query;
}
