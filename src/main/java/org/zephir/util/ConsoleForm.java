package org.zephir.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.zephir.photorenamer.view.SWTLoader;

public class ConsoleForm {
	// ===========================================================
	// Constants
	// ===========================================================

	private static boolean showOnFirstLog = true;
	private static boolean shownOnFirstLog = false;

	@SuppressWarnings("serial")
	public final Map<Level, ConsoleLineStyle> levelToStyleMap = new HashMap<Level, ConsoleLineStyle>() {
		{
			put(Level.WARN, new ConsoleLineStyle(SWT.BOLD, SWT.COLOR_YELLOW));
			put(Level.ERROR, new ConsoleLineStyle(SWT.BOLD, SWT.COLOR_RED));
			put(Level.FATAL, new ConsoleLineStyle(SWT.BOLD, SWT.COLOR_RED));
		}
	};

	// ===========================================================
	// Fields
	// ===========================================================

	private Shell sShell; // @jve:decl-index=0:visual-constraint="29,10"
	private StyledText textArea = null;

	// ===========================================================
	// Constructors
	// ===========================================================

	public ConsoleForm() {
		super();
		Display dis = ((Display) SWTLoader.getDisplay());
		if (dis != null) {
			dis.syncExec(new Runnable() {
				@Override
				public void run() {
					createSShell();
				}
			});
		}
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		final Display display = (Display) SWTLoader.getDisplay();
		sShell = new Shell(display);
		sShell.setText("Console");
		sShell.setLayout(new FillLayout(SWT.HORIZONTAL));
		sShell.setSize(new Point(1000, 800));
		// to hide the window instead of closing it
		sShell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				sShell.setVisible(false);
				event.doit = false;
			}
		});

		textArea = new StyledText(sShell, SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY); // line wrapping is too slow on large text | SWT.WRAP 
		// Choose a monospaced font
		textArea.setFont(SWTFontUtils.getMonospacedFont(sShell.getDisplay()));
		textArea.setBackground(sShell.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		textArea.setEditable(false);
	}

	public void println(final List<ConsoleLine> lines) {
		final ConsoleForm cf = this;

		if (sShell != null) {
			if (sShell.getDisplay() != null) {
				sShell.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						for (ConsoleLine line : lines) {
							String lineStr = line.getLine() + textArea.getLineDelimiter();

							ConsoleLineStyle lineStyle = levelToStyleMap.get(line.getLevel());
							if (lineStyle != null) {
								StyleRange styleRange = new StyleRange();
								styleRange.fontStyle = lineStyle.getFontStyle();
								styleRange.foreground = sShell.getDisplay().getSystemColor(lineStyle.getFontForeground());
								styleRange.start = textArea.getText().length();
								styleRange.length = lineStr.length();
								textArea.append(lineStr);
								textArea.setStyleRange(styleRange);
							} else {
								textArea.append(lineStr);
								textArea.invokeAction(ST.TEXT_END);
							}
							cf.sShell.setVisible(true);
						}

						// compute new size if needed
						final Point newSize = sShell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
						if (sShell.getSize().x < newSize.x) {
							sShell.setSize(newSize.x, sShell.getSize().y);
						}

						// resize window if needed
						sShell.layout(true, true);
					}
				});
			}
		}

		if (showOnFirstLog && !shownOnFirstLog) {
			shownOnFirstLog = true;
			focus();
		}
	}

	public void focus() {
		final ConsoleForm cf = this;
		if (sShell != null) {
			if (sShell.getDisplay() != null) {
				sShell.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						cf.sShell.forceActive();
						cf.sShell.forceFocus();
					}
				});
			}
		}
	}

	public String saveReport() throws IOException {
		String reportFileName = String.format("%1$tY_%1$te%1$tm_%1$tH%1$tM%1$tS_report.log", Calendar.getInstance());
		String parentFolder = new File("temp").getAbsoluteFile().getParentFile().getParent();
		File report = new File(parentFolder, reportFileName);
		List<String> lines = Arrays.asList(textArea.getText().split(textArea.getLineDelimiter()));

		FileUtils.writeLines(report, lines);
		return report.getAbsolutePath();
	}

	public void open() {
		sShell.open();
	}

	public void close() {
		sShell.close();
	}

	public boolean isVisible() {
		return sShell.isVisible();
	}

	public void setVisible(final boolean visible) {
		sShell.setVisible(visible);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class ConsoleLine {
		private String line;
		private Level level;

		public ConsoleLine(String line, Level level) {
			super();
			this.line = line;
			this.level = level;
		}

		public String getLine() {
			return line;
		}

		public void setLine(String line) {
			this.line = line;
		}

		public Level getLevel() {
			return level;
		}

		public void setLevel(Level level) {
			this.level = level;
		}
	}

	public static class ConsoleLineStyle {
		private int fontStyle;
		private int fontForeground;

		public ConsoleLineStyle(int fontStyle, int fontForeground) {
			super();
			this.fontStyle = fontStyle;
			this.fontForeground = fontForeground;
		}

		public int getFontStyle() {
			return fontStyle;
		}

		public void setFontStyle(int fontStyle) {
			this.fontStyle = fontStyle;
		}

		public int getFontForeground() {
			return fontForeground;
		}

		public void setFontForeground(int fontForeground) {
			this.fontForeground = fontForeground;
		}
	}
}
