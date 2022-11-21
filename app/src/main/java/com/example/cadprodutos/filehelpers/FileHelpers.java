package com.example.cadprodutos.filehelpers;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class FileHelpers {
    public static String functionName = "function";
    public static String parametersFileName = "parameters.txt";
    public static String fileToReadName = "fileToRead";
    public static String timesToRun = "timesToRun";


    public static String paramNameMakeFile = "makeFile";
    public static String paramNameSavePrefs = "savePrefs";
    public static String paramNameDatabaseSave = "databaseSave";

    public static Map<String, String> readParameters() {
        Map<String, String> parameters = new HashMap<>();

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), parametersFileName);

        //se o arquivo não existir cria com os parâmetros iniciais
        if (!file.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(("function=makeFile\n" +
                        "fileToRead=somefile.txt\n" +
                        "timesToRun=1").getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                parameters.put(parts[0], parts[1]);
            }
            br.close();

        } catch (IOException e) {
            Log.e("mytag", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }

        return parameters;
    }

    public static byte[] readFileByBytes(String fileName) {

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        byte[] tempBuf = new byte[100];
        int byteRead;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            while ((byteRead = bufferedInputStream.read(tempBuf)) != -1) {
                byteArrayOutputStream.write(tempBuf, 0, byteRead);
            }
            bufferedInputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void createFiles(String fileName, byte[] bytesToSave) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        //Escrever no arquivo
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytesToSave);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
