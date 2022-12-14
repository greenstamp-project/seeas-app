package com.example.cadprodutos.filehelpers;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class FileHelper {
    public static String functionName = "function";
    public static String parametersFileName = "parameters.txt";
    public static String fileToReadName = "fileToRead";
    public static String timesToRun = "timesToRun";
    public static String email = "email";
    public static String pass = "pass";


    public static String paramNameMakeFile = "makeFile";
    public static String paramNameMakeEncFile = "makeEncFile";
    public static String paramNameSaveCloud = "saveCloud";
    public static String paramNameLocalLogin = "localLogin";


    public static Map<String, String> readParameters() {
        Map<String, String> parameters = new HashMap<>();

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), parametersFileName);

        //se o arquivo não existir cria com os parâmetros iniciais
        if (!file.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                String fileStartContent = "function=makeFile\n" +
                        "fileToRead=somefile.txt\n" +
                        "timesToRun=1\n" +
                        "email=test1@gmail.com\n" +
                        "pass=test1";
                fos.write((fileStartContent).getBytes());
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

        if (!file.exists()) {
            return new byte[0];
        }

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

    public static List<String> readCredentialsNotEnc() {
        List<String> credentials = new ArrayList<>();

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "no-encripted-credentials.txt");

        //se o arquivo não existir cria com os parâmetros iniciais
        if (!file.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                String fileStartContent = "test1@test.com\n" + "pass1";
                fos.write((fileStartContent).getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                credentials.add(line);
            }
            br.close();

        } catch (IOException e) {
            Log.e("mytag", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }

        return credentials;
    }
}


