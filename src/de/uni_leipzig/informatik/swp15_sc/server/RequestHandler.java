package de.uni_leipzig.informatik.swp15_sc.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.uni_leipzig.informatik.swp15_sc.nlpmodule.NLPModule;
import de.uni_leipzig.informatik.swp15_sc.utils.JsonTableUtils;
import de.uni_leipzig.informatik.swp15_sc.utils.Timer;

import org.apache.commons.io.*;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.*;

/*
 * funktional
 */
public class RequestHandler implements HttpHandler {
	public RequestHandler(String configAsJson) {
		nlp = new NLPModule(configAsJson);
		JSONObject conf = new JSONObject(configAsJson);
		previous = new LinkedList<HtmlDocument>();
		next = new LinkedList<HtmlDocument>();
		try {
			css = de.uni_leipzig.informatik.swp15_sc.utils.IOUtils.readFile(conf.getString("cssFile"));
		} catch (JSONException e) {
			css = null;
		}
		timer = new Timer();
	}

	public void handle(HttpExchange exchange) throws IOException {
		HtmlDocument response;
		if (css == null) {
			response = new HtmlDocument("Semantic Chess");
		} else {
			response = new HtmlDocument("Semantic Chess", css);
		}
		response.appendText("ask your question here:");
		response.appendInputForm("question", "text");
		String requestMethod = exchange.getRequestMethod();
		try {
			URI uri = exchange.getRequestURI();

			// Saves URI into a List of NameValuePairs. For example, a URI with
			// "?demo=Show+all+matches+of+Magnus Carlsen"
			// would be saves as name: demo |
			// value:"Show all matches of Magnus Carlsen"
			List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");
			if (!params.isEmpty()) {
				NameValuePair pair = params.get(0);
				if (pair.getValue().toLowerCase().equals("why") || pair.getValue().toLowerCase().equals("why?")) {
					response.appendText("42");
					previous.clear();
					next.clear();
				} else if (pair.getValue().equals("1")) {
					if (next.isEmpty()) {
						timer.reset();
						timer.start();
						response.appendResultTable(rs);
						timer.stop();
						response.appendHtml("<hr><p>generated in " + timer.getTime() + " seconds.</p>");
					} else {
						response = next.pop();
					}
					previous.push(response);
				} else if (pair.getValue().equals("0")) {
					next.push(previous.pop());
					response = previous.peek();
				} else {
					timer.reset();
					timer.start();
					previous.clear();
					next.clear();
					System.out.println("[INFO] question: " + pair.getValue());
					response.appendText(pair.getValue());
					rs = nlp.process(pair.getValue());
					response.appendText("template:<br>" + nlp.getTemplate());
					response.appendResultTable(rs);
					timer.stop();
					response.appendHtml("<hr><p>generated in " + timer.getTime() + " msec, NLPModule: " + nlp.getGeneratingTime() + "(generating), " + nlp.getExecutionTime() + "(execution)</p>");
					previous.push(response);
				}
			}

			// output
			exchange.sendResponseHeaders(200, response.length());
			OutputStream os = exchange.getResponseBody();
			os.write(response.getData());
			os.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private final NLPModule nlp;
	private ResultSet rs;
	private LinkedList<HtmlDocument> previous;
	private LinkedList<HtmlDocument> next;
	private Timer timer;
	private String css;
}
