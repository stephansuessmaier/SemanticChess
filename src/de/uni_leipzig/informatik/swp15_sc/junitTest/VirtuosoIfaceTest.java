package de.uni_leipzig.informatik.swp15_sc.junitTest;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import com.hp.hpl.jena.query.*;

import de.uni_leipzig.informatik.swp15_sc.triplestoreinterface.VirtuosoQueryInterface;

/**
 * This is the unit test class for the de.uni_leipzig.informatik.swp15_sc.triplestoreinterface.VirtuosoQueryInterface.
 * Hint: you need a running Virtuoso instance on localhost:8890, containing a
 * graph named "metagraph". If your setup differs from that, please change the
 * parameters of the constructor call in the first line of setUp().
 * @author Stephan Suessmaier
 *
 */
public class VirtuosoIfaceTest {

	@Before
	public void setUp() throws Exception {
		iface = new VirtuosoQueryInterface ("http://localhost:8890/metagraph");
		
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

	private VirtuosoQueryInterface iface;
	private String query;
}
