package com.example.cadprodutos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cadprodutos.filehelpers.FileHelper;
import com.example.cadprodutos.filehelpers.Repository;
import com.example.cadprodutos.network.SendFileTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> listItems = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private static final int PERMISSION_REQUEST_STORAGE = 1000;

    private static final IvParameterSpec ivSpec = FileHelper.generateIv();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);

        adapter.add("STARTING...");

        //request file access permissions
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, PERMISSION_REQUEST_STORAGE);
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            if (!Environment.isExternalStorageManager()) {
                //request for the permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }

        //read the parameters to run the tests
        //adapter.add("Reading parameters");
        //Map<String, String> parameters = FileHelper.readParameters();
        //adapter.add("Parameters loaded");

        //if function is makeFile NESTE MOMENTO USAR O DASHBOARD, WHATNOW

        Repository repository = new Repository();
        String res = null;
        try {
            res = repository.doWhatNowAsync().get();
            //Log.e("@@@", res.toString());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        String function = FileHelper.paramNameLocalLoginFileEnc;
        int timesRun = 3;
        String fileName = FileHelper.defaultFileName;
        String email = FileHelper.defaultEmail;
        String pass = FileHelper.defaultPass;
        if (res != null) {
            String[] dashParameters = res.split("-");
            Log.i("tag", Arrays.toString(dashParameters));
            function = (dashParameters[0] != null) ? dashParameters[0] : FileHelper.paramNameMakeFile;
            timesRun = (dashParameters[1] != null) ? Integer.parseInt(dashParameters[1]) : 1;
            if(Objects.equals(function, FileHelper.paramNameMakeFile) || Objects.equals(function, FileHelper.paramNameMakeEncFile) ||
                    Objects.equals(function, FileHelper.paramHttpSave) || Objects.equals(function, FileHelper.paramHttpsSave)){
                fileName = (dashParameters[2] != null) ? dashParameters[2] : FileHelper.defaultFileName;
            }else{
                email = (dashParameters[2] != null) ? dashParameters[2] : FileHelper.defaultEmail;
                pass = (dashParameters[3] != null) ? dashParameters[3] : FileHelper.defaultPass;
            }
        }

        if (Objects.equals(function, FileHelper.paramNameMakeFile)) {
            makeFileFunctionNotEncrypted(fileName, timesRun);
        } else if (Objects.equals(function, FileHelper.paramNameMakeEncFile)) {
            makeFileFunctionEncrypted(fileName, timesRun);
        } else if (Objects.equals(function, FileHelper.paramNameLocalLoginFileNotEnc)) {
            localLoginFileNotEncrypted(email, pass, timesRun);
        } else if (Objects.equals(function, FileHelper.paramNameLocalLoginFileEnc)) {
            localLoginFileEncrypted(email, pass, timesRun);
        } else if (Objects.equals(function, FileHelper.paramNameLocalLoginSharedPref)) {
            localLoginSharedPreferencesNotEncrypted(email, pass, timesRun);
        } else if (Objects.equals(function, FileHelper.paramNameLocalLoginEncSharedPref)) {
            localLoginSharedPreferencesEncrypted(email, pass, timesRun);
        } else if (Objects.equals(function, FileHelper.paramHttpSave)) {
            httpSave("/sdcard/Download/file10mb.txt", timesRun);
        } else if (Objects.equals(function, FileHelper.paramHttpsSave)) {
            httpsSave("/sdcard/Download/file10mb.txt", timesRun);
        }

        repository.doLogDataAsync();
        repository.doneAsync();
    }

    private void httpSave(String fileName, int timesRun){

        for(int i = 0; i<timesRun;i++){
            SendFileTask sft = new SendFileTask(fileName,"http://10.3.2.129:80/upload");
            sft.execute();
            try {
                sft.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    private void httpsSave(String fileName, int timesRun){

        for(int i = 0; i<timesRun;i++){
            SendFileTask sft = new SendFileTask(fileName,"https://10.3.2.129:80/upload");
            sft.execute();
            try {
                sft.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    private void makeFileFunctionNotEncrypted(String fileName, int timesRun) {
        adapter.add("FUNCTION: make file selected");

        //read the file base
        adapter.add("Reading file...");
        byte[] fileBytes = FileHelper.readFileByBytes(fileName);

        if (fileBytes != null && fileBytes.length > 0) {
            for (int i = 1; i <= timesRun; i++) {
                adapter.add("Making copy " + i);
                FileHelper.createFiles(FileHelper.getFileName(fileName, i), fileBytes);
            }
            adapter.add("Test finished!");
        } else {
            adapter.add("FILE NOT FOUND!");
        }
    }

    private void makeFileFunctionEncrypted(String fileName, int timesRun) {
        // SAVE ENCRYPTED FILE
        adapter.add("FUNCTION: make encrypted file selected");

        //read the file base
        adapter.add("Reading file...");
        byte[] fileBytes = FileHelper.readFileByBytes(fileName);

        // encrypt file
        String SECRET_KEY = "aesEncryptionKey";
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");

        byte[] encrypted = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            encrypted = cipher.doFinal(fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (encrypted != null && encrypted.length > 0) {
            for (int i = 1; i <= timesRun; i++) {
                adapter.add("Making copy " + i);
                FileHelper.createFiles(FileHelper.getFileName(fileName, i), encrypted);
            }

            adapter.add("Test finished!");
        } else {
            adapter.add("FILE NOT FOUND!");
        }

    }

    private void localLoginFileNotEncrypted(String email, String pass, int timesRun) {
        adapter.add("FUNCTION: local login selected");

        adapter.add("Checking credentials");
        for (int i = 1; i <= timesRun; i++) {
            List<String> savedCredentials = FileHelper.readCredentialsNotEnc();
            //Log.i("mytag", savedCredentials.toString());
           //; Log.i("mytag",  email);
            //Log.i("mytag",  pass);
            if (Objects.equals(savedCredentials.get(0), email)
                    && Objects.equals(savedCredentials.get(1), pass)) {
                //Log.e("mytag", "sucesso");
                //adapter.add("Login " + i + " sucessful");
            } else {
                //Log.e("mytag", "faliu");
                //adapter.add("Login " + i + " failed");
            }
        }

        adapter.add("Test finished...");
    }

    private void localLoginFileEncrypted(String email, String pass, int timesRun) {
        adapter.add("FUNCTION: local login encrypted selected");

        adapter.add("Checking credentials");
        for (int i = 1; i <= timesRun; i++) {
            List<String> savedCredentials = FileHelper.readCredentialsEnc(this, ivSpec);
            //Log.i("mytag", savedCredentials.toString());
             //Log.i("mytag",  email);
            //Log.i("mytag",  pass);
            if (Objects.equals(savedCredentials.get(0), email)
                    && Objects.equals(savedCredentials.get(1), pass)) {
                //adapter.add("Login " + i + " sucessful");
                //Log.e("mytag", "sucessful");
            } else {
                //Log.e("mytag", "failed");
                //adapter.add("Login " + i + " failed");
            }
        }
        adapter.add("Test finished...");
    }

    private void localLoginSharedPreferencesNotEncrypted(String email, String pass, int timesRun) {
        adapter.add("FUNCTION: local login prefs selected");

        adapter.add("Checking credentials");
        for (int i = 1; i <= timesRun; i++) {
            Map<String, String> savedCredentials = FileHelper.getCredentialsFromPreferences(this);

            if (Objects.equals(savedCredentials.get(FileHelper.email), email)
                    && Objects.equals(savedCredentials.get(FileHelper.pass), pass)) {
                adapter.add("Login " + i + " sucessful");
            } else {
                adapter.add("Login " + i + " failed");
            }
        }

        adapter.add("Test finished...");
    }

    private void localLoginSharedPreferencesEncrypted(String email, String pass, int timesRun) {
        adapter.add("FUNCTION: local login prefs encrypted selected");

        adapter.add("Checking credentials");
        for (int i = 1; i <= timesRun; i++) {
            Map<String, String> savedCredentials = FileHelper.getCredentialsFromPreferencesEnc(this, ivSpec);

            if (Objects.equals(savedCredentials.get(FileHelper.emailEnc), email) &&
                    Objects.equals(savedCredentials.get(FileHelper.passEnc), pass)) {
                //adapter.add("Login " + i + " sucessful");
            } else {
                //+adapter.add("Login " + i + " failed");
            }
        }

        adapter.add("Test finished...");
    }

    private void sentToCloudFunction(String fileName, int timesRun, Repository repository) {
        adapter.add("FUNCTION: send to cloud selected");

        //read the file base
        adapter.add("Reading file...");
        byte[] fileBytes = FileHelper.readFileByBytes(fileName);

        if (fileBytes != null && fileBytes.length > 0) {
            //get times to run value
            //int timesToRun = Integer.parseInt(Objects.requireNonNull(parameters.get(FileHelper.timesToRun)));
            //create the files
            String[] fileToReadName = Objects.requireNonNull(fileName).split("\\.");
            sendToCloud(fileToReadName, fileBytes, timesRun, 1);

            //Neste ponto fazer a medição | LOGDATE
            // Fazer o DONE
            repository.doLogDataAsync();
            repository.doneAsync();
        } else {
            adapter.add("FILE NOT FOUND!");
        }
    }

    /*
     * Sends the file to firebase storage*/
    private void sendToCloud(String[] fileNameParts, byte[] fileBytes, int timesToRun, int runningTime) {
        String fileName = fileNameParts[0] + "-" + runningTime + "." + fileNameParts[1];
        adapter.add("Sending file " + runningTime);
        final int _runTime = runningTime;

        FirebaseStorage.getInstance().getReference(fileName).putBytes(fileBytes).addOnSuccessListener(taskSnapshot -> {
            adapter.add("Sent file " + fileName);
            if (timesToRun >= _runTime + 1) {
                sendToCloud(fileNameParts, fileBytes, timesToRun, _runTime + 1);
            } else {
                adapter.add("Test finished!");
                adapter.add("Test finished!");
            }
        });

    }
}