package org.fit.ssapp.util;

import java.io.File;
import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for file-related operations.
 */
public class SimpleFileUtils {

  /**
   * Constructs a file path by joining the directory, filename, and extension.
   *
   * @param dir       The directory path.
   * @param fileName  The name of the file (without extension).
   * @param extension The file extension (e.g., "txt", "json").
   * @return The full file path as a string.
   */
  public static String getFilePath(String dir, String fileName, String extension) {
    fileName = org.apache.commons.lang3.StringUtils.join(new String[]{fileName, extension}, ".");
    return StringUtils.join(new String[]{dir, fileName}, "/");
  }

  /**
   * Checks if a file exists at the given path.
   *
   * @param filePath The path of the file.
   * @return {@code true} if the file exists, otherwise {@code false}.
   */
  public static boolean isFileExist(String filePath) {
    File file = new File(filePath);
    return !file.exists();
  }

  /**
   * Checks if an object is serializable.
   *
   * @param obj The object to check.
   * @return {@code true} if the object implements {@code Serializable}, otherwise {@code false}.
   */
  public static boolean isObjectSerializable(Object obj) {
    return obj instanceof Serializable;
  }

}
