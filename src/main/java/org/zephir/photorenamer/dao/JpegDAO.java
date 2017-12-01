package org.zephir.photorenamer.dao;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffTagConstants;
import org.apache.sanselan.formats.tiff.fieldtypes.FieldType;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephir.photorenamer.core.PhotoRenamerConstants;
import org.zephir.photorenamer.core.PhotoRenamerCore;
import org.zephir.util.exception.CustomException;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import mediautil.image.jpeg.LLJTran;

public final class JpegDAO {
	private static Logger log = LoggerFactory.getLogger(PhotoRenamerCore.class);

	private static final SimpleDateFormat EXIF_DATE_FORMAT;

	static {
		EXIF_DATE_FORMAT = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);
		EXIF_DATE_FORMAT.setTimeZone(TimeZone.getDefault());
	}

	private JpegDAO() {
	}

	public static Date getDateTimeOriginal(final File file) throws CustomException {
		try {
			Date date = null;
			try {
				// try using Sanselan library
				IImageMetadata metadata = Sanselan.getMetadata(file);
				if (metadata instanceof JpegImageMetadata) {
					JpegImageMetadata metaJpg = (JpegImageMetadata) metadata;
					if (metaJpg != null) {
						TiffField dateTimeOriginalField = metaJpg.findEXIFValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
						TiffField createDateField = metaJpg.findEXIFValue(TiffConstants.EXIF_TAG_CREATE_DATE);
						TiffField modifyDateField = metaJpg.findEXIFValue(TiffConstants.EXIF_TAG_MODIFY_DATE);
						if (dateTimeOriginalField != null) {
							date = EXIF_DATE_FORMAT.parse(dateTimeOriginalField.getStringValue());
						} else if (createDateField != null) {
							date = EXIF_DATE_FORMAT.parse(createDateField.getStringValue());
						} else if (modifyDateField != null) {
							date = EXIF_DATE_FORMAT.parse(modifyDateField.getStringValue());
						}
						if (date != null && (dateTimeOriginalField == null || createDateField == null || modifyDateField == null)) {
							boolean workDone = setDateTimeOriginal(file, date, false);
							if (workDone) {
								log.debug(PhotoRenamerCore.getLogPrefix() + "'" + file.getAbsolutePath() + "' ---> missing EXIF date(s) added");
							}
						}
					}
				}
			} catch (ImageReadException e) {
				// try using drewnoakes library (read only)
				final Metadata metadata = ImageMetadataReader.readMetadata(file);
				final ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
				if (directory != null) {
					date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
					if (date == null) {
						date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
					}
					if (date == null) {
						date = directory.getDate(ExifIFD0Directory.TAG_DATETIME);
					}
				}
			}
			return date;
		} catch (Throwable e) {
			throw new CustomException("getDateTimeOriginal(file='" + file.getAbsolutePath() + "') KO: " + e, e);
		}
	}

	public static boolean setDateTimeOriginal(final File file, final Date dateFromFilename, boolean removeFieldIfExists) throws CustomException {
		try {
			if (dateFromFilename == null) {
				throw new NullPointerException("dateFromFilename can't be null");
			}
			boolean workDone = false;

			IImageMetadata metadata = Sanselan.getMetadata(file);
			if (metadata instanceof JpegImageMetadata) {
				JpegImageMetadata metaJpg = (JpegImageMetadata) metadata;

				TiffImageMetadata exif = null;
				if (metaJpg != null) {
					exif = metaJpg.getExif();
				}
				TiffOutputSet outputSet = new TiffOutputSet();
				if (exif != null) {
					outputSet = exif.getOutputSet();
				}

				// change original date tag info
				if (outputSet != null) {
					TiffOutputDirectory rootDirectory = outputSet.getRootDirectory();
					TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();

					String asciiTiffDate = EXIF_DATE_FORMAT.format(dateFromFilename) + 0x00;

					// new
					// String((byte[])outputSet.findField(TiffConstants.EXIF_TAG_MODIFY_DATE).bytes)
					// EXIF spec: https://www.media.mit.edu/pia/Research/deepview/exif.html

					// Date Time Original 0x9003 DateTimeOriginal - Exif SubIFD
					if (removeFieldIfExists || outputSet.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL) == null) {
						TiffOutputField dateTimeOriginalField = new TiffOutputField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, FieldType.FIELD_TYPE_ASCII,
								asciiTiffDate.length(), asciiTiffDate.getBytes());
						outputSet.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
						exifDirectory.add(dateTimeOriginalField);
						workDone = true;
					}

					// Create Date 0x9004 DateTimeDigitized - Exif SubIFD
					if (removeFieldIfExists || outputSet.findField(ExifTagConstants.EXIF_TAG_CREATE_DATE) == null) {
						TiffOutputField createDateField = new TiffOutputField(TiffConstants.EXIF_TAG_CREATE_DATE, TiffConstants.FIELD_TYPE_ASCII,
								asciiTiffDate.length(), asciiTiffDate.getBytes());
						outputSet.removeField(ExifTagConstants.EXIF_TAG_CREATE_DATE);
						exifDirectory.add(createDateField);
						workDone = true;
					}

					// Modify date 0x0132 DateTime - IFD0 (main image)
					if (removeFieldIfExists || outputSet.findField(ExifTagConstants.EXIF_TAG_MODIFY_DATE) == null) {
						TiffOutputField modifyDateField = new TiffOutputField(TiffConstants.EXIF_TAG_MODIFY_DATE, TiffConstants.FIELD_TYPE_ASCII,
								asciiTiffDate.length(), asciiTiffDate.getBytes());
						outputSet.removeField(ExifTagConstants.EXIF_TAG_MODIFY_DATE);
						rootDirectory.add(modifyDateField);
						workDone = true;
					}

					// Software
					if (outputSet.findField(ExifTagConstants.EXIF_TAG_SOFTWARE) == null) {
						String softwareFieldStr = "PhotoRenamer-" + PhotoRenamerConstants.VERSION;
						TiffOutputField softwareField = new TiffOutputField(ExifTagConstants.EXIF_TAG_SOFTWARE, ExifTagConstants.FIELD_TYPE_ASCII,
								softwareFieldStr.length(), softwareFieldStr.getBytes());
						outputSet.removeField(ExifTagConstants.EXIF_TAG_SOFTWARE);
						rootDirectory.add(softwareField);
					}
				}

				if (workDone) {
					// create stream using temp file for dst
					File tempFile = File.createTempFile("temp-" + System.currentTimeMillis(), ".jpeg");
					OutputStream os = new BufferedOutputStream(new FileOutputStream(tempFile));

					// write/update EXIF metadata to output stream
					try {
						new ExifRewriter().updateExifMetadataLossy(file, os, outputSet);
					} finally {
						if (os != null) {
							os.close();
						}
					}

					// copy temp file over original file
					FileUtils.copyFile(tempFile, file);
				}

				return workDone;
			}
			return false;

		} catch (Throwable e) {
			throw new CustomException("setDateTimeOriginal(file='" + file.getAbsolutePath() + "') KO: " + e, e);
		}
	}

	// // http://www.impulseadventure.com/photo/exif-orientation.html
	// // EXIF orientation values
	// // 1 = top, left
	// // 3 = bottom, right
	// // 6 = right, top
	// // 8 = left, bottom
	// // 2* = top, right
	// // 4* = bottom, left
	// // 5* = left, top
	// // 7* = right, bottom
	// public static boolean rotateImage(final File file) throws CustomException
	// {
	// try {
	// final Metadata metadata = ImageMetadataReader.readMetadata(file);
	// final ExifIFD0Directory exifIFD0Directory =
	// metadata.getDirectory(ExifIFD0Directory.class);
	// if (exifIFD0Directory != null) {
	// int orientation =
	// exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
	// int rotationNeeded = -1;
	// if (orientation == 6) { // orientation right, top
	// // rotate 90° clockwise
	// rotationNeeded = LLJTran.ROT_90;
	// log.debug("Rotating '" + file.getAbsolutePath() + "' 90° clockwise.");
	// } else if (orientation == 3) { // orientation bottom, right
	// // rotate 180° clockwise
	// rotationNeeded = LLJTran.ROT_180;
	// log.debug("Rotating '" + file.getAbsolutePath() + "' 180° clockwise.");
	// } else if (orientation == 8) { // orientation left, bottom
	// // rotate 270° clockwise
	// rotationNeeded = LLJTran.ROT_270;
	// log.debug("Rotating '" + file.getAbsolutePath() + "' 270° clockwise.");
	// }
	//
	// if (rotationNeeded != -1) {
	// // 1. Initialize LLJTran and Read the entire Image including Appx markers
	// LLJTran llj = new LLJTran(file);
	// // If you pass the 2nd parameter as false, Exif information is not
	// // loaded and hence will not be written.
	// llj.read(LLJTran.READ_ALL, true);
	//
	// // 2. Transform the image using default options along with
	// // transformation of the Orientation tags. Try other combinations of
	// // LLJTran_XFORM.. flags. Use a jpeg with partial MCU (partialMCU.jpg)
	// // for testing LLJTran.XFORM_TRIM and LLJTran.XFORM_ADJUST_EDGES
	// int options = LLJTran.OPT_DEFAULTS | LLJTran.OPT_XFORM_ORIENTATION;
	// llj.transform(rotationNeeded, options);
	//
	// // 3. Save the Image which is already transformed as specified by the
	// // input transformation in Step 2, along with the Exif header.
	// OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
	// llj.save(out, LLJTran.OPT_WRITE_ALL);
	// out.close();
	//
	// // Cleanup
	// llj.freeMemory();
	//
	// // update the metadata back
	// TiffImageMetadata exif = null;
	// if (metaJpg != null) {
	// exif = metaJpg.getExif();
	// }
	// TiffOutputSet outputSet = new TiffOutputSet();
	// if (exif != null) {
	// outputSet = exif.getOutputSet();
	// }
	//
	// // change orientation tag info
	// if (outputSet != null) {
	// outputSet.removeField(TiffConstants.TIFF_TAG_ORIENTATION);
	// TiffOutputField newOrientationField =
	// TiffOutputField.create(ExifTagConstants.EXIF_TAG_ORIENTATION,
	// outputSet.byteOrder, 1);
	// TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
	// exifDirectory.add(newOrientationField);
	// }
	//
	// // create stream using temp file for dst
	// File tempFile = File.createTempFile("temp-" + System.currentTimeMillis(),
	// ".jpeg");
	// OutputStream os = new BufferedOutputStream(new
	// FileOutputStream(tempFile));
	//
	// // write/update EXIF metadata to output stream
	// try {
	// new ExifRewriter().updateExifMetadataLossy(file, os, outputSet);
	// } finally {
	// if (os != null) {
	// os.close();
	// }
	// }
	//
	// // copy temp file over original file
	// FileUtils.copyFile(tempFile, file);
	//
	// return true;
	// }
	// }
	// return false;
	// } catch (Throwable e) {
	// throw new CustomException("rotateImage(file='" + file.getAbsolutePath() +
	// "') KO: " + e, e);
	// }
	// }

	// http://www.impulseadventure.com/photo/exif-orientation.html
	// EXIF orientation values
	// 1 = top, left
	// 3 = bottom, right
	// 6 = right, top
	// 8 = left, bottom
	// 2* = top, right
	// 4* = bottom, left
	// 5* = left, top
	// 7* = right, bottom
	public static boolean rotateImage(final File file) throws CustomException {
		try {
			IImageMetadata metadata = Sanselan.getMetadata(file);
			if (metadata instanceof JpegImageMetadata) {
				JpegImageMetadata metaJpg = (JpegImageMetadata) metadata;
				TiffField orientationField = metaJpg.findEXIFValue(TiffTagConstants.TIFF_TAG_ORIENTATION);
				if (orientationField != null) {
					int orientation = orientationField.getIntValue();
					int rotationNeeded = -1;
					if (orientation == 6) { // orientation right, top
						// rotate 90° clockwise
						rotationNeeded = LLJTran.ROT_90;
						log.debug("Rotating '" + file.getAbsolutePath() + "' 90° clockwise.");
					} else if (orientation == 3) { // orientation bottom, right
						// rotate 180° clockwise
						rotationNeeded = LLJTran.ROT_180;
						log.debug("Rotating '" + file.getAbsolutePath() + "' 180° clockwise.");
					} else if (orientation == 8) { // orientation left, bottom
						// rotate 270° clockwise
						rotationNeeded = LLJTran.ROT_270;
						log.debug("Rotating '" + file.getAbsolutePath() + "' 270° clockwise.");
					}

					if (rotationNeeded != -1) {
						// 1. Initialize LLJTran and Read the entire Image
						// including Appx markers
						LLJTran llj = new LLJTran(file);
						// If you pass the 2nd parameter as false, Exif
						// information is not
						// loaded and hence will not be written.
						llj.read(LLJTran.READ_ALL, true);

						// 2. Transform the image using default options along
						// with
						// transformation of the Orientation tags. Try other
						// combinations of
						// LLJTran_XFORM.. flags. Use a jpeg with partial MCU
						// (partialMCU.jpg)
						// for testing LLJTran.XFORM_TRIM and
						// LLJTran.XFORM_ADJUST_EDGES
						int options = LLJTran.OPT_DEFAULTS | LLJTran.OPT_XFORM_ORIENTATION;
						llj.transform(rotationNeeded, options);

						// 3. Save the Image which is already transformed as
						// specified by the
						// input transformation in Step 2, along with the Exif
						// header.
						OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
						llj.save(out, LLJTran.OPT_WRITE_ALL);
						out.close();

						// Cleanup
						llj.freeMemory();

						// update the metadata back
						TiffImageMetadata exif = null;
						if (metaJpg != null) {
							exif = metaJpg.getExif();
						}
						TiffOutputSet outputSet = new TiffOutputSet();
						if (exif != null) {
							outputSet = exif.getOutputSet();
						}

						// change orientation tag info
						if (outputSet != null) {
							outputSet.removeField(TiffConstants.TIFF_TAG_ORIENTATION);
							TiffOutputField newOrientationField = TiffOutputField.create(ExifTagConstants.EXIF_TAG_ORIENTATION, outputSet.byteOrder, 1);
							TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
							exifDirectory.add(newOrientationField);
						}

						// create stream using temp file for dst
						File tempFile = File.createTempFile("temp-" + System.currentTimeMillis(), ".jpeg");
						OutputStream os = new BufferedOutputStream(new FileOutputStream(tempFile));

						// write/update EXIF metadata to output stream
						try {
							new ExifRewriter().updateExifMetadataLossy(file, os, outputSet);
						} finally {
							if (os != null) {
								os.close();
							}
						}

						// copy temp file over original file
						FileUtils.copyFile(tempFile, file);

						return true;
					}
				}
			}
			return false;
		} catch (Throwable e) {
			throw new CustomException("rotateImage(file='" + file.getAbsolutePath() + "') KO: " + e, e);
		}
	}

	// public static boolean splitImageVertically(final File file, final File
	// destinationFolder) throws CustomException {
	// File firstImage = null, secondImage = null;
	// try {
	// Image image1 = ImageIO.read(file);
	// BufferedImage buffered = (BufferedImage) image1;
	//
	// JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
	// jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	// jpegParams.setCompressionQuality(1f);
	// IIORegistry registry = IIORegistry.getDefaultInstance();
	//
	// Iterator<ImageWriterSpi> services =
	// registry.getServiceProviders(ImageWriterSpi.class, new
	// ServiceRegistry.Filter() {
	// @Override
	// public boolean filter(Object provider) {
	// if (!(provider instanceof ImageWriterSpi))
	// return false;
	//
	// ImageWriterSpi writerSPI = (ImageWriterSpi) provider;
	// String[] formatNames = writerSPI.getFormatNames();
	// for (int i = 0; i < formatNames.length; i++) {
	// if (formatNames[i].equalsIgnoreCase("JPEG")) {
	// return true;
	// }
	// }
	//
	// return false;
	// }
	// }, true);
	// ImageWriterSpi writerSpi = services.next();
	// ImageWriter writer = writerSpi.createWriterInstance();
	//
	// firstImage = new File(destinationFolder,
	// FilenameUtils.getBaseName(file.getName()) + "_1." +
	// FilenameUtils.getExtension(file.getName()));
	// writer.setOutput(new FileImageOutputStream(firstImage));
	//
	// BufferedImage b1 = buffered.getSubimage(0, 0, (buffered.getWidth() / 2),
	// buffered.getHeight());
	//
	// writer.write(null, new IIOImage((RenderedImage) b1, null, null),
	// jpegParams);
	//
	// ImageWriter writer1 = writerSpi.createWriterInstance();
	// BufferedImage b2 = buffered.getSubimage(buffered.getWidth() / 2, 0,
	// buffered.getWidth() / 2, buffered.getHeight());
	//
	// secondImage = new File(destinationFolder,
	// FilenameUtils.getBaseName(file.getName()) + "_2." +
	// FilenameUtils.getExtension(file.getName()));
	// writer1.setOutput(new FileImageOutputStream(secondImage));
	//
	// writer1.write(null, new IIOImage((RenderedImage) b2, null, null),
	// jpegParams);
	// return false;
	//
	// } catch (Throwable e) {
	// throw new CustomException("splitImageVertically(file='" +
	// file.getAbsolutePath() + "') KO: " + e, e);
	// }
	// }
	//

	public static boolean splitImageVertically(final File file, final File destinationFolder) throws CustomException {
		try {
			BufferedImage image = ImageIO.read(file);

			if (image.getWidth() > image.getHeight()) {
				// don't split vertical images

				// Find a suitable ImageReader
				// Iterator<ImageReader> readers =
				// ImageIO.getImageReadersByFormatName("JPEG");
				// ImageReader reader = null;
				// while (readers.hasNext()) {
				// reader = readers.next();
				// System.out.println("reader: " + reader);
				// }

				// Working with this plugin:
				// https://github.com/haraldk/TwelveMonkeys#jpeg

				int rows = 1; // You should decide the values for rows and cols
								// variables
				int cols = 2;
				int chunks = rows * cols;

				int chunkWidth = image.getWidth() / cols; // determines the
															// chunk width and
															// height
				int chunkHeight = image.getHeight() / rows;
				int count = 0;
				BufferedImage imgs[] = new BufferedImage[chunks]; // Image array
																	// to hold
																	// image
																	// chunks
				for (int x = 0; x < rows; x++) {
					for (int y = 0; y < cols; y++) {
						// Initialize the image array with image chunks
						imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

						// draws the image chunk
						Graphics2D gr = imgs[count++].createGraphics();
						gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth,
								chunkHeight * x + chunkHeight, null);
						gr.dispose();
					}
				}

				// writing mini images into image files
				for (int i = 0; i < imgs.length; i++) {
					File splitImage = new File(destinationFolder,
							FilenameUtils.getBaseName(file.getName()) + "_" + i + "." + FilenameUtils.getExtension(file.getName()));
					ImageIO.write(imgs[i], "jpg", splitImage);
				}

			} else {
				// just copy the already vertical ones
				FileUtils.copyFile(file, new File(destinationFolder, file.getName()));
			}
			return false;

		} catch (Throwable e) {
			throw new CustomException("splitImageVertically(file='" + file.getAbsolutePath() + "') KO: " + e, e);
		}
	}
}
