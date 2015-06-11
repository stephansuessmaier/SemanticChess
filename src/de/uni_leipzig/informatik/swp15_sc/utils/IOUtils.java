package de.uni_leipzig.informatik.swp15_sc.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class contains static methods for I/O related tasks.
 * @author Stephan Suessmaier
 *
 */
public class IOUtils {
	/**
	 * Reads a line from the command prompt.
	 * @param prompt message that shows up on the console.
	 * @return what the user typed in.
	 * @throws Exception if an I/O-related error occurs.
	 */
	public static String readln (String prompt) throws Exception {
		System.out.print (prompt + ":> ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		return reader.readLine();
	}
	
	/**
	 * Loads a text file
	 * @param path Path to file
	 * @return Text as String
	 */
	public static String readFile (String path) {
		byte[] data;
		FileInputStream reader;
		try {
			reader = new FileInputStream (path);
			data = new byte[reader.available()];
			reader.read (data);
			reader.close();
			return new String (data);
		}
		catch (IOException e) {
			System.err.println (e);
			return "FAIL";
		}
	}
	
	/**
	 * Stores given data bytes into a file
	 * @param data bytes to save
	 * @param path path to file
	 */
	public static void saveData (byte[] data, String path) {
        FileOutputStream writer;
        try {
            writer = new FileOutputStream (path);
            writer.write(data);
            writer.close();
        }
        catch (IOException e) {
            System.err.println("[ERROR] in eu.stephansuessmaier.utils.IO: saveData(..): " + e);
        }
    }

}
