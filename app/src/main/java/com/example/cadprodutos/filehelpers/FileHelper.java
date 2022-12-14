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
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


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
    public static String paramNameLocalLoginEnc = "localLoginEnc";


    public static String SECRET_KEY = "aesEncryptionKey";


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

    public static byte[] readCredentialsEnc(IvParameterSpec ivSpec) {
        byte[] bytes = new byte[0];

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "encripted-credentials.txt");

        //se o arquivo não existir cria com os parâmetros iniciais
        if (!file.exists()) {
            try {

                // encrypt file
                //String SECRET_KEY = "aesEncryptionKey";
                SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");

                FileOutputStream fos = new FileOutputStream(file);
                String fileStartContent = "test1@test.compass1";
                byte[] fileBytes = fileStartContent.getBytes(StandardCharsets.UTF_8);

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
                byte[] encrypted = cipher.doFinal(fileBytes);

                fos.write(encrypted);
                fos.close();
            } catch (IOException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
        }

        try {
            FileInputStream inputStream = new FileInputStream(file);
            bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            //Log.e("mytag", new String(bytes));
        } catch (IOException e) {
            Log.e("mytag", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }

        return bytes;
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = "lskfjkdljfsalkdjfkds".getBytes(StandardCharsets.UTF_8);
        byte[] slice = Arrays.copyOfRange(iv, 0, 16);

        return new IvParameterSpec(slice);
    }
}


