package com.stdio.warehousecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class InformationResultActivity extends AppCompatActivity {

    TextView tvArticle, tvBarcode, tvName, tvCount, tvAddress;
    public static String key;
    public static boolean viewed = false;
    public static String article, barcode, name, count, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_result);
        initViews();
        setTextViewValues();
    }

    private void initViews() {
        tvArticle = findViewById(R.id.tvArticle);
        tvBarcode = findViewById(R.id.tvBarcode);
        tvName = findViewById(R.id.tvName);
        tvCount = findViewById(R.id.tvCount);
        tvAddress = findViewById(R.id.tvAddress);
    }

    private void setTextViewValues() {
        tvArticle.setText("Артикул: " + article);
        tvBarcode.setText("Штрих-код: " + barcode);
        tvName.setText("Название: " + name);
        tvCount.setText("Количество: " + count);
        tvAddress.setText("Номер полки: " + address);
    }

    public void changeButton(View view) {
        ChangeItemActivity.key = key;
        ChangeItemActivity.article = article;
        ChangeItemActivity.barcode = barcode;
        ChangeItemActivity.name = name;
        ChangeItemActivity.count = count;
        ChangeItemActivity.number = address;
        startActivity(new Intent(this, ChangeItemActivity.class));
    }

    public void okButton(View view) {
        viewed = true;
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ChangeItemActivity.itemIsChanged) {
            tvArticle.setText("Артикул: " + ChangeItemActivity.article);
            tvBarcode.setText("Штрих-код: " + ChangeItemActivity.barcode);
            tvName.setText("Название: " + ChangeItemActivity.name);
            tvCount.setText("Количество: " + ChangeItemActivity.count);
            tvAddress.setText("Номер полки: " + ChangeItemActivity.number);
        }
    }
}
