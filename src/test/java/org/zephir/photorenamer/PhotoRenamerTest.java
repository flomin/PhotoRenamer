package org.zephir.photorenamer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zephir.photorenamer.core.PhotoRenamerConstants;
import org.zephir.photorenamer.core.PhotoRenamerCore;

public class PhotoRenamerTest {
	@BeforeClass
	public static void setUpClass() throws Exception {}

	@AfterClass
	public static void tearDownClass() throws Exception {}

	@Before
	public void setUp() {}

	@After
	public void tearDown() {}

	@Test
	public void testScalene() throws Exception {
		final PhotoRenamerCore core = new PhotoRenamerCore();
		core.setFolderToProcess("D:\\Boulot\\Java\\PhotoRenamer-git\\src\\test\\resources\\");
		core.setSuffix("");
		core.setPattern(PhotoRenamerConstants.DEFAULT_PATTERN);
		core.setDelta("");
		core.setRenameExtraFiles(true);
		core.setRotateImages(true);
		
		core.processFolder();
	}
}
