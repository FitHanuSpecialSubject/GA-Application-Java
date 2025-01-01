package com.example.SS2_Backend.util;

import com.example.SS2_Backend.constants.AppConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.moeaframework.core.Problem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;

import static com.example.SS2_Backend.util.SimpleFileUtils.getFilePath;

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
        if (!SimpleFileUtils.isFileExist(dataFilePath)) {
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
