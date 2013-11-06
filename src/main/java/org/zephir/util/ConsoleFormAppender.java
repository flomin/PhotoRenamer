package org.zephir.util;

import java.util.ArrayList;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class ConsoleFormAppender extends AppenderSkeleton {
	
	private static ConsoleForm consoleForm;
	private static LogPublishingThread logPublisher;

	public ConsoleFormAppender() {
		//System.out.println("ConsoleFormAppender()");
		consoleForm = new ConsoleForm();
		logPublisher = new LogPublishingThread(consoleForm, 500);
	}

	public static Logger add(final Logger log) {
		log.addAppender(new ConsoleFormAppender());
		log.setLevel(Level.DEBUG);
		return log;
	}

	@Override
	protected void append(final LoggingEvent loggingevent) {
		boolean inError = loggingevent.getLevel().equals(Level.ERROR) || loggingevent.getLevel().equals(Level.FATAL);
		logPublisher.addEvent(loggingevent.getMessage().toString(), inError);
	}
	
	@Override
	public void close() {
		ConsoleFormAppender.closeAll();
	}
	
	public static void closeAll() {
		if (consoleForm != null) {
			consoleForm.close();
		}
		if (logPublisher != null) {
			logPublisher.stopThread();
		}
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}
	
	public static void focus() {
		if (consoleForm != null) {
			consoleForm.focus();
		}
	}
}

class LogPublishingThread extends Thread {
	private ConsoleForm consoleForm;
	private ArrayList<Event> events;
	private long pubInterval;
	private boolean goon;
	
	public LogPublishingThread(final ConsoleForm consoleForm, final long pubInterval) {
		this.events = new ArrayList<Event>(1000);
		this.pubInterval = pubInterval;
		this.consoleForm = consoleForm;
		// this.setPriority(Thread.NORM_PRIORITY - 1);
		this.goon = true;
		this.start();
	}
	
	public void setConsoleForm(final ConsoleForm consoleForm) {
		this.consoleForm = consoleForm;
	}

	@Override
	public void run() {
		while (goon) {
			synchronized (events) {
				try {
					events.wait(pubInterval);
				} catch (InterruptedException e) {
					System.err.println("Exception during LogPublishingThread wait: "+e.toString());
				}
				//System.out.println("LogPublishingThread woke up !");
				if (!events.isEmpty()) {
					//System.out.println("LogPublishingThread has events !");
				
					for (Event e:events) {
						//System.out.println("consoleForm.print("+s+")");
						consoleForm.println(e.getMessage(), e.isInError());
					}
					events.clear();
				}
			}
		}
	}

	public void addEvent(final String text, final boolean inError) {
		synchronized (events) {
			events.add(new Event(text, inError));
			//if (triggerPrio != null && prio.isGreaterOrEqual(triggerPrio))
			events.notify();
		}
	}
	
	public final void stopThread() {
		goon = false;
	}
}

class Event {
	private String message;
	private boolean inError;
	public Event(final String message, final boolean inError) {
		this.message = message;
		this.inError = inError;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(final String message) {
		this.message = message;
	}
	public boolean isInError() {
		return inError;
	}
	public void setInError(final boolean inError) {
		this.inError = inError;
	}
}
