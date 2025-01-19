package org.fit.ssapp.util;

import static org.fit.ssapp.util.SimpleFileUtils.getFilePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.fit.ssapp.constants.AppConst;
import org.moeaframework.core.Problem;

@Slf4j
public class ProblemUtils {

  private ProblemUtils() {
  }

  /**
   * Handle write problem to file
   *
   * @param problem  problem Obj
   * @param fileName file name
   * @return true if success
   */
  public static boolean writeProblemToFile(Problem problem, String fileName) {
    if (!SimpleFileUtils.isObjectSerializable(problem)) {
      return false;
    }
    try {
      String dataFilePath = getFilePath(AppConst.DATA_DIR, fileName, AppConst.DATA_EXT);
      File file = new File(dataFilePath);
      boolean isAppend = false;

      FileUtils.touch(file);
      FileOutputStream fOut = new FileOutputStream(file, isAppend);
      SerializationUtils.serialize((Serializable) problem, fOut);

      return true;

    } catch (Exception e) {
      return false;
    }
  }

  public static Problem readProblemFromFile(String dataFilePath) {
    if (SimpleFileUtils.isFileExist(dataFilePath)) {
      return null;
    }
    try {
      FileInputStream fIn = new FileInputStream(dataFilePath);
      Object obj = SerializationUtils.deserialize(fIn);
      if (obj instanceof Problem) {
        return (Problem) obj;
      }
    } catch (FileNotFoundException e) {
      return null;
    }
    return null;
  }

}
