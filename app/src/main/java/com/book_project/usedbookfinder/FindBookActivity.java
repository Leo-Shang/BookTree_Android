package com.book_project.usedbookfinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FindBookActivity extends Activity {
    private FirebaseFirestore db = null;
    private String TAG = "TAG";
    private EditText bookName;
    private Spinner searchBy;
    private FirebaseStorage storage;
    private ArrayList<Book> bookList;
    private BookListAdapter adapter = null;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_book);
        bookName = (EditText) findViewById(R.id.bookName_textfield);
        db = FirebaseFirestore.getInstance();
        searchBy = (Spinner) findViewById(R.id.searchBy);
        storage = FirebaseStorage.getInstance();
        bookList = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.listView);


        populateRectentAddedList();
        setupSpinner();
        setupNav();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book clicked = bookList.get(position);
                Intent i = new Intent(FindBookActivity.this, ResultDetailActivity.class);
                i.putExtra("bookName", clicked.getBook_name());
                i.putExtra("bookAuthor", clicked.getAuthor());
                i.putExtra("bookPrice", clicked.getPrice());
                i.putExtra("bookEdition", clicked.getEdition());
                i.putExtra("bookID", clicked.getBk_id());
                i.putExtra("bookDescrip", clicked.getBook_descrip());

                i.putExtra("bookCreateDate", clicked.getCreate_date().getTime());
                System.out.println(clicked.getCreate_date());

                i.putExtra("bookCourse", clicked.getCourse_num());
                i.putExtra("bookSchool", clicked.getSchool());
                i.putExtra("bookSeller", clicked.getSeller());
                i.putExtra("bookStatus", clicked.getStatus());
                i.putExtra("username", clicked.getUsername());
                i.putExtra("email", clicked.getEmail());
                i.putExtra("extraContact", clicked.getExtraContact());
                i.putExtra("imageURL", clicked.getImageURL());

                startActivity(i);
            }
        });
    }

    private void setupNav() {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.findBook:
                        Intent findbook = new Intent(FindBookActivity.this, FindBookActivity.class);
                        startActivity(findbook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.sellBook:
                        Intent sellBook = new Intent(FindBookActivity.this, SellBookActivity.class);
                        startActivity(sellBook);
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                    case R.id.myBuyers:
                        Intent myBuyers = new Intent(FindBookActivity.this, MyBuyersActivity.class);
                        startActivity(myBuyers);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myAccount:
                        Intent myAccount = new Intent(FindBookActivity.this, MyAccountActivity.class);
                        startActivity(myAccount);
                        overridePendingTransition(0, 0);
                        break;
                }
                return false;
            }
        });
    }


    private void setupSpinner() {
        List<String> categories = new ArrayList<String>();
        categories.add(" ");
        categories.add("Book Name");
        categories.add("School & Course");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item, categories){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View v = null;

                // If this is the initial dummy entry, make it hidden
                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                } else {
                    v = super.getDropDownView(position, null, parent);
                    ((TextView) v).setGravity(Gravity.CENTER);
                }
                parent.setVerticalScrollBarEnabled(false);
                return v;
            }
        };

        //dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchBy.setAdapter(dataAdapter);

        AdapterView.OnItemSelectedListener searchOptionSelectedListener = new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> spinner, View container, int position, long id) {
                if(position == 2) {
                    Intent i = new Intent(FindBookActivity.this, FindBookBySchoolMajorActivity.class);
                    startActivity(i);
                    overridePendingTransition(0, 0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };
        searchBy.setOnItemSelectedListener(searchOptionSelectedListener);
    }

    public void clickSellBookButton(View view){
        Log.i(TAG, "clicked Sell Book Button");
        startActivity(new Intent(FindBookActivity.this, SellBookActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickMyAccountButton(View view) {
        Log.i(TAG, "clicked my account button");
        startActivity(new Intent(FindBookActivity.this, MyAccountActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickOnMyBuyers(View view){
        Log.i(TAG, "clicked on my buyers button");
        startActivity(new Intent(FindBookActivity.this, MyBuyersActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickFindBookButton(View view){
        Log.i(TAG, "clicked find book button");
        String bookName_str = bookName.getText().toString();
        if(!TextUtils.isEmpty(bookName_str)){
            Bundle b = new Bundle();
            b.putStringArray("Array", new String[]{bookName_str});
            Intent i=new Intent(FindBookActivity.this, FindBookResultActivity.class);
            i.putExtras(b);
            startActivity(i);
        }
        else if(TextUtils.isEmpty(bookName_str)){
            Toast.makeText(getApplicationContext(), "Please Enter Book Name", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateRectentAddedList() {
        db.collection("RecentlyAdded").document("Books")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot document) {
                        Object recentTitles = document.get("RecentTitles");
                        if (recentTitles != null) {
                            ArrayList<String> titles = (ArrayList) recentTitles;
                            int counter = 0;
                            for (int i = titles.size() - 1; i >= 0 && counter < 10; i--) {
                                String[] segments = titles.get(i).split("_");
                                String school = segments[0];
                                String major = segments[1];
                                String bookID = segments[2];
                                String status = segments[3];
                                if (status.equals("A")) {
                                    counter += 1;
                                    db.collection("Books").document(school).collection(major).document(bookID)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                                    final String downloadURL = documentSnapshot.getString("Image");
                                                    if (downloadURL != null) {
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
                                                                            Book temp_book = new Book(documentSnapshot.getString("Book_Name"), documentSnapshot.getString("Edition"), "CAD $" + documentSnapshot.getString("Price"), bitmap[0],
                                                                                    documentSnapshot.getString("School"), documentSnapshot.getString("Course_Number"), documentSnapshot.getString("Book_ID"), documentSnapshot.getString("Author"),
                                                                                    documentSnapshot.getString("Book_Description"), documentSnapshot.getDate("Create_Date"), documentSnapshot.getString("Seller"), "A", documentSnapshot.getString("Username"),
                                                                                    documentSnapshot.getString("Email"), documentSnapshot.getString("ExtraContact"), downloadURL, documentSnapshot.getString("BuyerUID"));
                                                                            bookList.add(temp_book);

                                                                            //TODO:
                                                                            // Temp workaround
                                                                            adapter = new BookListAdapter(getApplicationContext(), R.layout.adapter_view_layout, bookList);
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
                                                    } else {
                                                        Book temp_book = new Book(documentSnapshot.getString("Book_Name"), documentSnapshot.getString("Edition"), "CAD $" + documentSnapshot.getString("Price"), null,
                                                                documentSnapshot.getString("School"), documentSnapshot.getString("Course_Number"), documentSnapshot.getString("Book_ID"), documentSnapshot.getString("Author"),
                                                                documentSnapshot.getString("Book_Description"), documentSnapshot.getDate("Create_Date"), documentSnapshot.getString("Seller"), "A", documentSnapshot.getString("Username"),
                                                                documentSnapshot.getString("Email"), documentSnapshot.getString("ExtraContact"), downloadURL, documentSnapshot.getString("BuyerUID"));
                                                        bookList.add(temp_book);
                                                    }
                                                    // TODO:
                                                    // Temp workaround
                                                    adapter = new BookListAdapter(getApplicationContext(), R.layout.adapter_view_layout, bookList);
                                                    mListView.setAdapter(adapter);
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }

}
