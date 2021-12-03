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

public class PhotoRenamerForm implements PhotoRenamerConstants {
	private static Logger log = null;
	private Shell sShell = null; // @jve:decl-index=0:visual-constraint="10,10"
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
	private Button checkboxRenameExtra = null;
	private Button checkboxRotateImage = null;
	private Button checkboxRetroDateExif = null;
	private Button buttonHelp = null;

	public static void main(final String[] args) {
		final Display display = (Display) SWTLoader.getDisplay();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				log = Logger.getLogger(PhotoRenamerForm.class);

				PhotoRenamerForm thisClass = new PhotoRenamerForm();
				thisClass.createSShell();
				thisClass.sShell.open();

				while (!thisClass.sShell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
				display.dispose();
			}
		});
	}

	private void loadPreferences() {
		try {
			if (new File("PhotoRenamer.properties").exists()) {
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
					checkboxRenameExtra.setSelection(renameVideo.equalsIgnoreCase("true"));
				}
				String rotateImage = props.getProperty("rotateImage");
				if (rotateImage != null) {
					checkboxRotateImage.setSelection(rotateImage.equalsIgnoreCase("true"));
				}
				String retroDateExif = props.getProperty("retroDateExif");
				if (retroDateExif != null) {
					checkboxRetroDateExif.setSelection(retroDateExif.equalsIgnoreCase("true"));
				}
			}
		} catch (IOException e) {
			log.error("loadPreferences() KO: " + e, e);
		}
	}

	private void savePreferences() {
		try {
			Properties props = new Properties();
			props.setProperty("inputFolder", textInputFolder.getText());
			props.setProperty("delta", textDelta.getText());
			props.setProperty("suffix", textSuffix.getText());
			props.setProperty("pattern", textPattern.getText());
			props.setProperty("renameVideo", checkboxRenameExtra.getSelection() ? "true" : "false");
			props.setProperty("rotateImage", checkboxRotateImage.getSelection() ? "true" : "false");
			props.setProperty("retroDateExif", checkboxRetroDateExif.getSelection() ? "true" : "false");
			FileOutputStream out = new FileOutputStream("PhotoRenamer.properties");
			props.store(out, "---No Comment---");
			out.close();

		} catch (IOException e) {
			log.error("savePreferences() KO: " + e, e);
		}
	}

	private void createSShell() {
		sShell = new Shell((Display) SWTLoader.getDisplay(), SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.MAX);
		sShell.setText("PhotoRenamer by wInd v" + VERSION);
		sShell.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/skull2-16x16.gif")));
		sShell.setLayout(null);
		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			@Override
			public void shellClosed(final org.eclipse.swt.events.ShellEvent e) {
				savePreferences();
				ConsoleFormAppender.closeAll();
			}
		});

		int y = FORM_LINE_SPACE;
		labelInputFolder = new Label(sShell, SWT.HORIZONTAL);
		labelInputFolder.setText("Input folder :");
		labelInputFolder.setBounds(new Rectangle(3, y + FORM_LABEL_DELTA, FORM_LINE_TAB, FORM_LINE_HEIGHT));
		textInputFolder = new Text(sShell, SWT.BORDER);
		textInputFolder.setText(USER_DIR);
		textInputFolder.setBounds(new Rectangle(FORM_LINE_TAB + FORM_LINE_SPACE, y, 450, FORM_LINE_HEIGHT));
		textInputFolder.setTextLimit(655);
		buttonInputFolder = new Button(sShell, SWT.NONE);
		buttonInputFolder.setText("...");
		buttonInputFolder.setBounds(new Rectangle(560, y, 29, FORM_LINE_HEIGHT));
		buttonInputFolder.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				DirectoryDialog directoryDialog = new DirectoryDialog(sShell);
				directoryDialog.setFilterPath(textInputFolder.getText());
				String dir = directoryDialog.open();
				if (dir != null) {
					textInputFolder.setText(dir);
				}
			}
		});

		y += FORM_LINE_HEIGHT + FORM_LINE_SPACE;
		labelSuffix = new Label(sShell, SWT.NONE);
		labelSuffix.setText("Photo suffix :");
		labelSuffix.setBounds(new Rectangle(3, y + FORM_LABEL_DELTA, FORM_LINE_TAB, FORM_LINE_HEIGHT));
		textSuffix = new Text(sShell, SWT.BORDER);
		textSuffix.setText("");
		textSuffix.setBounds(new Rectangle(FORM_LINE_TAB + FORM_LINE_SPACE, y, 200, FORM_LINE_HEIGHT));

		y += FORM_LINE_HEIGHT + FORM_LINE_SPACE;
		labelDelta = new Label(sShell, SWT.NONE);
		labelDelta.setText("Delta :");
		labelDelta.setBounds(new Rectangle(3, y + FORM_LABEL_DELTA, FORM_LINE_TAB, FORM_LINE_HEIGHT));
		textDelta = new Text(sShell, SWT.BORDER);
		textDelta.setText("0s");
		textDelta.setBounds(new Rectangle(FORM_LINE_TAB + FORM_LINE_SPACE, y, 200, FORM_LINE_HEIGHT));

		y += FORM_LINE_HEIGHT + FORM_LINE_SPACE;
		labelPattern = new Label(sShell, SWT.NONE);
		labelPattern.setText("Date pattern :");
		labelPattern.setBounds(new Rectangle(3, y + FORM_LABEL_DELTA, FORM_LINE_TAB, FORM_LINE_HEIGHT));
		textPattern = new Text(sShell, SWT.BORDER);
		textPattern.setText(DEFAULT_PATTERN);
		textPattern.setBounds(new Rectangle(FORM_LINE_TAB + FORM_LINE_SPACE, y, 200, FORM_LINE_HEIGHT));

		buttonProceed = new Button(sShell, SWT.NONE);
		buttonProceed.setText("Proceed !");
		buttonProceed.setBounds(new Rectangle(386, y, 119, FORM_BUTTON_HEIGHT));
		buttonProceed.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
			@Override
			public void mouseDown(final org.eclipse.swt.events.MouseEvent e) {
				proceed();
			}
		});

		buttonHelp = new Button(sShell, SWT.NONE);
		buttonHelp.setText(" ? ");
		buttonHelp.setBounds(new Rectangle(355, y, 29, FORM_BUTTON_HEIGHT));
		buttonHelp.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
			@Override
			public void mouseDown(final org.eclipse.swt.events.MouseEvent e) {
				openHelpDialog("Help - PhotoRenamer", FORM_HELP);
			}
		});

		y += FORM_LINE_HEIGHT + FORM_LINE_SPACE;
		checkboxRenameExtra = new Button(sShell, SWT.CHECK);
		checkboxRenameExtra.setText("Rename extra files");
		checkboxRenameExtra.setSelection(true);
		checkboxRenameExtra.setBounds(new Rectangle(3, y, 200, FORM_LINE_HEIGHT));

		y += FORM_LINE_HEIGHT + FORM_LINE_SPACE;
		checkboxRotateImage = new Button(sShell, SWT.CHECK);
		checkboxRotateImage.setText("Rotate images");
		checkboxRotateImage.setSelection(true);
		checkboxRotateImage.setBounds(new Rectangle(3, y, 200, FORM_LINE_HEIGHT));

		y += FORM_LINE_HEIGHT + FORM_LINE_SPACE;
		checkboxRetroDateExif = new Button(sShell, SWT.CHECK);
		checkboxRetroDateExif.setText("Set absent Exif date from filename if possible");
		checkboxRetroDateExif.setSelection(true);
		checkboxRetroDateExif.setBounds(new Rectangle(3, y, 400, FORM_LINE_HEIGHT));
		
		y += (FORM_LINE_HEIGHT + FORM_LINE_SPACE) * 2;
		sShell.setSize(new Point(600, y));
		loadPreferences();
	}

	private void openHelpDialog(final String title, final String text) {
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
			core.setRenameExtraFiles(checkboxRenameExtra.getSelection());
			core.setRotateImages(checkboxRotateImage.getSelection());
			core.setRetroDateExif(checkboxRetroDateExif.getSelection());

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						// show console
						ConsoleFormAppender.focus();

						// process
						core.processFolder();

					} catch (Exception e) {
						e.printStackTrace();
						log.debug("Exception: " + e.toString());
						
					} finally {
						// processing finished
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								buttonProceed.setEnabled(true);
							}
						});
					}
				}
			};
			new Thread(runnable).start();
		} catch (Exception e) {
			log.error("Error: " + e, e);
			buttonProceed.setEnabled(true);
			return;
		}
	}
}
