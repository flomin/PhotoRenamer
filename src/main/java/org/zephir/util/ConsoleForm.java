package org.zephir.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
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

	private Shell sShell;  //  @jve:decl-index=0:visual-constraint="29,10"
	private StyledText textArea = null;
	
	public ConsoleForm() {
		super();
		((Display) SWTLoader.getDisplay()).syncExec(new Runnable() {
			public void run() {
				createSShell();
			}
		});
	}
	
	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell(((Display) SWTLoader.getDisplay()));
		sShell.setText("Console");
		sShell.setLayout(new FillLayout(SWT.HORIZONTAL));
		sShell.setSize(new Point(810, 400));
		//to hide the window instead of closing it
		sShell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				sShell.setVisible(false);
				event.doit = false;
			}
		});
		textArea = new StyledText(sShell, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		textArea.setBackground(sShell.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		textArea.setEditable(false);
	}
	
	public void println(final String str, final boolean inError) {	
		final ConsoleForm cf = this;
		sShell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				String line = str + textArea.getLineDelimiter();
				
//				if (line.toLowerCase().indexOf("exception") != -1
//						|| line.toLowerCase().indexOf("error") != -1
//						|| line.toLowerCase().indexOf("problem") != -1) {
				if (inError) {
					StyleRange styleRange = new StyleRange();
					styleRange.fontStyle = SWT.BOLD;
					styleRange.foreground = sShell.getDisplay().getSystemColor(SWT.COLOR_RED);
					styleRange.start = textArea.getText().length();
					styleRange.length = line.length();
					textArea.append(line);
					textArea.setStyleRange(styleRange);
				} else {
					textArea.append(line);
					textArea.invokeAction(ST.TEXT_END);
				}
				cf.sShell.setVisible(true);
				cf.sShell.forceActive();
				cf.sShell.forceFocus();
			}
			
		});
	}

	public String saveReport() throws IOException {
		String reportFileName = String.format("%1$tY_%1$te%1$tm_%1$tH%1$tM%1$tS_report.log", Calendar.getInstance());
		String parentFolder = (new File("temp")).getAbsoluteFile().getParentFile().getParent();
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
	
	public void setVisible(boolean visible) {
		sShell.setVisible(visible);
	}
}
