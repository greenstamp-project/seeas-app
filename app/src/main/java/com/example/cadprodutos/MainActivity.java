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
import com.google.firebase.storage.FirebaseStorage;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//import network.Repository;

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
        listView = (ListView) findViewById(R.id.list_view);
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

        String function = FileHelper.paramNameLocalLoginSharedPref;
        int timesRun = 20;
        String fileName = "somefile.txt";
        String email = "test1@test.com";
        String pass = "pass1";
        if (res != null) {
            String[] dashParameters = res.split("-");
            function = dashParameters[0];
            timesRun = Integer.parseInt(dashParameters[1]);
            fileName = dashParameters[2];
        }

        if (Objects.equals(function, FileHelper.paramNameMakeFile)) {
            adapter.add("FUNCTION: make file selected");

            //read the file base
            adapter.add("Reading file...");
            byte[] fileBytes = FileHelper.readFileByBytes(fileName);

            if (fileBytes != null && fileBytes.length > 0) {
                //get times to run value
                //create the files
                String[] fileToReadName = Objects.requireNonNull(fileName).split("\\.");
                for (int i = 1; i <= timesRun; i++) {
                    adapter.add("Making copy " + i);
                    String _fileName = fileToReadName[0] + "-" + i + "." + fileToReadName[1];
                    FileHelper.createFiles(_fileName, fileBytes);
                }


                //Neste ponto fazer a medição | LOGDATE
                // Fazer o DONE

                repository.doLogDataAsync();
                repository.doneAsync();


                adapter.add("Test finished!");
            } else {
                adapter.add("FILE NOT FOUND!");
            }
        } else if (Objects.equals(function, FileHelper.paramNameMakeEncFile)) {
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
                //byte[] iv = cipher.getIV();
                encrypted = cipher.doFinal(fileBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (encrypted != null && encrypted.length > 0) {
                //get times to run value
                //create the files
                String[] fileToReadName = Objects.requireNonNull(fileName).split("\\.");
                for (int i = 1; i <= timesRun; i++) {
                    adapter.add("Making copy " + i);
                    String _fileName = fileToReadName[0] + "-" + i + "." + fileToReadName[1];
                    FileHelper.createFiles(_fileName, encrypted);
                }

                repository.doLogDataAsync();
                repository.doneAsync();

                adapter.add("Test finished!");
            } else {
                adapter.add("FILE NOT FOUND!");
            }

        } else if (Objects.equals(function, FileHelper.paramNameSaveCloud)) {
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
        } else if (Objects.equals(function, FileHelper.paramNameLocalLogin)) {
            adapter.add("FUNCTION: local login selected");

            adapter.add("Checking credentials");
            for (int i = 1; i <= timesRun; i++) {
                List<String> savedCredentials = FileHelper.readCredentialsNotEnc();
                if (Objects.equals(savedCredentials.get(0), email)
                        && Objects.equals(savedCredentials.get(1), pass)) {
                    adapter.add("Login " + i + " sucessful");
                } else {
                    adapter.add("Login " + i + " failed");
                }
            }

            repository.doLogDataAsync();
            repository.doneAsync();

            adapter.add("Test finished...");
        } else if (Objects.equals(function, FileHelper.paramNameLocalLoginEnc)) {
            adapter.add("FUNCTION: local login encrypted selected");

            adapter.add("Checking credentials");
            for (int i = 1; i <= timesRun; i++) {
                byte[] savedCredentials = FileHelper.readCredentialsEnc(ivSpec);
                byte[] credentialsBytes = (email + pass).getBytes();

                byte[] encrypted = null;
                // encrypt file
                SecretKeySpec keySpec = new SecretKeySpec(FileHelper.SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
                try {
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
                    encrypted = cipher.doFinal(credentialsBytes);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }

                //Log.i("mytag", new String(encrypted));

                repository.doLogDataAsync();
                repository.doneAsync();

                if (Arrays.equals(encrypted, savedCredentials)) {
                    adapter.add("Login " + i + " sucessful");
                } else {
                    adapter.add("Login " + i + " failed");
                }
            }
            adapter.add("Test finished...");

            repository.doLogDataAsync();
            repository.doneAsync();
        } else if (Objects.equals(function, FileHelper.paramNameLocalLoginSharedPref)) {
            adapter.add("FUNCTION: local login prefs selected");

            adapter.add("Checking credentials");
            for (int i = 1; i <= timesRun; i++) {
                Map<String, String> savedCredentials = FileHelper.getCredentialsFromPreferences(this);

                Log.e("mytag", savedCredentials.get(FileHelper.email));
                Log.i("mytag", email);

                if (Objects.equals(savedCredentials.get(FileHelper.email), email)
                        && Objects.equals(savedCredentials.get(FileHelper.pass), pass)) {
                    adapter.add("Login " + i + " sucessful");
                } else {
                    adapter.add("Login " + i + " failed");
                }
            }

            repository.doLogDataAsync();
            repository.doneAsync();

            adapter.add("Test finished...");
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
            }
        });

    }
}