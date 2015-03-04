package at.aau;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import at.aau.grounder.GroundingException;
import at.aau.postprocessing.PostprocessingException;

/**
 * Main class of the gringo-wrapper.
 * 
 * @author Philip Gasteiger
 *
 */
public class App {
	public static void main(String[] args) {
		InputStream input;

		try {
			// switch according to the command line parameters
			if (args.length == 0) {
				input = System.in;
			} else if (args.length == 1) {
				input = new FileInputStream(args[0]);
			} else {
				System.err.println("Invalid parameters.\nUsage: gringo-wrapper [filename]");
				return;
			}			
			
			// instantiate a gringo wrapper and print the grounded program
			GringoWrapper wrapper = new GringoWrapper();
			System.out.print(wrapper.ground(readInput(input)));
		} catch (FileNotFoundException e) {
			System.err.println("The file " + args[0] + " was not found.");
		} catch (IOException e) {
			System.err.println("Could not read the logic program.");
		} catch (GroundingException e) {
			System.err.println("The program could not be grounded. Details:\n" + e.getMessage());
		} catch (PostprocessingException e) {
			System.err.println("Postprocessing the grounded program failed.");
		}
	}
	
	/**
	 * Read the input from the given input stream.
	 * 
	 * @param input
	 *            The input stream to read from.
	 * @return The read logic program.
	 * @throws IOException
	 *             If there was an IO error while reading the logic program.
	 */
	private static String readInput (InputStream input) throws IOException {
		StringBuilder logicProgram = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));

		String line = reader.readLine();

		while (line != null) {
			logicProgram.append(line);
			logicProgram.append('\n');

			line = reader.readLine();
		}

		return logicProgram.toString();
	}
}
