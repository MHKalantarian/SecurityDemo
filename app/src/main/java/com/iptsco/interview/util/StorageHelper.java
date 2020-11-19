package com.iptsco.interview.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by MHK on 11/19/2020.
 * www.MHKSoft.com
 */
public class StorageHelper {
    /**
     * Create or get file from internal storage
     *
     * @param context  Storage owner
     * @param fileName Filename to be saved
     * @return Created file
     */
    private static File createOrGetFile(Context context, String fileName) {
        return new File(context.getFilesDir(), fileName);
    }

    /**
     * Reads text in file
     *
     * @param file File to be read
     * @return Data contained in file
     */
    private static byte[] readFile(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Writes text to file
     *
     * @param file File on storage
     * @param data Data to be written
     */
    private static void writeFile(File file, byte[] data) throws IOException {
        FileOutputStream fos;
        fos = new FileOutputStream(file);
        fos.write(data);
        fos.flush();
        fos.close();
    }

    /**
     * Gets text from a saved file in storage
     *
     * @param context  Storage owner
     * @param fileName Filename to be read
     * @return String in file
     */
    public static byte[] getDataFromStorage(Context context, String fileName) throws IOException {
        return readFile(createOrGetFile(context, fileName));
    }

    /**
     * Saves a text in file storage
     *
     * @param context  Storage owner
     * @param fileName Filename to be saved
     * @param data     Data to set in file
     */
    public static void setDataInStorage(Context context, String fileName, byte[] data) throws IOException {
        writeFile(createOrGetFile(context, fileName), data);
    }
}
