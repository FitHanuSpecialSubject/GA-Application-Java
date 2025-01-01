package com.example.SS2_Backend.util;

import com.example.SS2_Backend.constants.AppConst;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.Serializable;

/**
 * naming SimpleFileUtils not to clash with apache commons FileUtils
 */
public class SimpleFileUtils {

    public static String getFilePath(String dir, String fileName, String extension) {
        fileName = org.apache.commons.lang3.StringUtils.join(new String[] {fileName, extension}, ".");
        return StringUtils.join(new String[] {dir, fileName}, "/");
    }


    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean isObjectSerializable(Object obj) {
        return obj instanceof Serializable;
    }

}
