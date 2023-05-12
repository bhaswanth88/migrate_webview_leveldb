package com.instasafe.leveldb;

import android.util.Log;

import com.edwardstock.leveldb.Iterator;
import com.edwardstock.leveldb.LevelDB;
import com.edwardstock.leveldb.exception.LevelDBException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Migrate {
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static List<GenericKeyValue> read(String levelDBPath) {
        List<GenericKeyValue> values = new ArrayList<>();
        LevelDB.Config config = new LevelDB.Config();
        LevelDB levelDB = null;
        try {
            levelDB = LevelDB.Companion.open(levelDBPath, config);
            Iterator iterator = levelDB.iterator();

            Log.d("MigrateLevelDB", "Reading...;");
//            List<String> acceptedKeyStrings = new ArrayList<>();
//            acceptedKeyStrings.add("insta_firebase_token");
//            acceptedKeyStrings.add("insta_push");
//            acceptedKeyStrings.add("insta_mpin");
//            acceptedKeyStrings.add("insta_profile_list");
//            acceptedKeyStrings.add("insta_last_active_time");
//            acceptedKeyStrings.add("insta_domain_name");

            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                byte[] key = iterator.key();
                byte[] value = iterator.value();
                try {


                    String keyString = new String(key);
                    byte[] charsetKey = keyString.getBytes("UTF-8");
                    keyString = new String(charsetKey, "UTF-8");


                    String valueString = new String(value);
                    byte[] charsetValue = valueString.getBytes("UTF-8");
                    valueString = new String(charsetValue, "UTF-8");


                    if (keyString.contains("insta")) {
                        keyString=keyString.substring(keyString.indexOf("insta"));
                        GenericKeyValue keyValue = new GenericKeyValue(keyString, valueString);
                        values.add(keyValue);
                        Log.d("MigrateLevelDB", "Adding  keyvalue json=> " + gson.toJson(keyValue));
                    } else {
                        Log.d("MigrateLevelDB", "Ignoring  key => " + keyString);

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MigrateLevelDB", "Ignored Exception=> " + e.getMessage(), e);
                }
            }
            iterator.close();
            levelDB.close();
            Log.d("MigrateLevelDB", "Reading...;");

        } catch (LevelDBException e) {
            e.printStackTrace();
            Log.e("MigrateLevelDB", "Exception=> " + e.getMessage(), e);
        }
        Log.d("MigrateLevelDB", "Successfully migrated localStorage..");
        return values;
    }

    public static void writeToFile(String folderPath, List<GenericKeyValue> list) {
        try {
            String jsonString = gson.toJson(list);
            Log.d("MigrateLevelDB", "Got JsonString of whole file as =>" + jsonString);

            FileWriter writer = new FileWriter((folderPath.endsWith("/") ? folderPath + "leveldb.data" : folderPath + "/" + "leveldb.data"));
            writer.write(jsonString);
            writer.flush();
            writer.close();
            Log.d("MigrateLevelDB", "Wrote JsonString to whole file");
            createCompletedFile(folderPath);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MigrateLevelDB", "Exception=> " + e.getMessage(), e);
        }

    }

    public static void createCompletedFile(String folderPath) {
        File completedFile = new File(folderPath + "/leveldb.read");
        try {
            if (completedFile.createNewFile()) {
                Log.d("MigrateLevelDB", "Create leveldb.read file");

            } else {
                Log.d("MigrateLevelDB", "Failed to create leveldb.read file");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MigrateLevelDB", "Exception=> " + e.getMessage(), e);
        }
    }
}
