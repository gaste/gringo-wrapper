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
		return readGroundedProgram(grounderProcess.getInputStream());
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

	private String readGroundedProgram(InputStream inputStream)
			throws GroundingException {
		StringBuilder groundedProgram = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		try {
			String line = reader.readLine();

			while (line != null) {
				groundedProgram.append(line);
				groundedProgram.append('\n');

				line = reader.readLine();
			}
		} catch (IOException e) {
			throw new GroundingException("Reading the grounded program from the grounder failed", e);
		}

		return groundedProgram.toString();
	}
}
