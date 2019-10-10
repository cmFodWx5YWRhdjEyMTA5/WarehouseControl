package com.stdio.warehousecontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.text.ParseException;

public class MainActivity extends AppCompatActivity implements RecyclerTouchListener.RecyclerTouchListenerHelper{

    private OnActivityTouchListener touchListener;
    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;
    private RecyclerTouchListener onTouchListener;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    public static List<DataModel> list;
    DialogProperties properties = new DialogProperties();
    FilePickerDialog dialog;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private EditText etSearch;
    public static List<DataModel> listForSearching = new ArrayList();
    public static String senderPassword;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        database= FirebaseDatabase.getInstance();
        try {
            database.setPersistenceEnabled(true);
        }
        catch (DatabaseException e) {

        }
        myRef = database.getReference("items");
        getSenderPasswordFromDatabase(database);

        getData();
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        initOnTouchListener();

        etSearch = findViewById(R.id.etSearch);
        setSearchListener();

        mRecyclerView.addOnItemTouchListener(onTouchListener);

        setFilePickerProperties();
        dialog = new FilePickerDialog(MainActivity.this,properties);
        dialog.setTitle("Выберите xlsx документ");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                boolean exception = false;
                System.out.println(files[0]);
                files[0] = files[0].replace("/mnt/sdcard/", "/storage/emulated/0/");
                FileInputStream file = null;
                XSSFWorkbook workbook = null;
                try {
                    file = new FileInputStream(files[0]);
                    // формируем из файла экземпляр HSSFWorkbook
                    workbook = new XSSFWorkbook(file);
                } catch (FileNotFoundException e) {
                    Toast.makeText(MainActivity.this, "File not found\n" + files[0], Toast.LENGTH_LONG).show();
                    exception = true;
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    exception = true;
                }
                if (!exception) {
                    checkImport(MainActivity.this, workbook);
                }

            }
        });
    }

    private void getSenderPasswordFromDatabase(FirebaseDatabase database) {
        DatabaseReference refPass = database.getReference("password");
        // Read from the database
        refPass.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                senderPassword = dataSnapshot.getValue(String.class);
                Log.d("firebasee", "Value is: " + senderPassword);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("firebasee", "Failed to read value.", error.toException());
            }
        });
    }

    private void initOnTouchListener() {
        onTouchListener = new RecyclerTouchListener(this, mRecyclerView);
        onTouchListener
                .setClickable((new RecyclerTouchListener.OnRowClickListener() {
                    @Override
                    public void onRowClicked(int position) {
                        //Toast.makeText(MainActivity.this, "Row " + (position + 1) + " clicked!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onIndependentViewClicked(int independentViewID, int position) {
                        //Toast.makeText(MainActivity.this, "Button in row " + (position + 1) + " clicked!", Toast.LENGTH_SHORT).show();
                    }
                })).setLongClickable(true, (new RecyclerTouchListener.OnRowLongClickListener() {
            public void onRowLongClicked(int position) {
                Toast.makeText(MainActivity.this, "Row " + (position + 1) + " long clicked!", Toast.LENGTH_SHORT).show();
            }
        })).setSwipeOptionViews(R.id.edit, R.id.delete, R.id.btnPlus, R.id.btnMinus).setSwipeable(R.id.rowFG, R.id.rowBG, (new RecyclerTouchListener.OnSwipeOptionsClickListener() {
            public void onSwipeOptionClicked(int viewID, int position) {
                if (viewID == R.id.edit) {
                    ChangeItemActivity.key = listForSearching.get(position).key;
                    ChangeItemActivity.article = listForSearching.get(position).article;
                    ChangeItemActivity.barcode = listForSearching.get(position).barcode;
                    ChangeItemActivity.name = listForSearching.get(position).name;
                    ChangeItemActivity.count = listForSearching.get(position).count;
                    ChangeItemActivity.size = listForSearching.get(position).size;
                    startActivity(new Intent(MainActivity.this, ChangeItemActivity.class));
                } else if (viewID == R.id.delete) {
                    deleteItem(MainActivity.this, position);
                }
                else if (viewID == R.id.btnPlus) {
                    DataModel item = new DataModel(listForSearching.get(position).article, listForSearching.get(position).barcode, listForSearching.get(position).name, String.valueOf(Integer.parseInt(listForSearching.get(position).count) + 1), listForSearching.get(position).size, listForSearching.get(position).key);
                    myRef.child(listForSearching.get(position).key).setValue(item);
                    getData();
                }
                else if (viewID == R.id.btnMinus) {
                    DataModel item = new DataModel(listForSearching.get(position).article, listForSearching.get(position).barcode, listForSearching.get(position).name, String.valueOf(Integer.parseInt(listForSearching.get(position).count) - 1), listForSearching.get(position).size, listForSearching.get(position).key);
                    myRef.child(listForSearching.get(position).key).setValue(item);
                    getData();
                }
            }
        }));
    }

    private void setSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                getDataForSearching();
            }
        });
    }

    private void getDataForSearching() {
        listForSearching.clear();
        for (int i = 0; i < list.size(); i++) {
            System.out.println("HEELP " + list.get(i).name);
            System.out.println("HEELP " + etSearch.getText().toString().isEmpty());
            if (etSearch.getText().toString().isEmpty()) {
                listForSearching.add(list.get(i));
            }
            else {
                if (list.get(i).name.toLowerCase().contains(etSearch.getText().toString().toLowerCase()) || list.get(i).article.toLowerCase().contains(etSearch.getText().toString().toLowerCase()) || list.get(i).barcode.toLowerCase().contains(etSearch.getText().toString().toLowerCase()) || list.get(i).size.toLowerCase().contains(etSearch.getText().toString().toLowerCase()) || list.get(i).count.toLowerCase().contains(etSearch.getText().toString().toLowerCase())) {
                    listForSearching.add(list.get(i));
                }
            }
        }
        setRecyclerViewAdapter();
        mAdapter.notifyDataSetChanged();
        initOnTouchListener();
    }

    private void xlsxReader(XSSFWorkbook workbook) {

        String article = null, barcode = null, name = null, count = null, size = null;

        String result = "";
        // выбираем первый лист для обработки
        // нумерация начинается с 0
        XSSFSheet sheet = workbook.getSheetAt(0);

        // получаем Iterator по всем строкам в листе
        Iterator<Row> rowIterator = sheet.iterator();

        //проходим по всему листу
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() != 0) {
                Double tmpDoubleVal = 0.0;
                long longValue = 0;
                Iterator<Cell> cells = row.iterator();
                while (cells.hasNext()) {
                    Cell cell = cells.next();
                    int cellType = cell.getCellType();
                    //перебираем возможные типы ячеек
                    switch (cellType) {
                        case Cell.CELL_TYPE_STRING:
                            result += cell.getStringCellValue() + "=";
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            tmpDoubleVal = cell.getNumericCellValue();
                            longValue = tmpDoubleVal.longValue();
                            result += longValue + "=";
                            break;

                        case Cell.CELL_TYPE_FORMULA:
                            tmpDoubleVal = cell.getNumericCellValue();
                            longValue = tmpDoubleVal.longValue();
                            result += longValue + "=";
                            break;
                        default:
                            result += cell.getStringCellValue() + "=";
                            break;
                    }
                }
                String[] subStr;
                String delimeter = "="; // Разделитель
                subStr = result.split(delimeter); // Разделения строки str с помощью метода split()
                // Вывод результата на экран
                article = subStr[0];
                barcode = subStr[1];
                name = subStr[2];
                count = subStr[3];
                size = subStr[4];
                System.out.println(result);
                DataModel item = new DataModel(article, barcode, name, count, size, "");
                myRef.push().setValue(item);
                result = "";
            }
        }
    }

    private void setFilePickerProperties() {
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;
    }

    public void checkImport(Context context, final XSSFWorkbook workbook) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

