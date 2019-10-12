package com.book_project.usedbookfinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MyBuyersActivity extends Activity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String uid;
    private ArrayList<Book> bookList;
    private BookListWithTransButtonAdapter adapter = null;
    ListView mListView;
    String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_buyers);
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        bookList = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.listView);
        user = auth.getCurrentUser();
        setupNav();

        // Red Dot
        db.collection("CountOnBuyers").document(user.getUid())
                .update("CountsDiff" , 0);
        // End Red Dot

        if(user != null)
            uid = user.getUid();

        if(uid != null)
            PopulateMyBuyerList();
        else
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
    }

    private void setupNav() {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.findBook:
                        Intent findbook = new Intent(MyBuyersActivity.this, FindBookActivity.class);
                        startActivity(findbook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.sellBook:
                        Intent sellBook = new Intent(MyBuyersActivity.this, SellBookActivity.class);
                        startActivity(sellBook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myBuyers:
                        Intent myBuyers = new Intent(MyBuyersActivity.this, MyBuyersActivity.class);
                        startActivity(myBuyers);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myAccount:
                        Intent myAccount = new Intent(MyBuyersActivity.this, MyAccountActivity.class);
                        startActivity(myAccount);
                        overridePendingTransition(0, 0);
                        break;
                }
                return false;
            }
        });
    }

    public void clickSellBookButton(View view){
        Log.i(TAG, "clicked Sell Book Button");
        startActivity(new Intent(MyBuyersActivity.this, SellBookActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickMyAccountButton(View view) {
        Log.i(TAG, "clicked my account button");
        startActivity(new Intent(MyBuyersActivity.this, MyAccountActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickFindBookButton(View view){
        startActivity(new Intent(MyBuyersActivity.this, FindBookActivity.class));
        overridePendingTransition(0, 0);
    }

    private void PopulateMyBuyerList() {
        final Task<QuerySnapshot> task = db.collection("Users").document(uid).collection("Sell").get();
        task.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                for (final QueryDocumentSnapshot document : task.getResult()){
                    String username = document.getString("Username");
                    String email = document.getString("Email");
                    if(username != null && email != null){
                        final String downloadURL = document.getString("Image");
                        if(downloadURL != null){
                            StorageReference httpsReference = null;
                            final Bitmap[] bitmap = {null};
                                httpsReference = storage.getReferenceFromUrl(Objects.requireNonNull(downloadURL));
                                try {
                                    final File localFile = File.createTempFile("images", "jpg");
                                    httpsReference.getFile(localFile)
                                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    bitmap[0] = BitmapFactory.decodeFile(localFile.getPath());
                                                    Book temp_book = new Book(document.getString("Book_Name"), document.getString("Edition"), "CAD $" + document.getString("Book_Price"), bitmap[0],
                                                            document.getString("School"), document.getString("Major"), document.getString("Book_ID"), document.getString("Author"),
                                                            document.getString("Book_Description"), document.getDate("Create_Date"), uid.toString(), document.getString("Status"), document.getString("Username"),
                                                            document.getString("Email"), document.getString("ExtraContact"), document.getString("Image"), document.getString("BuyerUID"));
                                                    bookList.add(temp_book);

                                                    //TODO:
                                                    // Temp workaround
                                                    adapter = new BookListWithTransButtonAdapter(getApplicationContext(), R.layout.adapter_view_with_trans_buttons, bookList);
                                                    mListView.setAdapter(adapter);

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    System.out.println("EXCEPTIOIN: " + exception.toString());
                                                }
                                            });

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        } else{
                            Book temp_book = new Book(document.getString("Book_Name"), document.getString("Edition"), "CAD $" + document.getString("Book_Price"), null,
                                    document.getString("School"), document.getString("Major"), document.getString("Book_ID"), document.getString("Author"),
                                    document.getString("Book_Description"), document.getDate("Create_Date"), uid.toString(), document.getString("Status"), document.getString("Username"),
                                    document.getString("Email"), document.getString("ExtraContact"), document.getString("Image"), document.getString("BuyerUID"));
                            bookList.add(temp_book);
                        }
                        // TODO:
                        // Temp workaround
                        adapter =  new BookListWithTransButtonAdapter(getApplicationContext(), R.layout.adapter_view_with_trans_buttons, bookList);
                        mListView.setAdapter(adapter);
                        }
                    }
                }
        });
        task.addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                // handle any errors here
            }
        });
    }
}
