package com.stdio.warehousecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeItemActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    public static String article, barcode, name, count, size;
    EditText etArticle, etBarcode, etName, etCount, etSize;
    public static boolean itemIsChanged = false;
    public static String key = "";
    String firstBarcode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_item);
        initViews();

        database= FirebaseDatabase.getInstance();
        myRef = database.getReference("items");
        firstBarcode = barcode;
    }

    public void pushOrder(View view) {
        getData();
        changeValue();
    }

    private void initViews() {
        etArticle = findViewById(R.id.etArticle);
        etBarcode = findViewById(R.id.etBarcode);
        etName = findViewById(R.id.etName);
        etCount = findViewById(R.id.etCount);
        etSize = findViewById(R.id.etSize);

        setCurrentValues();
    }

    private void getData() {
        article = etArticle.getText().toString();
        barcode = etBarcode.getText().toString();
        name = etName.getText().toString();
        count = etCount.getText().toString();
        size = etSize.getText().toString();
    }

    private void changeValue() {
        boolean barcodeIsAlreadyExist = false;
        for (DataModel dataModel : MainActivity.list) {
            if (dataModel.barcode.equals(barcode)) {
                barcodeIsAlreadyExist = true;
            }
        }
        if (!barcodeIsAlreadyExist || firstBarcode.equals(barcode)) {
            DataModel item = new DataModel(article, barcode, name, count, size, key);
            myRef.child(key).setValue(item);
            itemIsChanged = true;
            finish();
        }
        else {
            Toast.makeText(this, "Указанный штрих-код " + barcode + " уже есть в базе", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCurrentValues() {
        etArticle.setText(article);
        etBarcode.setText(barcode);
        etName.setText(name);
        etCount.setText(String.valueOf(count));
        etSize.setText(String.valueOf(size));
    }
}
