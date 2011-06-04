package org.zephir.util.exception;

@SuppressWarnings("serial")
public class CustomException extends Exception {
	public CustomException() {
		super();
	}
	public CustomException(String message) {
		super(message);
	}
	public CustomException(Throwable cause) {
		super(cause);
	}
	public CustomException(String message, Throwable cause) {
		super(message, cause);
	}
}
