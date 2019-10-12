package com.book_project.usedbookfinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
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

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FindBookResultSchMajorActivity extends Activity {
    private FirebaseFirestore db = null;
    private String TAG = "TAG";
    private String bkn = null;
    private String school = null;
    private String course_num = null;
    private ArrayList<Book> bookList = new ArrayList<>();
    private FirebaseStorage storage;
    private BookListAdapter adapter = null;
    ListView mListView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_result_sch_major);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mListView = (ListView) findViewById(R.id.listView);
        setupNav();
        setupSpinner(Integer.parseInt(getIntent().getStringExtra("Position")));


        school = getIntent().getStringExtra("School");
        course_num = getIntent().getStringExtra("Course");

        QueryOnSchoolAndMajor(school, course_num);
        setupListClickListener();

    }

    private void setupSpinner(int position) {
        Spinner spinner = (Spinner) findViewById(R.id.major_spinner);
        List<String> categories = new ArrayList<String>();
        categories.add("SFU");
        categories.add("UBC");
        categories.add("BCIT");
        categories.add("KPU");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item, categories);
        //dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(position);
    }

    private void setupNav() {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.findBook:
                        Intent findbook = new Intent(FindBookResultSchMajorActivity.this, FindBookActivity.class);
                        startActivity(findbook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.sellBook:
                        Intent sellBook = new Intent(FindBookResultSchMajorActivity.this, SellBookActivity.class);
                        startActivity(sellBook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myBuyers:
                        Intent myBuyers = new Intent(FindBookResultSchMajorActivity.this, MyBuyersActivity.class);
                        startActivity(myBuyers);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myAccount:
                        Intent myAccount = new Intent(FindBookResultSchMajorActivity.this, MyAccountActivity.class);
                        startActivity(myAccount);
                        overridePendingTransition(0, 0);
                        break;
                }
                return false;
            }
        });
    }

    public void new_search(View view){
        Spinner spinner = (Spinner) findViewById(R.id.major_spinner);
        String school = spinner.getSelectedItem().toString();
        EditText course_num = (EditText) findViewById(R.id.course_num);
        String course_number = course_num.getText().toString().replaceAll(" ", "").toUpperCase();

        if (TextUtils.isEmpty(course_number)) {
            Toast.makeText(getApplicationContext(), "Enter Course Number", Toast.LENGTH_SHORT).show();
        } else {
            bookList = new ArrayList<>();
            QueryOnSchoolAndMajor(school, course_number);
            setupListClickListener();
        }
    }

    private void QueryOnSchoolAndMajor(String school, String course_num) {
        adapter = new BookListAdapter(getApplicationContext(), R.layout.adapter_view_layout, bookList);
        mListView.setAdapter(adapter);
        course_num = course_num.toUpperCase();
        int endPoint = 0;
        for (int i = 0; i < course_num.length(); i++) {
            if (Character.isDigit(course_num.charAt(i)) || Character.isSpaceChar(course_num.charAt(i))) {
                endPoint = i;
                break;
            }
        }
        final String major;
        if(endPoint == 0)
            major = course_num.toUpperCase();
        else
            major = course_num.substring(0, endPoint).toUpperCase();

        System.out.println(major);

        db.collection("Books").document(school).collection(major)
                .whereEqualTo("Course_Number", course_num)
                .whereEqualTo("Status", "A")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) { // for each school (document = school document)
                                String downloadURL = document.getString("Image");
                                if (downloadURL != null) {
                                    getImage(downloadURL, document);
                                } else {
                                    Book temp_book = new Book(document.getString("Book_Name"), document.getString("Edition"),
                                            "CAD $" + document.getString("Price"), null, document.getString("School"),
                                            document.getString("Course_Number"), document.getString("Book_ID"), document.getString("Author"),
                                            document.getString("Book_Description"), document.getDate("Create_Date"), document.getString("Seller"),
                                            document.getString("Status"), document.getString("Username"), document.getString("Email"),
                                            document.getString("ExtraContact"), document.getString("Image"), document.getString("BuyerUID"));
                                    bookList.add(temp_book);
                                }
                                // TODO:
                                // Temp workaround
                                adapter = new BookListAdapter(getApplicationContext(), R.layout.adapter_view_layout, bookList);
                                mListView.setAdapter(adapter);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });
    }

    private void QueryOnBookName(final String searchedBookName) {
        db.collection("Books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) { // for each school (document = school document)
                                Object title_list = document.get("Titles");
                                ArrayList al = (ArrayList) title_list;

                                if (title_list != null) {
                                    ArrayList<Pair> majors_and_ID = new ArrayList<>();
                                    for (Object title_segment : al) {
                                        String temp = (String) title_segment;
                                        String[] bk_name_and_major = temp.split("_");
                                        if (bk_name_and_major[0].toLowerCase().contains(searchedBookName.toLowerCase())) {
                                            Pair<String, String> pair = new Pair<String, String>(bk_name_and_major[1], bk_name_and_major[2]);
                                            majors_and_ID.add(pair);
                                        }
                                    }

                                    for (int i = 0; i < majors_and_ID.size(); i++) {
                                        QueryOnMajor(String.valueOf(document.getId()), majors_and_ID.get(i).first.toString(), majors_and_ID.get(i).second.toString());
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });
    }

    private void QueryOnMajor(final String school, final String major, final String bookID) {
        System.out.println(school + "_+_" + major + "_+_" + bookID);
        db.collection("Books").document(school).collection(major).document(bookID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.getString("Status").equals("A")) {
                                String downloadURL = document.getString("Image");
                                if (downloadURL != null) {
                                    getImage(downloadURL, document);
                                } else {
                                    Book temp_book = new Book(document.getString("Book_Name"), document.getString("Edition"),
                                            "CAD $" + document.getString("Price"), null, document.getString("School"),
                                            document.getString("Course_Number"), document.getString("Book_ID"), document.getString("Author"),
                                            document.getString("Book_Description"), document.getDate("Create_Date"), document.getString("Seller"),
                                            document.getString("Status"), document.getString("Username"), document.getString("Email"),
                                            document.getString("ExtraContact"), document.getString("Image"), document.getString("BuyerUID"));
                                    bookList.add(temp_book);
                                }
                                // TODO:
                                // Temp workaround
                                adapter = new BookListAdapter(getApplicationContext(), R.layout.adapter_view_layout, bookList);
                                mListView.setAdapter(adapter);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    private void getImage(final String downloadURL, final DocumentSnapshot document) {
        StorageReference httpsReference = null;
        final Bitmap[] bitmap = {null};
        if(downloadURL != null) {
            httpsReference = storage.getReferenceFromUrl(Objects.requireNonNull(downloadURL));
            try {
                final File localFile = File.createTempFile("images", "jpg");
                httpsReference.getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                bitmap[0] = BitmapFactory.decodeFile(localFile.getPath());
                                Book temp_book = new Book(document.getString("Book_Name"), document.getString("Edition"),
                                        "CAD $" + document.getString("Price"), bitmap[0],  document.getString("School"),
                                        document.getString("Course_Number"), document.getString("Book_ID"), document.getString("Author"),
                                        document.getString("Book_Description"), document.getDate("Create_Date"), document.getString("Seller"),
                                        document.getString("Status"), document.getString("Username"), document.getString("Email"),
                                        document.getString("ExtraContact"), downloadURL, document.getString("BuyerUID"));
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
        }
    }

    private void setupListClickListener(){
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book clicked = bookList.get(position);
                Intent i = new Intent(FindBookResultSchMajorActivity.this, ResultDetailActivity.class);
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
}