//        // set title
//        alertDialogBuilder.setTitle("Delete item");

        // set dialog message
        alertDialogBuilder
                .setMessage("Импортировать данные с файла?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        list.clear();
                        listForSearching.clear();
                        myRef.removeValue();
                        xlsxReader(workbook);
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void deleteItem(Context context, final int position) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

//        // set title
//        alertDialogBuilder.setTitle("Delete item");

        // set dialog message
        alertDialogBuilder
                .setMessage("Вы действительно хотите удалить?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        myRef.child(listForSearching.get(position).key).removeValue();
                        list.remove(listForSearching.get(position));
                        listForSearching.remove(position);
                        mAdapter.removeItem(position);
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id) {
            case 0:
                final String[] addMods ={"Сканирование", "Ручной ввод"};

                builder.setTitle("Выберите способ ввода штрих-кода"); // заголовок для диалога

                builder.setItems(addMods, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (addMods[item].equals("Сканирование")) {
                            startActivity(new Intent(MainActivity.this, ScannerActivityForAddingItem.class));
                        }
                        else if (addMods[item].equals("Ручной ввод")) {
                            startActivity(new Intent(MainActivity.this, AddItemActivity.class));
                        }
                    }
                });
                break;
        }
        return builder.create();
    }

    @Override
    public void setOnActivityTouchListener(OnActivityTouchListener listener) {
        this.touchListener = listener;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AddItemActivity.itemIsAdded || ChangeItemActivity.itemIsChanged) {
            AddItemActivity.itemIsAdded = false;
            ChangeItemActivity.itemIsChanged = false;
            getData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void getData() {
        list = new ArrayList();

        Query myQuery = myRef;
        myQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DataModel item = dataSnapshot.getValue(DataModel.class);
                list.add(new DataModel(item.article, item.barcode, item.name, item.count, item.size, dataSnapshot.getKey()));
                getDataForSearching();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setRecyclerViewAdapter() {
        mAdapter = new MainAdapter(this, listForSearching);
        mRecyclerView.setAdapter(mAdapter);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (touchListener != null) {
            touchListener.getTouchCoordinates(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void toAddItemActivity(View view) {
        showDialog(0);
    }

    public void toGetInformationFromScannerActivity(View view) {
        startActivity(new Intent(MainActivity.this, ScannerForGetInformationActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_export :
                try {
                    ExcelCreator.createExcelFile(this, MainActivity.this);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_import:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    dialog.show();
                } else {
                    requestReadPermission();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestReadPermission() {
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, REQUEST_EXTERNAL_STORAGE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(dialog!=null)
                    {   //Show dialog if the read permission has been granted.
                        dialog.show();
                    }
                }
                else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(MainActivity.this,"Permission is Required for getting list of files",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}