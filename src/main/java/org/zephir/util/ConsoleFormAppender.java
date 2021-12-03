package org.zephir.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.zephir.util.ConsoleForm.ConsoleLine;

public class ConsoleFormAppender extends AppenderSkeleton {
	// ===========================================================
	// Constants
	// ===========================================================

	private static ConsoleForm consoleForm;
	private static LogPublishingThread logPublisher;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	public ConsoleFormAppender() {
		// System.out.println("ConsoleFormAppender()");
		consoleForm = new ConsoleForm();
		logPublisher = new LogPublishingThread(consoleForm, 500);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void append(final LoggingEvent loggingevent) {
		Event newEvent = new Event(loggingevent.getMessage().toString(), loggingevent.getLevel());
		logPublisher.addEvent(newEvent);
	}

	@Override
	public void close() {
		ConsoleFormAppender.closeAll();
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public static Logger add(final Logger log) {
		log.addAppender(new ConsoleFormAppender());
		log.setLevel(Level.DEBUG);
		return log;
	}

	public static void closeAll() {
		if (consoleForm != null) {
			consoleForm.close();
		}
		if (logPublisher != null) {
			logPublisher.stopThread();
		}
	}

	public static void focus() {
		if (consoleForm != null) {
			consoleForm.focus();
		}
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class LogPublishingThread extends Thread {
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

		@Override
		public void run() {
			while (goon) {
				synchronized (events) {
					try {
						events.wait(pubInterval);
					} catch (InterruptedException e) {
						System.err.println("Exception during LogPublishingThread wait: " + e.toString());
					}
					// System.out.println("LogPublishingThread woke up !");
					if (!events.isEmpty()) {
						// System.out.println("LogPublishingThread has events !");

						List<ConsoleLine> consoleLineList = new ArrayList<>();
						for (Event e : events) {
							consoleLineList.add(new ConsoleLine(e.getMessage(), e.getLevel()));
							// System.out.println("consoleForm.print("+s+")");
						}
						consoleForm.println(consoleLineList);
						System.out.println(ConsoleFormAppender.class.getSimpleName() + events.size() + " events managed");
						events.clear();
					}
				}
			}
		}

		public void addEvent(final Event event) {
			synchronized (events) {
				events.add(event);
			}
		}

		public final void stopThread() {
			goon = false;
		}
	}

	@SuppressWarnings("unused")
	private class Event {
		private String message;
		private Level level;
		private boolean inError;

		public Event(final String message, final Level level) {
			this.message = message;
			this.level = level;
			this.inError = level.equals(Level.ERROR) || level.equals(Level.FATAL);
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

		public Level getLevel() {
			return level;
		}

		public void setLevel(Level level) {
			this.level = level;
		}
	}
}
