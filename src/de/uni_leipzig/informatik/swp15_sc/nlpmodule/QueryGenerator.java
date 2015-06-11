package de.uni_leipzig.informatik.swp15_sc.nlpmodule;

import org.json.*;
import org.apache.commons.lang3.StringUtils;

import de.uni_leipzig.informatik.swp15_sc.utils.URIUtils;

import java.util.LinkedList;

/**
 * This class is used for generating a SPARQL-query out of query templates.
 * 
 * @author Stephan Suessmaier
 *
 */
public class QueryGenerator {
	/**
	 * Creates a new QueryGenerator
	 * 
	 * @param properties
	 *            available property URIs.
	 */
	public QueryGenerator(String[] properties, String[] classes) {
		propertyURIs = properties;
		classURIs = classes;

		// cut off http://../.../property until the property name begins
		shortProperties = URIUtils.cutOff(propertyURIs);
		shortClasses = URIUtils.cutOff(classURIs);
	}

	/**
	 * Generates a SPARQL query out of query templates.
	 * 
	 * @param queryTemplates
	 *            query templates
	 * @return SPARQL query
	 */
	public String generateQuery(JSONObject queryTemplate) {
		// fill query template, pick the best result and return query

		// foreach relation in template
		// foreach triples in relation
		// foreach variable in relation:
		// if slot: fill in uri most similar to word

		String querySelectClause = "select * ";
		String queryWhereClause = "where { ";
		String queryOrderByClause = "";

		String triples = "";

		for (int i = 0; i < queryTemplate.getJSONArray("where").length(); i++) {

			for (int j = 0; j < queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").length(); j++) {

				String subjectString = "";
				String propertyString = "";
				String objectString = "";

				switch (queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("subject").getString("vartype")) {
				case "var":
					subjectString += "?" + queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("subject").getString("word");
					break;
				case "const":
					if (queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("subject").getString("word").contains("http://")) {
						subjectString += "<" + queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("subject").getString("word") + ">";
					} else {
						subjectString += queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("subject").getString("word");
					}
					break;
				default:
					break;
				}

				switch (queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("property").getString("vartype")) {
				case "var":
					propertyString += "?" + queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("property").getString("word");
					break;
				case "const":
					if (queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("property").getString("word").contains("http://")) {
						propertyString += "<" + queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("property").getString("word") + ">";
					} else {
						propertyString += queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("property").getString("word");
					}
					break;
				case "slot":
					String propertyWord = queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("property").getString("word");
					queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("property").put("word", getPropertySimilarTo(propertyWord));
					queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("property").put("vartype", "const");
					propertyString += "<" + queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("property").getString("word") + ">";
					break;
				default:
					break;
				}

				switch (queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("object").getString("vartype")) {
				case "var":
					objectString += "?" + queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("object").getString("word");
					break;
				case "const":
					if (queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("object").getString("word").contains("http://")) {
						objectString += "<" + queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("object").getString("word") + ">";
					} else {
						objectString += "\"" + queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("object").getString("word") + "\"";
					}
					break;
				case "slot":
					String objectWord = queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("object").getString("word");
					queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("object").put("word", getClassSimilarTo(objectWord));
					queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("object").put("vartype", "const");
					objectString += "<" + queryTemplate.getJSONArray("where").getJSONObject(i).getJSONArray("triples").getJSONObject(j).getJSONObject("object").getString("word") + ">";
					break;
				default:
					break;
				}

				triples += " " + subjectString + " " + propertyString + " " + objectString + ".";

			}
			queryWhereClause += "{ " + triples + "}";

			if (i < queryTemplate.getJSONArray("where").length() - 1) {
				queryWhereClause += " UNION ";
			}
			triples = "";
		}
		queryWhereClause += " }";

		if (queryTemplate.getBoolean("setOptions")) {

			String orderby = "";
			String limit = "";

			if (queryTemplate.getJSONObject("options").has("orderby")) {
				orderby += "order by " + queryTemplate.getJSONObject("options").getString("orderby");
			}

			if (queryTemplate.getJSONObject("options").has("limit")) {
				limit += " limit " + queryTemplate.getJSONObject("options").getInt("limit");
			}

			queryOrderByClause += orderby + limit;
		}


		System.out.println(querySelectClause + queryWhereClause + queryOrderByClause);

		return (querySelectClause + queryWhereClause + queryOrderByClause);
	}

	/**
	 * finds the most suitable property URI for a given word
	 * 
	 * @param word
	 *            word
	 * @return property URI
	 */
	private String getPropertySimilarTo(String word) {
		int bestIndex = 0;
		int smallestDistance = Integer.MAX_VALUE;

		for (int i = 0; i < shortProperties.length; i++) {
			int distance = StringUtils.getLevenshteinDistance(word, shortProperties[i]);
			if (distance < smallestDistance) {
				smallestDistance = distance;
				bestIndex = i;
			}
		}

		return propertyURIs[bestIndex];
	}

	/**
	 * finds the most suitable class URI for a given word
	 * 
	 * @param word
	 *            word
	 * @return class URI
	 */
	private String getClassSimilarTo(String word) {
		int bestIndex = 0;
		int smallestDistance = Integer.MAX_VALUE;

		for (int i = 0; i < shortClasses.length; i++) {
			int distance = StringUtils.getLevenshteinDistance(word, shortClasses[i]);
			if (distance < smallestDistance) {
				smallestDistance = distance;
				bestIndex = i;
			}
		}

		return classURIs[bestIndex];
	}

	private String[] propertyURIs;
	private String[] shortProperties;
	private String[] classURIs;
	private String[] shortClasses;
}
