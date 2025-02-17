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

/**
 * Enables logging using Lombok's @Slf4j. Provides a static logger instance for this class.
 */
@Slf4j
public class ProblemUtils {

  private ProblemUtils() {
  }

  /**
   * Writes a given Problem object to a file.
   *
   * @param problem  the Problem object to serialize and save
   * @param fileName the name of the file where the object should be written
   * @return {@code true} if the problem was successfully written, {@code false} otherwise
   */
  @SuppressWarnings("unused")
  public static boolean writeProblemToFile(Problem problem, String fileName) {
    if (!SimpleFileUtils.isObjectSerializable(problem)) {
      return false;
    }
    try {
      String dataFilePath = getFilePath(AppConst.DATA_DIR, fileName, AppConst.DATA_EXT);
      File file = new File(dataFilePath);
      boolean isAppend = false;

      FileUtils.touch(file);
      FileOutputStream fut = new FileOutputStream(file, isAppend);
      SerializationUtils.serialize((Serializable) problem, fut);

      return true;

    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Reads a Problem object from a file.
   *
   * @param dataFilePath the file path to read from
   * @return the deserialized Problem object, or {@code null} if the operation fails
   */
  public static Problem readProblemFromFile(String dataFilePath) {
    if (SimpleFileUtils.isFileExist(dataFilePath)) {
      return null;
    }
    try {
      FileInputStream fin = new FileInputStream(dataFilePath);
      Object obj = SerializationUtils.deserialize(fin);
      if (obj instanceof Problem) {
        return (Problem) obj;
      }
    } catch (FileNotFoundException e) {
      return null;
    }
    return null;
  }

}
