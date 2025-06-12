package com.outdu.camconnect.communication;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Data {

    private static boolean FAR=false;
    private static boolean OD=true;
    private static boolean DS=false;
    private static boolean AUDIO=false;
    private static int   MODEL = 0;
    private static float DS_THRESHOLD =0.8f;

    public static void loadData(Context context)
    {
        // Load properties from file in a static block
        try {
            File filesDir=context.getExternalFilesDir(null);
            if(!filesDir.exists()) {
                filesDir.mkdir();
            }
            File f=new File(filesDir, "config.properties");
            FileInputStream inputStream = new FileInputStream(f);
            Properties properties = new Properties();
            properties.load(inputStream);

            // Retrieve values by key
            String far = properties.getProperty("far");
            String od = properties.getProperty("od");
            String ds = properties.getProperty("ds");
            String model = properties.getProperty("model");
            String ds_threshold = properties.getProperty("ds_threshold");
            String audio = properties.getProperty("audio");

            FAR = Boolean.parseBoolean(far);
            OD = Boolean.parseBoolean(od);
            DS = Boolean.parseBoolean(ds);
            MODEL = Integer.parseInt(model);
            DS_THRESHOLD = Float.parseFloat(ds_threshold);
            AUDIO = Boolean.parseBoolean(audio);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(Context context)
    {
        try {
            Properties properties = new Properties();

            // Add your key-value pairs here
            properties.setProperty("far", String.valueOf(FAR));
            properties.setProperty("od", String.valueOf(OD));
            properties.setProperty("ds", String.valueOf(DS));
            properties.setProperty("model", String.valueOf(MODEL));
            properties.setProperty("ds_threshold", String.valueOf(DS_THRESHOLD));
            properties.setProperty("audio", String.valueOf(AUDIO));

            // Save properties to a file
            File filesDir=context.getExternalFilesDir(null);
            if(!filesDir.exists()) {
                filesDir.mkdir();
            }
            File f=new File(filesDir, "config.properties");
            FileOutputStream outputStream = new FileOutputStream(f);
            properties.store(outputStream, null);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFAR() {
        return FAR;
    }

    public static void setFAR(Context context, boolean FAR) {
        Data.FAR = FAR;
        writeToFile(context);
    }

    public static float getDsThreshold() {
        return DS_THRESHOLD;
    }

    public static void setDsThreshold(Context context, float dsThreshold) {
        DS_THRESHOLD = dsThreshold;
        writeToFile(context);
    }

    public static boolean isAUDIO() {
        return AUDIO;
    }

    public static void setAUDIO(Context context, boolean AUDIO) {
        Data.AUDIO = AUDIO;
        writeToFile(context);
    }

    public static void setOD(Context context, boolean OD) {
        Data.OD = OD;
        writeToFile(context);
    }

    public static int getMODEL() {
        return MODEL;
    }

    public static void setMODEL(Context context, int MODEL) {
        Data.MODEL = MODEL;
        writeToFile(context);
    }

    public static void setDS(Context context, boolean DS) {
        Data.DS = DS;
        writeToFile(context);
    }

    public static boolean isOD() {
        return OD;
    }

    public static boolean isDS() {
        return DS;
    }
}
