package com.stdio.warehousecontrol;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerTouchListener.RecyclerTouchListenerHelper{

    private OnActivityTouchListener touchListener;
    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;
    private RecyclerTouchListener onTouchListener;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    public static List<DataModel> list;
    public static ArrayList<String> keysList = new ArrayList<>();

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

        getData();
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        })).setSwipeOptionViews(R.id.edit, R.id.delete).setSwipeable(R.id.rowFG, R.id.rowBG, (new RecyclerTouchListener.OnSwipeOptionsClickListener() {
            public void onSwipeOptionClicked(int viewID, int position) {
                if (viewID == R.id.edit) {
                    ChangeItemActivity.key = keysList.get(position);
                    ChangeItemActivity.article = list.get(position).article;
                    ChangeItemActivity.barcode = list.get(position).barcode;
                    ChangeItemActivity.name = list.get(position).name;
                    ChangeItemActivity.count = list.get(position).count;
                    ChangeItemActivity.number = list.get(position).address;
                    startActivity(new Intent(MainActivity.this, ChangeItemActivity.class));
                } else if (viewID == R.id.delete) {
                    deleteItem(MainActivity.this, position);
                }
            }
        }));
        mRecyclerView.addOnItemTouchListener(onTouchListener);
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
                        myRef.child(keysList.get(position)).removeValue();
                        list.remove(position);
                        keysList.remove(position);
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
            case 1:
                final String[] getInfoMods ={"Сканирование (штрих-код)", "Ручной ввод (артикул, штрих-код, имя)"};

                builder.setTitle("Выберите способ ввода штрих-кода"); // заголовок для диалога

                builder.setItems(getInfoMods, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (getInfoMods[item].equals("Сканирование (штрих-код)")) {
                            startActivity(new Intent(MainActivity.this, ScannerForGetInformationActivity.class));
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
            recreate();
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
                keysList.add(dataSnapshot.getKey());
                list.add(item);
                setRecyclerViewAdapter();
                mAdapter.notifyDataSetChanged();
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
        mAdapter = new MainAdapter(this, list);
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
        showDialog(1);
    }
}