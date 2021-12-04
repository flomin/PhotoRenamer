package org.zephir.photorenamer;

import org.junit.Test;
import org.zephir.photorenamer.core.PhotoRenamerConstants;
import org.zephir.photorenamer.core.PhotoRenamerCore;

public class PhotoRenamerTest {

  @Test
  public void testScalene() throws Exception {
    final PhotoRenamerCore core = new PhotoRenamerCore();
    core.setFolderToProcess("C:\\wind\\Boulot\\Java\\PhotoRenamer\\src\\test\\resources\\test\\");
    core.setSuffix("");
    core.setPattern(PhotoRenamerConstants.DEFAULT_PATTERN);
    core.setDelta("");
    core.setRenameExtraFiles(true);
    core.setRotateImages(true);
    core.setRetroDateExif(true);

    core.processFolder();
  }
}
