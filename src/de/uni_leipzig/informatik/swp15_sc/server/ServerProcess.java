package de.uni_leipzig.informatik.swp15_sc.server;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.*;

import de.uni_leipzig.informatik.swp15_sc.utils.IOUtils;

import org.json.*;


/*
 * funktional
*/
public class ServerProcess {
    public static void main(String[] args) throws Exception {
    	if (args.length < 1) {
    		System.out.println("Usage: java -jar SemanticChess.jar <path to json de.uni_leipzig.informatik.swp15_sc.config file>");
    		return;
    	}
    	
        // HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        String config = IOUtils.readFile(args[0]);
        JSONObject conf = new JSONObject (config);
        int port;
        try {
        	port = conf.getInt("serverPort");
        }
        catch (Exception e) {
        	System.err.println("[INFO] no port configured, using default port 8080 instead.");
        	port = 0;
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        RequestHandler handler = new RequestHandler (config);
        server.createContext("/", handler);
        server.setExecutor(null);
        server.start();
    }
}
