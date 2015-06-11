package de.uni_leipzig.informatik.swp15_sc.utils;


/**
 * This class contains functions for URIs
 * @author Stephan Suessmaier
 *
 */
public class URIUtils {
	/**
	 * This function cuts off http://../.../property until the property name begins
	 * @param uris URIs to shorten
	 * @return shortened URIs
	 */
	public static String[] cutOff (String[] uris) {
		String[] result = new String[uris.length];
		
		for (int i=0; i<uris.length; i++) {
			String[] arr = uris[i].split("/");
			String value = arr[arr.length-1];
			if (value.contains("#")) {
				String[] arr2 = value.split("#");
				result[i] = arr2[arr2.length-1];
			}
			else {
				result[i] = value;
			}
		}
		
		return result;
	}
}
