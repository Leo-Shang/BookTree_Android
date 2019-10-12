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

import java.io.ByteArrayOutputStream;

import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class FindBookResultActivity extends Activity {
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
        setContentView(R.layout.activity_find_result);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mListView = (ListView) findViewById(R.id.listView);
        setupNav();

        Bundle b = this.getIntent().getExtras();
        assert b != null;
        String[] array = b.getStringArray("Array");
        assert array != null;
        if(array.length == 1){
            bkn = array[0];
            QueryOnBookName(bkn);
            setupListClickListener();
        } else if (array.length == 2){
            school = array[0];
            course_num = array[1];
            QueryOnSchoolAndMajor(school, course_num);
            setupListClickListener();
        }
    }

    private void setupNav() {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.findBook:
                        Intent findbook = new Intent(FindBookResultActivity.this, FindBookActivity.class);
                        startActivity(findbook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.sellBook:
                        Intent sellBook = new Intent(FindBookResultActivity.this, SellBookActivity.class);
                        startActivity(sellBook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myBuyers:
                        Intent myBuyers = new Intent(FindBookResultActivity.this, MyBuyersActivity.class);
                        startActivity(myBuyers);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myAccount:
                        Intent myAccount = new Intent(FindBookResultActivity.this, MyAccountActivity.class);
                        startActivity(myAccount);
                        overridePendingTransition(0, 0);
                        break;
                }
                return false;
            }
        });
    }

    public void new_search(View view){
        EditText bookName = (EditText) findViewById(R.id.bookName_textfield);
        String book_name = bookName.getText().toString();

        if (TextUtils.isEmpty(book_name)) {
            Toast.makeText(getApplicationContext(), "Enter Book Name", Toast.LENGTH_SHORT).show();
        } else {
            bkn = book_name;
            bookList = new ArrayList<>();
            bookList.clear();
            QueryOnBookName(bkn);
            setupListClickListener();
        }
    }

    private void QueryOnSchoolAndMajor(String school, String course_num) {
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
        adapter = new BookListAdapter(getApplicationContext(), R.layout.adapter_view_layout, bookList);
        mListView.setAdapter(adapter);
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
                Intent i = new Intent(FindBookResultActivity.this, ResultDetailActivity.class);
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

//                if (clicked.getIv() != null) {
//                    ByteArrayOutputStream bStream = new ByteArrayOutputStream();
//                    Bitmap bitmap = clicked.getIv();
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
//                    byte[] byteArray = bStream.toByteArray();
//                    i.putExtra("bookImage", byteArray);
//                }
                startActivity(i);
            }
        });
    }

    public void clickSellBookButton(View view){
        startActivity(new Intent(FindBookResultActivity.this, SellBookActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickMyAccountButton(View view) {
        startActivity(new Intent(FindBookResultActivity.this, MyAccountActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickOnMyBuyers(View view){
        startActivity(new Intent(FindBookResultActivity.this, MyBuyersActivity.class));
        overridePendingTransition(0, 0);
    }
}