/*
 *  Copyright 2015 Philip Gasteiger
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.aau.grounder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of the {@link Grounder} interface that calls gringo.
 * 
 * @author Philip Gasteiger
 *
 */
public class GrounderGringoImpl implements Grounder {
	/** name of the command of the grounder */
	public final String GROUNDER_COMMAND_NAME;
	
	/** options passed to the grounder, separated by spaces */
	public final String[] GROUNDER_OPTIONS;
	
	/**
	 * Creates a new instance of the gringo grounder implementation.
	 * 
	 * @param grounderCommand
	 *            The command of the grounder.
	 */
	public GrounderGringoImpl(String grounderCommand, String grounderOptions) {
		this.GROUNDER_COMMAND_NAME = grounderCommand;
		this.GROUNDER_OPTIONS = grounderOptions.split(" ");
	}

	@Override
	public String ground(String logicProgram) throws GroundingException {
		Process grounderProcess;
		List<String> grounderCommand = new ArrayList<String>();
		grounderCommand.add(GROUNDER_COMMAND_NAME);
		
		if (GROUNDER_OPTIONS.length > 1 && !GROUNDER_OPTIONS[0].isEmpty())
			grounderCommand.addAll(Arrays.asList(GROUNDER_OPTIONS));
		
		try {
			ProcessBuilder builder = new ProcessBuilder(grounderCommand);
			grounderProcess = builder.start();
		} catch (IOException e) {
			throw new GroundingException("Starting the grounder failed", e);
		}

		writeLogicProgram(logicProgram, grounderProcess.getOutputStream());
		
		try {
			String groundingResult = read(grounderProcess.getInputStream());
			String errors = read(grounderProcess.getErrorStream());

			if (!errors.isEmpty() && groundingResult.isEmpty()) {
				// no grounded result --> throw exception
				throw new GroundingException(errors);
			} else if (!errors.isEmpty()) {
				// print as warning
				System.err.println(errors);
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
