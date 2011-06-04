package org.zephir.photorenamer.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.zephir.photorenamer.core.PhotoRenamerConstants;
import org.zephir.photorenamer.core.PhotoRenamerCore;
import org.zephir.util.ConsoleFormAppender;

public class PhotoRenamerForm {	
	private static Logger log = Logger.getLogger(PhotoRenamerForm.class);  //  @jve:decl-index=0:
	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Label labelInputFolder = null;
	private Text textInputFolder = null;
	private Button buttonInputFolder = null;
	private Label labelSuffix = null;
	private Text textSuffix = null;
	private Label labelDelta = null;
	private Text textDelta = null;
	private Label labelPattern = null;
	private Text textPattern = null;
	private Button buttonProceed = null;
	private Button checkboxRenameVideo = null;
	private Button checkboxRotateImage = null;
	private Button buttonHelp = null;
	
	public static void main(String[] args) {
		
		Display display = Display.getDefault();
		PhotoRenamerForm thisClass = new PhotoRenamerForm();
		thisClass.createSShell();
		thisClass.sShell.open();
		
		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	
	private void loadPreferences() {
		try {
			if ((new File("PhotoRenamer.properties")).exists()) {
				Properties props = new Properties();
				FileInputStream in = new FileInputStream("PhotoRenamer.properties");
				props.load(in);
				in.close();
				
				String inputFolder = props.getProperty("inputFolder");
				if (inputFolder != null) {
					textInputFolder.setText(inputFolder);
				}
				String delta = props.getProperty("delta");
				if (delta != null) {
					textDelta.setText(delta);
				}
				String suffix = props.getProperty("suffix");
				if (suffix != null) {
					textSuffix.setText(suffix);
				}
				String pattern = props.getProperty("pattern");
				if (pattern != null) {
					textPattern.setText(pattern);
				}
				String renameVideo = props.getProperty("renameVideo");
				if (renameVideo != null) {
					checkboxRenameVideo.setSelection(renameVideo.equalsIgnoreCase("true"));
				}
				String rotateImage = props.getProperty("rotateImage");
				if (rotateImage != null) {
					checkboxRotateImage.setSelection(renameVideo.equalsIgnoreCase("true"));
				}
			}
		} catch (IOException e) {
			log.error("loadPreferences() KO: "+e, e);
		}
	}
	
	private void savePreferences() {
		try {
			Properties props = new Properties();
			props.setProperty("inputFolder", textInputFolder.getText());
			props.setProperty("delta", textDelta.getText());
			props.setProperty("suffix", textSuffix.getText());
			props.setProperty("pattern", textPattern.getText());
			props.setProperty("renameVideo", checkboxRenameVideo.getSelection() ? "true" : "false");
			props.setProperty("rotateImage", checkboxRotateImage.getSelection() ? "true" : "false");
			FileOutputStream out = new FileOutputStream("PhotoRenamer.properties");
			props.store(out, "---No Comment---");
			out.close();
			
		} catch (IOException e) {
			log.error("savePreferences() KO: "+e, e);
		}
	}

	
	private void createSShell() {
		sShell = new Shell(SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX);
		sShell.setText("PhotoRenamer by wInd v" + PhotoRenamerConstants.VERSION);
		sShell.setSize(new Point(530, 160));
		sShell.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/skull2-16x16.gif")));
		sShell.setLayout(null);
		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				savePreferences();
				ConsoleFormAppender.closeAll();
			}
		});
		
		labelInputFolder = new Label(sShell, SWT.HORIZONTAL);
		labelInputFolder.setText("Input folder:   ");
		labelInputFolder.setBounds(new Rectangle(3, 3, 100, 19));
		textInputFolder = new Text(sShell, SWT.BORDER);
		textInputFolder.setText(PhotoRenamerConstants.USER_DIR);
		textInputFolder.setBounds(new Rectangle(105, 2, 380, 19));
		textInputFolder.setTextLimit(655);
		buttonInputFolder = new Button(sShell, SWT.NONE);
		buttonInputFolder.setText("...");
		buttonInputFolder.setBounds(new Rectangle(490, 2, 17, 19));
		buttonInputFolder.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				DirectoryDialog directoryDialog = new DirectoryDialog(sShell);
				directoryDialog.setFilterPath(textInputFolder.getText());
				String dir = directoryDialog.open();
				if (dir != null) {
					textInputFolder.setText(dir);
				}
			}
		});
		
		labelSuffix = new Label(sShell, SWT.NONE);
		labelSuffix.setText("Photo suffix:  ");
		labelSuffix.setBounds(new Rectangle(3, 23, 100, 19));
		textSuffix = new Text(sShell, SWT.BORDER);
		textSuffix.setText("");
		textSuffix.setBounds(new Rectangle(105, 22, 200, 19));
		
		labelDelta = new Label(sShell, SWT.NONE);
		labelDelta.setText("Delta:  ");
		labelDelta.setBounds(new Rectangle(3, 43, 100, 19));
		textDelta = new Text(sShell, SWT.BORDER);
		textDelta.setText("0s");
		textDelta.setBounds(new Rectangle(105, 42, 200, 19));
		
		labelPattern = new Label(sShell, SWT.NONE);
		labelPattern.setText("Date pattern:  ");
		labelPattern.setBounds(new Rectangle(3, 63, 100, 19));
		textPattern = new Text(sShell, SWT.BORDER);
		textPattern.setText(PhotoRenamerConstants.DEFAULT_PATTERN);
		textPattern.setBounds(new Rectangle(105, 62, 200, 19));
		
		checkboxRenameVideo = new Button(sShell, SWT.CHECK);
		checkboxRenameVideo.setText("Rename videos");
		checkboxRenameVideo.setSelection(true);
		checkboxRenameVideo.setBounds(new Rectangle(3, 83, 200, 19));
		
		checkboxRotateImage = new Button(sShell, SWT.CHECK);
		checkboxRotateImage.setText("Rotate images");
		checkboxRotateImage.setSelection(true);
		checkboxRotateImage.setBounds(new Rectangle(3, 103, 200, 19));
		
		buttonProceed = new Button(sShell, SWT.NONE);
		buttonProceed.setText("Proceed !");
		buttonProceed.setBounds(new Rectangle(386, 51, 119, 29));
		buttonProceed.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				proceed();
			}
		});
		
		buttonHelp = new Button(sShell, SWT.NONE);
		buttonHelp.setText(" ? ");
		buttonHelp.setBounds(new Rectangle(355, 51, 29, 29));
		buttonHelp.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
					public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
						openHelpDialog("Help - PhotoRenamer", PhotoRenamerConstants.FORM_HELP);
					}
				});
		
		loadPreferences();
	}
	
	private void openHelpDialog(String title, String text) {
		MessageBox mb = new MessageBox(sShell, SWT.OK | SWT.ICON_QUESTION);
		mb.setText(title);
		mb.setMessage(text);
		mb.open();
	}

	private void proceed() {
		try {
			buttonProceed.setEnabled(false);

			final PhotoRenamerCore core = new PhotoRenamerCore();
			core.setFolderToProcess(textInputFolder.getText());
			core.setSuffix(textSuffix.getText());
			core.setPattern(textPattern.getText());
			core.setDelta(textDelta.getText());
			core.setRenameVideo(checkboxRenameVideo.getSelection());
			core.setRotateImages(checkboxRotateImage.getSelection());
			
			Runnable downloadRunnable = new Runnable() {
				public void run() {
					try {
						core.processFolder();
						
						// processing finished
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								buttonProceed.setEnabled(true);
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
						log.debug("Exception: " + e.toString());
					}
				}
			};
			new Thread(downloadRunnable).start();
		} catch (Exception e) {
			log.error("Error: " + e, e);
			buttonProceed.setEnabled(true);
			return;
		}
	}
}
