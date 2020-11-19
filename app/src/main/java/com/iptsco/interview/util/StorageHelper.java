package com.iptsco.interview.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
     * @return Text contained in file
     */
    private static String readFile(File file) {
        StringBuilder buffer = new StringBuilder();
        try {
            FileInputStream fin = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fin);
            BufferedReader br = new BufferedReader(isr);

            String readString = br.readLine();
            while (readString != null) {
                buffer.append(readString);
                readString = br.readLine();
            }

            isr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    /**
     * Writes text to file
     *
     * @param file File on storage
     * @param text Text to be written
     */
    private static void writeFile(File file, String text) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            fos.write(text.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets text from a saved file in storage
     *
     * @param context  Storage owner
     * @param fileName Filename to be read
     * @return String in file
     */
    public static String getTextFromStorage(Context context, String fileName) {
        return readFile(createOrGetFile(context, fileName));
    }

    /**
     * Saves a text in file storage
     *
     * @param context  Storage owner
     * @param fileName Filename to be saved
     * @param text     Text to set in file
     */
    public static void setTextInStorage(Context context, String fileName, String text) {
        writeFile(createOrGetFile(context, fileName), text);
    }
}
