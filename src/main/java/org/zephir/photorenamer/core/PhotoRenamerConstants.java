package org.zephir.photorenamer.core;

public class PhotoRenamerConstants {

	private PhotoRenamerConstants() {}

	public static final String VERSION = "1.5.3";

	public static final String SUFFIX_ORIGINAL_FILENAME_TOKEN = "%ori%";
	
	public static final String DEFAULT_PATTERN = "yyyy_MMdd_HHmmss";
	
	public static final String USER_DIR = System.getProperty("user.dir");

	// command-line arguments
	public static final String COMMAND_LINE_ARG_PATTERN = "pattern";
	public static final String COMMAND_LINE_ARG_DELTA = "delta";
	public static final String COMMAND_LINE_ARG_FOLDER = "folder";
	public static final String COMMAND_LINE_ARG_SUFFIX = "suffix";
	public static final String COMMAND_LINE_ARG_VIDEO = "video";
	public static final String COMMAND_LINE_ARG_ROTATE = "rotate";
	public static final String COMMAND_LINE_ARG_HELP = "?";
	public static final String COMMAND_LINE_USAGE = "Usage: java -jar PhotoRenamer.jar [--folder <folderToProcess>] [--delta (-)(<deltaInMinutes>m|<deltaInSeconds>s|<deltaInHours>h|<deltaInHours>d]"
													+ " [--pattern <SimpleDateFormatPattern>] [--suffix <suffixText>]\n"
													+ " If one parameter contains spaces, you need to add ' around it (e.g.: --suffix ' - by wIndou').\n"
													+ " Inside the suffix, you can use "+SUFFIX_ORIGINAL_FILENAME_TOKEN+" to use the original filename without extension";
	public static final String FORM_HELP = "PhotoRenamer v" + VERSION + " made by wInd (wInd@zephir.org)\n\n" +
		
		"When you hit the 'Proceed !' button, the tool will go through the photos of the input folder,\n" +
		"and rename them based on their EXIF data and the parameters provided\n\n" +
		"Parameters: \n" +
		"Input folder: folder containing the photos to rename\n" +
		"Photo suffix: suffix to add after the date in the filename of the photos\n" +
		"Delta: time to add or substract from the EXIF date in seconds(s)/minutes(m)/hours(h)/days(d) (e.g.: -15s, 60m, -48h, -1d, 30d3h-15s)\n" +
		"Date pattern: format of the date (y: year M: month, d: day, H: hour, m: min, s: sec)\n" +
		"Rename videos: whether the video files should be renamed too based on their creation date (no EXIF data)\n" +
		"Rotate images: auto rotation of the images based on the EXIF orientation\n";
}
