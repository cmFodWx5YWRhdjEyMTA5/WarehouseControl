package com.stdio.warehousecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddItemActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    String article, name, count, size;
    public static String barcode = "";
    EditText etArticle, etBarcode, etName, etCount, etSize;
    public static boolean itemIsAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        initViews();

        database= FirebaseDatabase.getInstance();
        myRef = database.getReference("items");
    }

    public void pushOrder(View view) {
        getData();
        pushToDatabase();
    }

    private void initViews() {
        etArticle = findViewById(R.id.etArticle);
        etBarcode = findViewById(R.id.etBarcode);
        etName = findViewById(R.id.etName);
        etCount = findViewById(R.id.etCount);
        etSize = findViewById(R.id.etSize);

        if (barcode != null) {
            etBarcode.setText(barcode);
        }
    }

    private void getData() {
        article = etArticle.getText().toString();
        barcode = etBarcode.getText().toString();
        name = etName.getText().toString();
        count = etCount.getText().toString();
        size = etSize.getText().toString();
    }

    private void pushToDatabase() {
        boolean barcodeIsAlreadyExist = false;
        for (DataModel dataModel : MainActivity.list) {
            if (dataModel.barcode.equals(barcode)) {
                barcodeIsAlreadyExist = true;
            }
        }
        if (!barcodeIsAlreadyExist) {
            DataModel item = new DataModel(article, barcode, name, count, size);
            myRef.push().setValue(item);
            itemIsAdded = true;
            barcode = "";
            finish();
        }
        else {
            Toast.makeText(this, "Указанный штрих-код " + barcode + " уже есть в базе", Toast.LENGTH_SHORT).show();
        }
    }
}
