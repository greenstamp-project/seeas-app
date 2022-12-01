package com.example.cadprodutos;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.ListView;



import com.example.cadprodutos.filehelpers.FileHelper;
import com.example.cadprodutos.filehelpers.Repository;
import com.example.cadprodutos.filehelpers.Api;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

//import network.Repository;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> listItems = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private static final int PERMISSION_REQUEST_STORAGE = 1000;

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
        adapter.add("Reading parameters");
        Map<String, String> parameters = FileHelper.readParameters();
        adapter.add("Parameters loaded");

        //if function is makeFile NESTE MOMENTO USAR O DASHBOARD, WHATNOW
        Repository repository = new Repository();
        try {
           String res = repository.doWhatNowAsync().get();


           System.out.println("@@@@@@@@@@@@@@@@"+res);


        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (Objects.equals(parameters.get(FileHelper.functionName), FileHelper.paramNameMakeFile)) {
            adapter.add("FUNCTION: make file selected");

            //read the file base
            adapter.add("Reading file...");
            byte[] fileBytes = FileHelper.readFileByBytes(parameters.get(FileHelper.fileToReadName));

            if (fileBytes != null && fileBytes.length > 0) {
                //get times to run value
                int timesToRun = Integer.parseInt(Objects.requireNonNull(parameters.get(FileHelper.timesToRun)));
                //create the files
                String[] fileToReadName = Objects.requireNonNull(parameters.get(FileHelper.fileToReadName)).split("\\.");
                for (int i = 1; i <= timesToRun; i++) {
                    adapter.add("Making copy " + i);
                    String fileName = fileToReadName[0] + "-" + i + "." + fileToReadName[1];
                    FileHelper.createFiles(fileName, fileBytes);
                }
                //Neste ponto fazer a medição | LOGDATE
                // Fazer o DONE
                adapter.add("Test finished!");
            } else {
                adapter.add("FILE NOT FOUND!");
            }
        } else if (Objects.equals(parameters.get(FileHelper.functionName), FileHelper.paramNameSaveCloud)) {
            adapter.add("FUNCTION: send to cloud selected");

            //read the file base
            adapter.add("Reading file...");
            byte[] fileBytes = FileHelper.readFileByBytes(parameters.get(FileHelper.fileToReadName));

            if (fileBytes != null && fileBytes.length > 0) {
                //get times to run value
                int timesToRun = Integer.parseInt(Objects.requireNonNull(parameters.get(FileHelper.timesToRun)));
                //create the files
                String[] fileToReadName = Objects.requireNonNull(parameters.get(FileHelper.fileToReadName)).split("\\.");
                sendToCloud(fileToReadName, fileBytes, timesToRun, 1);
            } else {
                adapter.add("FILE NOT FOUND!");
            }
        }
    }


    /*
     * Sends the file to firebase storage*/
    private void sendToCloud(String[] fileNameParts, byte[] fileBytes, int timesToRun, int runningTime) {
        String fileName = fileNameParts[0] + "-" + runningTime + "." + fileNameParts[1];
        adapter.add("Sending file " + runningTime);
        final int _runTime = runningTime;

        FirebaseStorage.getInstance().getReference(fileName).putBytes(fileBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                adapter.add("Sent file " + fileName);
                if (timesToRun >= _runTime + 1) {
                    sendToCloud(fileNameParts, fileBytes, timesToRun, _runTime + 1);
                } else {
                    adapter.add("Test finished!");
                }
            }
        });
    }
}