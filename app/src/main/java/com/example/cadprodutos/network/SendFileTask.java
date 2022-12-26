package com.example.cadprodutos.network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class SendFileTask extends AsyncTask<Void, Void, Void> {

    String fileName;
    String address;

    public SendFileTask(String fileName, String address) {
        this.fileName = fileName;
        this.address = address;
    }

    @Override
    protected Void doInBackground(Void... voids) {
       // private void sendToServer(String address, String filepath) {
        Log.e("@@@", String.valueOf("StartingSending..."));
            try {
                // Create a new HttpURLConnection
                URL url = new URL(address);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Generate a unique boundary based on the current time
                long time = System.currentTimeMillis();
                Random rnd = new Random();
                int rndInt = rnd.nextInt();
                String boundary = "===" + time + "." + rndInt + "===";

                // Set the request method to POST and specify the request body
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                // TODO: add path to a file
                File file = new File(fileName);

                // Write the file data to the request body
                OutputStream os = connection.getOutputStream();
                InputStream is = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                is.close();

                // Get the response code and read the response body
                int responseCode = connection.getResponseCode();
                //if(responseCode==200){
                Log.e("###", String.valueOf(responseCode));
                //}
            } catch (IOException e) {
                e.printStackTrace();
            }
        Log.e("@@@", String.valueOf("FinishSending..."));

        return null;

    }
}
