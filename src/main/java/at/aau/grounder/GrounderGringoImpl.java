package at.aau.grounder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Implementation of the {@link Grounder} interface that calls gringo.
 * 
 * @author Philip Gasteiger
 *
 */
public class GrounderGringoImpl implements Grounder {
	/** name of the command of the grounder */
	public static final String GROUNDER_COMMAND_NAME = "gringo";

	@Override
	public String ground(String logicProgram) throws GroundingException {
		Process grounderProcess;
		 
		try {
			grounderProcess = Runtime.getRuntime().exec(GROUNDER_COMMAND_NAME);
		} catch (IOException e) {
			throw new GroundingException("Starting the grounder failed", e);
		}

		writeLogicProgram(logicProgram, grounderProcess.getOutputStream());
		
		try {
			String groundingResult = read(grounderProcess.getInputStream());
			String errors = read(grounderProcess.getErrorStream());
			
			// check if there are any errors
			if (!errors.isEmpty()) {
				throw new GroundingException(errors);
			}
			
			return groundingResult;
		} catch (IOException e) {
			throw new GroundingException("Could not read the grounded program");
		}
	}

	private void writeLogicProgram(String logicProgram, OutputStream outputStream)
			throws GroundingException {
		try {
			outputStream.write(logicProgram.getBytes());
			outputStream.close();
		} catch (IOException e) {
			throw new GroundingException("Passing the logic program to the grounder failed", e);
		}
	}

	private String read(InputStream inputStream)
			throws IOException {
		StringBuilder input = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		String line = reader.readLine();

		while (line != null) {
			input.append(line);
			input.append('\n');

			line = reader.readLine();
		}

		return input.toString();
	}
}
