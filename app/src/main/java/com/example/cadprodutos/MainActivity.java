package com.example.cadprodutos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.cadprodutos.filehelpers.FileHelpers;
import com.example.cadprodutos.model.DBHelper.ProdutosDB;
import com.example.cadprodutos.model.GuardarFicheiroLocal;
import com.example.cadprodutos.model.Produtos;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ListView lista;
    ProdutosDB bdHelper;
    ArrayList<String> listItems = new ArrayList<>();
    Produtos produto;
    ArrayAdapter<String> adapter;

    private static final int PERMISSION_REQUEST_STORAGE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lista = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, listItems);
        lista.setAdapter(adapter);

        adapter.add("starting...");

        //request file access permissions
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
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
        Map<String, String> parameters = FileHelpers.readParameters();
        adapter.add("Parameters loaded");
        //if function is makeFile
        if (Objects.equals(parameters.get(FileHelpers.functionName), FileHelpers.paramNameMakeFile)) {
            adapter.add("FUNCTION: make file selected");

            //read the file base
            adapter.add("Reading file...");
            byte[] fileBytes = FileHelpers.readFileByBytes(parameters.get(FileHelpers.fileToReadName));

            if (fileBytes != null && fileBytes.length > 0) {
                //get times to run value
                int timesToRun = Integer.parseInt(Objects.requireNonNull(parameters.get(FileHelpers.timesToRun)));
                //create the files
                String[] fileToReadName = Objects.requireNonNull(parameters.get(FileHelpers.fileToReadName)).split("\\.");
                for (int i = 0; i < timesToRun; i++) {
                    adapter.add("Making copy " + i);
                    String fileName = fileToReadName[0] + i + "." + fileToReadName[1];
                    FileHelpers.createFiles(fileName, fileBytes);
                }
                adapter.add("Test finished!");
            } else {
                adapter.add("FILE NOT FOUND!");
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuItem menuDelete = menu.add("Deletar Este Produto");
        menuDelete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                bdHelper = new ProdutosDB(MainActivity.this);
                bdHelper.deletarProduto(produto);
                bdHelper.close();

                carregarProduto();
                return true;
            }
        });
    }

    public void carregarProduto() {
        bdHelper = new ProdutosDB(MainActivity.this);
        bdHelper.close();

        if (listItems != null) {


        }
        //  finish();
    }

}