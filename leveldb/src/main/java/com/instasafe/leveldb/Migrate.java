package com.instasafe.leveldb;

import android.util.Log;

import com.edwardstock.leveldb.Iterator;
import com.edwardstock.leveldb.LevelDB;
import com.edwardstock.leveldb.exception.LevelDBException;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class Migrate {
    private static Gson gson = new Gson();

    public static List<GenericKeyValue> read(String levelDBPath) {
        List<GenericKeyValue> values = new ArrayList<>();
        LevelDB.Config config = new LevelDB.Config();
        LevelDB levelDB = null;
        try {
            levelDB = LevelDB.Companion.open(levelDBPath, config);
            Iterator iterator = levelDB.iterator();

            Log.d("MigrateLevelDB", "Reading...;");

            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                byte[] key = iterator.key();
                byte[] value = iterator.value();
                try {
                    GenericKeyValue keyValue = new GenericKeyValue(new String(key, StandardCharsets.UTF_8), new String(value, StandardCharsets.UTF_8));
                    values.add(keyValue);
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
            gson.toJson(list, new FileWriter((folderPath.endsWith("/") ? folderPath + "leveldb.data" : folderPath + "/" + "leveldb.data")));
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
