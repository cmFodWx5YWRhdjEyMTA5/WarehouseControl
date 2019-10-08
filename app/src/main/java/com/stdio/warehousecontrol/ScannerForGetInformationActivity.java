package com.stdio.warehousecontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerForGetInformationActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{


    private ZXingScannerView mScannerView;
    private static final int MY_PERMISSION_REQUEST_CAMERA = 0;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    String barcode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database= FirebaseDatabase.getInstance();
        myRef = database.getReference("items");
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            setContentView(mScannerView);                // Set the scanner view as the content view
        } else {
            requestCameraPermission();
        }
    }

    private void requestCameraPermission() {
        Toast.makeText(this, "Permission is not available. Requesting camera permission.", Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.CAMERA
        }, MY_PERMISSION_REQUEST_CAMERA);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSION_REQUEST_CAMERA) {
            return;
        }

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setContentView(mScannerView);                // Set the scanner view as the content view
        } else {
            Toast.makeText(this, "Camera permission request was denied.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (InformationResultActivity.viewed) {
            InformationResultActivity.viewed = false;
            finish();
        }
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        System.out.println("result 1 " + rawResult.getText()); // Prints scan results

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);

        if (InformationResultActivity.viewed) {
            InformationResultActivity.viewed = false;
            finish();
        }

        barcode = rawResult.getText();
        dataReceiver();
    }

    private void dataReceiver() {

        boolean barcodeIsAlreadyExist = false;
        for (int i = 0; i < MainActivity.list.size(); i++) {
            if (MainActivity.list.get(i).barcode.equals(barcode)) {
                barcodeIsAlreadyExist = true;
                InformationResultActivity.article = MainActivity.list.get(i).article;
                InformationResultActivity.barcode = MainActivity.list.get(i).barcode;
                InformationResultActivity.name = MainActivity.list.get(i).name;
                InformationResultActivity.count = MainActivity.list.get(i).count;
                InformationResultActivity.size = MainActivity.list.get(i).size;
                InformationResultActivity.key = MainActivity.listForSearching.get(i).key;
            }
        }

        if (barcodeIsAlreadyExist) {
            startActivity(new Intent(ScannerForGetInformationActivity.this, InformationResultActivity.class));
        }
        else {
            Toast.makeText(ScannerForGetInformationActivity.this, "Указанного штрих-кода " + barcode + " нет в базе", Toast.LENGTH_SHORT).show();
            mScannerView.resumeCameraPreview(ScannerForGetInformationActivity.this);
        }
    }
}