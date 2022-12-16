package com.example.cadprodutos.filehelpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.format.Time;
import android.util.Base64;
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
    public static String emailEnc = "emailEnc";
    public static String pass = "pass";
    public static String passEnc = "passEnc";

    public static String defaultEmail = "test1@test.com";
    public static String defaultPass = "password@67";
    public static String defaultFileName = "somefile.txt";


    public static String paramNameMakeFile = "makeFile";
    public static String paramNameMakeEncFile = "makeEncFile";
    public static String paramNameSaveCloud = "saveCloud";
    public static String paramNameLocalLoginFileNotEnc = "localLogin";
    public static String paramNameLocalLoginFileEnc = "localLoginEnc";
    public static String paramNameLocalLoginSharedPref = "localLoginSharedPref";
    public static String paramNameLocalLoginEncSharedPref = "localLoginEncSharedPref";


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

    public static String getFileName(String fileName, int runTime) {
        String[] fileToReadName = Objects.requireNonNull(fileName).split("\\.");
        Time today = new Time();
        today.setToNow();
        return fileToReadName[0] + "-" + today.toMillis(false) + runTime + "." + fileToReadName[1];
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

    private static SharedPreferences getSPEditor(Context context) {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    public static Map<String, String> getCredentialsFromPreferences(Context context) {
        Map<String, String> credentials = new HashMap<>();

        SharedPreferences sp = getSPEditor(context);

        //email
        String emailSaved = sp.getString(email, null);
        //is the first run
        if (emailSaved == null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(email, defaultEmail);
            editor.putString(pass, defaultPass);
            editor.commit();
        }

        emailSaved = sp.getString(email, null);
        String passSaved = sp.getString(pass, null);

        credentials.put(email, emailSaved);
        credentials.put(pass, passSaved);

        return credentials;
    }

    public static Map<String, String> getCredentialsFromPreferencesEnc(Context context, IvParameterSpec ivSpec) {
        Map<String, String> credentials = new HashMap<>();

        SharedPreferences sp = getSPEditor(context);

        //email
        String emailSaved = sp.getString(emailEnc, null);
        //is the first run
        if (emailSaved == null) {
            try {
                String emailToEncript = defaultEmail;
                String passToEncript = defaultPass;

                String emailEncrypted = encrypt(context, emailToEncript, ivSpec);
                String passEncrypted = encrypt(context, passToEncript, ivSpec);

                SharedPreferences.Editor editor = sp.edit();
                editor.putString(emailEnc, emailEncrypted);
                editor.putString(passEnc, passEncrypted);
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String passSaved = null;
        try {
            emailSaved = decrypt(context, sp.getString(emailEnc, null), ivSpec);
            passSaved = decrypt(context, sp.getString(passEnc, null), ivSpec);

            Log.e("mytag", emailSaved);
        } catch (Exception e) {
            e.printStackTrace();
        }
        credentials.put(emailEnc, emailSaved);
        credentials.put(passEnc, passSaved);

        return credentials;
    }

    public static List<String> readCredentialsNotEnc() {
        List<String> credentials = new ArrayList<>();

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "no-encripted-credentials.txt");

        //se o arquivo não existir cria com os parâmetros iniciais
        if (!file.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                String fileStartContent = defaultEmail + "\n" + defaultPass;
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
                FileOutputStream fos = new FileOutputStream(file);
                String fileStartContent = defaultEmail + defaultPass;

                byte[] fileBytes = fileStartContent.getBytes(StandardCharsets.UTF_8);

                SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
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


    private static String encrypt(Context context, String value, IvParameterSpec ivParameterSpec) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

        byte[] encrypted = cipher.doFinal(value.getBytes());

        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    private static String decrypt(Context context, String value, IvParameterSpec ivParameterSpec) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");

        // get iv from shared preferences
        SharedPreferences prefs = context.getSharedPreferences("com.example.encryptednotes", Context.MODE_PRIVATE);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);

        byte[] original = cipher.doFinal(Base64.decode(value, Base64.DEFAULT));

        return new String(original);
    }


}


