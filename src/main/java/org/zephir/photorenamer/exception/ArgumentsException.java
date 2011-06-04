package org.zephir.photorenamer.exception;

import org.zephir.photorenamer.core.PhotoRenamerConstants;

@SuppressWarnings("serial")
public class ArgumentsException extends Exception {
	private String argument;
	public ArgumentsException() {
		super();
	}
	public ArgumentsException(String argument) {
		super();
		this.argument = argument;
	}
	public ArgumentsException(Throwable cause) {
		super(cause);
	}
	public String getArgument() {
		return argument;
	}
	@Override
	public String toString() {
		return "Command-line arguments invalid: '" + getArgument() + "'\n." + PhotoRenamerConstants.COMMAND_LINE_USAGE;
	}
}