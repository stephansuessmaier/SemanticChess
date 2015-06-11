package de.uni_leipzig.informatik.swp15_sc.utils;

import org.json.*;

public class CompilerTest {
	public static void main (String[] args) {
		String query = "select ?x count(?y) as ?c where { { ?x :p ?y. } union { ?x :result '1/2-1/2'. } } limit 1 order by asc(count(?x))";
		System.out.println(query);
		try {
			JSONObject compiled = TemplateUtils.compile(query);
			System.out.println(compiled);
			String test = TemplateUtils.makePretty(compiled);
			System.out.println(test);
		} catch (Exception e) {
			System.err.println(e);
		}
		
		String query2 = "select * where { ?x :p ?y }";
		try {
			JSONObject compiled2 = TemplateUtils.compile(query2);
			String test2 = TemplateUtils.makePretty(compiled2);
			System.out.println(test2);
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
