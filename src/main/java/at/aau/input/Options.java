package at.aau.input;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

/**
 * Holds the options of the gringo-wrapper.
 * 
 * @author Philip Gasteiger
 *
 */
@Parameters(separators = "=")
public class Options {
	private static final String PROGRAM_NAME = "gringo-wrapper";
	
	private JCommander cli;
	
	@Parameter(description = "[files]")
	private List<String> inputFiles = new ArrayList<String>();

	@Parameter(names = { "-h", "--help" }, help = true, description = "Print help information and exit")
	private boolean printHelp = false;

	@Parameter(names = { "-n", "--no-debug" }, description = "Do not add the '_debug' literal")
	private boolean noDebug = false;

	@Parameter(names = { "-d", "--debug-constant" }, description = "The debug constant to be added to the rules")
	private String debugLiteral = "_debug";

	@Parameter(names = { "-g", "--grounder" }, description = "The command of the grounder")
	private String grounderCommand = "gringo";
	
	/**
	 * Parse the given command line arguments.
	 * 
	 * @param args
	 *            The arguments to parse.
	 * @throws InvalidOptionException
	 *             Thrown, if an invalid option is specified.
	 */
	public Options(String[] args) throws InvalidOptionException {
		try{
			cli = new JCommander(this, args);
			cli.setProgramName(PROGRAM_NAME);
		} catch (ParameterException e) {
			// unknown option
			StringBuilder errorMessage = new StringBuilder(e.getMessage() + "\n");
			
			cli = new JCommander(this);
			cli.setProgramName(PROGRAM_NAME);
			cli.usage(errorMessage);
			
			throw new InvalidOptionException(errorMessage.toString());
		}
	}
	
	public void printHelp() {
		cli.usage();
	}

	public List<String> getInputFiles() {
		return inputFiles;
	}

	public boolean isPrintHelp() {
		return printHelp;
	}

	public boolean isDebug() {
		return !noDebug;
	}

	public String getDebugLiteral() {
		return debugLiteral;
	}

	public String getGrounderCommand() {
		return grounderCommand;
	}
}