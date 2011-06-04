package org.zephir.photorenamer.view;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephir.photorenamer.core.PhotoRenamerConstants;
import org.zephir.photorenamer.core.PhotoRenamerCore;
import org.zephir.photorenamer.exception.ArgumentsException;
import org.zephir.util.exception.CustomException;

/**
 * Console to use the JMITCore
 */
public class PhotoRenamerConsole {
	private static Logger log = LoggerFactory.getLogger(PhotoRenamerConsole.class);

	private static final String COMMAND_LINE_PARAMETER_PREFIX = "--";
	private static final String COMMAND_LINE_STRING_SEPARATOR = "'";

	private PhotoRenamerConsole() {}

	/**
	 * Main method for MF Utility.
	 */
	public static void main(String[] args) {
		boolean isDebugMode = true;
		File outputFile = null;
		boolean isInteractive = false;
		PhotoRenamerCore core = new PhotoRenamerCore();

		try {
			int toRepeat = "_______________".length() - PhotoRenamerConstants.VERSION.length() - 2;
			log.info(" _______________ \n"
				   + "| PHOTO RENAMER |\n"
				   + " "+StringUtils.repeat("¯", toRepeat)+"v"+PhotoRenamerConstants.VERSION+"¯ \n");

			if (args.length > 0) {
				// checking command line args for parameters
				Map<String, String> argsMap = collectArgs(args);
				core.setPattern(argsMap.get(PhotoRenamerConstants.COMMAND_LINE_ARG_PATTERN));
				core.setDelta(argsMap.get(PhotoRenamerConstants.COMMAND_LINE_ARG_DELTA));
				core.setFolderToProcess(argsMap.get(PhotoRenamerConstants.COMMAND_LINE_ARG_FOLDER));
				core.setSuffix(argsMap.get(PhotoRenamerConstants.COMMAND_LINE_ARG_SUFFIX));
				core.setRenameVideo(argsMap.containsKey(PhotoRenamerConstants.COMMAND_LINE_ARG_VIDEO));
				core.setRotateImages(argsMap.containsKey(PhotoRenamerConstants.COMMAND_LINE_ARG_ROTATE));

				if (argsMap.containsKey(PhotoRenamerConstants.COMMAND_LINE_ARG_HELP)) {
					log.info(PhotoRenamerConstants.COMMAND_LINE_USAGE);
					System.exit(0);
				}
			}

			//launch process
			core.processFolder();

		} catch (CustomException ex) {
			logCriticalError("Error during batch process", ex, isDebugMode, outputFile);
		} catch (Exception ex) {
			logCriticalError("Unhandled Exception occured", ex, isDebugMode, outputFile);
		}

		if (isInteractive) {
			log.info("Type any key then Enter to exit the application");
			try {
				System.in.read();
			} catch (Exception e) {
				log.error("Error during final read(): " + e, e);
			}
		} else {
			log.info("PhotoRenamer exits now!");
			System.exit(0);
		}
	}

	/**
	 * @param args
	 * @return a Map of the key-value or key only arguments
	 * @throws ArgumentsException
	 */
	private static Map<String, String> collectArgs(String[] args) throws ArgumentsException {
		try {
			if (args == null || args.length == 0) {
				log.debug("collectArgs(args="+StringUtils.join(args, ", ")+") args is empty");
				return null;
			}
			Map<String, String> argsMap = new HashMap<String, String>();

			for (int i=0; i<args.length; i++) {
				String arg = args[i];
				if (arg.startsWith(COMMAND_LINE_PARAMETER_PREFIX)) {
					if (i+1 < args.length) {
						String nextArg = args[i+1];
						if (nextArg.startsWith(COMMAND_LINE_PARAMETER_PREFIX)) {
							// the next arg is beginning by '--': arg must be a key only argument
							argsMap.put(arg.substring(COMMAND_LINE_PARAMETER_PREFIX.length()), "");
						} else {
							// the next arg isn't beginning by '-': arg must be a key-value argument
							if (nextArg.startsWith(COMMAND_LINE_STRING_SEPARATOR)) {
								// begining of a String parameter ==> wait for the same at the end of another parameter
								nextArg = nextArg.substring(COMMAND_LINE_STRING_SEPARATOR.length());
								boolean endOfStringParamFound = false;
								for (int j=i+2; j<args.length; j++) {
									String argInStringParameter = args[j];
									if (argInStringParameter.endsWith(COMMAND_LINE_STRING_SEPARATOR)) {
										// String parameter finished
										nextArg += " " + argInStringParameter.substring(0, argInStringParameter.length() - COMMAND_LINE_STRING_SEPARATOR.length());
										i = j - 1;
										endOfStringParamFound = true;
										break;
									} else {
										// still in String parameter
										nextArg += " " + argInStringParameter;
									}
								}
								if (!endOfStringParamFound) {
									log.debug("collectArgs(args='"+StringUtils.join(args, ", ")+"') Couldn't find the String parameter end for parameter named '"+arg+"'");
									throw new ArgumentsException(arg);
								}
							}
							argsMap.put(arg.substring(COMMAND_LINE_PARAMETER_PREFIX.length()), nextArg);
							i++;
						}
					} else {
						argsMap.put(arg.substring(COMMAND_LINE_PARAMETER_PREFIX.length()), "");
					}
				} else {
					log.debug("collectArgs(args='"+StringUtils.join(args, ", ")+"') parameter '"+arg+"' isn't valid");
					throw new ArgumentsException(arg);
				}
			}
			return argsMap;
		} catch (Exception ex) {
			log.error("collectArgs(args="+StringUtils.join(args, ", ")+") KO: " + ex);
			throw new ArgumentsException(ex);
		}
	}

	/**
	 * Method to output a critical Error: it will output the error using log and write it in the outputFile
	 * @param message
	 * @param t
	 * @param isDebugMode if true, the StackTrace will be included.
	 * @param outputFile
	 */
	private static void logCriticalError(String message, Throwable t, boolean isDebugMode, File outputFile) {
		String lMessage = StringUtils.isNotBlank(message) ? message + ": ": "";
		if (isDebugMode) {
			log.error(lMessage, t);
		} else {
			log.error(lMessage + t.getMessage());
		}
		if (outputFile != null) {
			FileWriter outWriter = null;
			try {
				outWriter = new FileWriter(outputFile);
				if (isDebugMode) {
					outWriter.write(lMessage + "\n");
					t.printStackTrace(new PrintWriter(outWriter));
				} else {
					outWriter.write(lMessage + t.getMessage());
				}
				log.info("Output file created with error log: " + outputFile.getAbsolutePath());
			} catch (IOException e) {
				log.error("IOException while writing the output file with the thrown Exception: " + e, e);
			} finally {
				if (outWriter != null) {
					try {
						outWriter.close();
					} catch (IOException e) {
						log.error("IOException while closing the output file: " + e, e);
					}
				}
			}
		}
	}
}
