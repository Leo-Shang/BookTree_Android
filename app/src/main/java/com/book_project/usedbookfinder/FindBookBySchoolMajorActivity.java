package com.book_project.usedbookfinder;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class FindBookBySchoolMajorActivity extends Activity {
    private FirebaseFirestore db = null;
    private FirebaseStorage storage;
    private ArrayList<Book> bookList;
    private BookListAdapter adapter = null;
    private ListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_book_sch_major);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mListView = (ListView) findViewById(R.id.listView);
        bookList = new ArrayList<>();
        populateRectentAddedList();

        setupSpinner();
        setupSchoolSpinner();
        setupNav();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book clicked = bookList.get(position);
                Intent i = new Intent(FindBookBySchoolMajorActivity.this, ResultDetailActivity.class);
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
                        Intent findbook = new Intent(FindBookBySchoolMajorActivity.this, FindBookActivity.class);
                        startActivity(findbook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.sellBook:
                        Intent sellBook = new Intent(FindBookBySchoolMajorActivity.this, SellBookActivity.class);
                        startActivity(sellBook);
                        overridePendingTransition(0, 0);
                        finish();
                        break;
                    case R.id.myBuyers:
                        Intent myBuyers = new Intent(FindBookBySchoolMajorActivity.this, MyBuyersActivity.class);
                        startActivity(myBuyers);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myAccount:
                        Intent myAccount = new Intent(FindBookBySchoolMajorActivity.this, MyAccountActivity.class);
                        startActivity(myAccount);
                        overridePendingTransition(0, 0);
                        break;
                }
                return false;
            }
        });
    }

    private void setupSchoolSpinner() {
        Spinner school = (Spinner) findViewById(R.id.school);
        List<String> categories = new ArrayList<String>();
        categories.add("SFU");
        categories.add("UBC");
        categories.add("BCIT");
        categories.add("KPU");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item, categories);
        //dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        school.setAdapter(dataAdapter);
    }

    private void setupSpinner() {
        Spinner searchBy = (Spinner) findViewById(R.id.searchBy);
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
        searchBy.setSelection(2);

        AdapterView.OnItemSelectedListener searchOptionSelectedListener = new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> spinner, View container, int position, long id) {
                if(position == 1) {
                    startActivity(new Intent(FindBookBySchoolMajorActivity.this, FindBookActivity.class));
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
        startActivity(new Intent(FindBookBySchoolMajorActivity.this, SellBookActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickMyAccountButton(View view) {
        startActivity(new Intent(FindBookBySchoolMajorActivity.this, MyAccountActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickOnMyBuyers(View view){
        startActivity(new Intent(FindBookBySchoolMajorActivity.this, MyBuyersActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickFindBookButton(View view){
        Spinner school_spinner = (Spinner) findViewById(R.id.school);

        TextView cour = (TextView) findViewById(R.id.search_book_major);
        String school = school_spinner.getSelectedItem().toString();
        String course = cour.getText().toString().replaceAll(" ", "").toUpperCase();
        if(!TextUtils.isEmpty(course)) {
            Intent i = new Intent(FindBookBySchoolMajorActivity.this, FindBookResultSchMajorActivity.class);
            i.putExtra("School", school);
            i.putExtra("Course", course);
            i.putExtra("Position", String.valueOf(school_spinner.getSelectedItemPosition()));
            startActivity(i);
            overridePendingTransition(0, 0);
        } else {
            Toast.makeText(getApplicationContext(), "Please Fill In Search Field", Toast.LENGTH_SHORT).show();
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
