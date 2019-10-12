package com.book_project.usedbookfinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.BottomNavigationView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SellBookActivity extends Activity {
    private FirebaseFirestore db = null;
    private String TAG = "TAG";
    private EditText bookName;
    private EditText edition;
    private EditText author;
    private EditText price;
    private EditText courseNum;
    private Spinner school;
    private EditText bookDescrip;
    private TextView contact;
    private ImageButton addPic;
    private FirebaseAuth auth;
    private String uid;
    private FirebaseUser user;
    private final String[] username = new String[1];

    private static final int SELECTED_PICTURE = 1;
//    ImageView iv;
    private FirebaseStorage storage;
    private ArrayList<Uri> uri_list;
    private String bookID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_book);
        setupNav();
        bookName = (EditText) findViewById(R.id.bookName_textfield);
        edition = (EditText) findViewById(R.id.Edition);
        author = (EditText) findViewById(R.id.Author);
        price = (EditText) findViewById(R.id.price);
        courseNum = (EditText) findViewById(R.id.course_num);
        school = (Spinner) findViewById(R.id.school);
        bookDescrip = (EditText) findViewById(R.id.book_descrip);
        addPic = (ImageButton) findViewById(R.id.add_pic);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uid = user.getUid();
        uri_list = new ArrayList<>();
//        iv = (ImageView) findViewById(R.id.bookPic);
        contact = (TextView) findViewById(R.id.extraContact);

        List<String> categories = new ArrayList<String>();
        categories.add("SFU");
        categories.add("UBC");
        categories.add("BCIT");
        categories.add("KPU");
//        categories.add("N/A");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item, categories);
        //dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        school.setAdapter(dataAdapter);

        db.collection("Users").document(uid)
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    username[0] = documentSnapshot.getString("Username");
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
                        Intent findbook = new Intent(SellBookActivity.this, FindBookActivity.class);
                        startActivity(findbook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.sellBook:
                        Intent sellBook = new Intent(SellBookActivity.this, SellBookActivity.class);
                        startActivity(sellBook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myBuyers:
                        Intent myBuyers = new Intent(SellBookActivity.this, MyBuyersActivity.class);
                        startActivity(myBuyers);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myAccount:
                        Intent myAccount = new Intent(SellBookActivity.this, MyAccountActivity.class);
                        startActivity(myAccount);
                        overridePendingTransition(0, 0);
                        break;
                }
                return false;
            }
        });
    }

    public void clickFindBookButton(View view){
        startActivity(new Intent(SellBookActivity.this, FindBookActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickMyAccountButton(View view) {
        Log.i(TAG, "clicked my account button");
        startActivity(new Intent(SellBookActivity.this, MyAccountActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickOnMyBuyers(View view){
        Log.i(TAG, "clicked on my buyers button");
        startActivity(new Intent(SellBookActivity.this, MyBuyersActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickAddPicButton(View view){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECTED_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SELECTED_PICTURE:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    uri_list.add(uri);
                    final Button delete = new Button(SellBookActivity.this);
                    delete.setId(1+1);
                    delete.setBackground(getDrawable(R.drawable.delete_bin));
                    delete.setPadding(15, 5, 0, 5);

                    // ADD YOUR CODE HERE...
                    // delete is the name of the button

                    LinearLayout parent = (LinearLayout) findViewById(R.id.LinearLayout);
                    parent.addView(delete);
                    // TODO: change R.id.add_pic to filled button version
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            uri_list = new ArrayList<>();
                            LinearLayout parent = (LinearLayout) findViewById(R.id.LinearLayout);
                            parent.removeView(delete);
                            // TODO: chagne R.id.add_pic to hollowed button version
                        }
                    });

//                    try {
//                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                        ImageView imageView = (ImageView) findViewById(R.id.bookPic);
//                        imageView.setImageBitmap(bitmap);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
        }
    }

//    public static String getPath(Context context, Uri uri ) {
//        String result = null;
//        String[] proj = { MediaStore.Images.Media.DATA };
//        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
//        if(cursor != null){
//            if ( cursor.moveToFirst( ) ) {
//                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
//                result = cursor.getString( column_index );
//            }
//            cursor.close( );
//        }
//        if(result == null) {
//            result = "Not found";
//        }
//        return result;
//    }

    public void clickSellBookButton(View view){

        if(user != null) {
            Log.i(TAG, "clicked on sell me button");
            final String bookName_str = (String) bookName.getText().toString();
            final String edition_str = (String) edition.getText().toString();
            final String author_str = (String) author.getText().toString();
            final String price_str = (String) price.getText().toString();
            final String courseNum_str = (String) courseNum.getText().toString().replaceAll("\\s+", "").toUpperCase();
            final String school_str = (String) school.getSelectedItem().toString();
            final String bookDescrip_str = (String) bookDescrip.getText().toString();
            final String extraContact = (String) contact.getText().toString();


            Map<String, Object> new_book = new HashMap<>();
            new_book.put("Book_Name", bookName_str);
            new_book.put("Edition", edition_str);
            new_book.put("Author", author_str);
            new_book.put("Price", price_str);
            new_book.put("Course_Number", courseNum_str);
            new_book.put("School", school_str);
            if (TextUtils.isEmpty(bookDescrip_str))
                new_book.put("Book_Description", null);
            else
                new_book.put("Book_Description", bookDescrip_str);
            new_book.put("Book_ID", null);
            new_book.put("Create_Date", null);
            new_book.put("Seller", uid);
            new_book.put("Status", "A");
            new_book.put("Image", null);
            new_book.put("Username", username[0]);
            new_book.put("Email", user.getEmail());
            new_book.put("ExtraContact", extraContact);


            int endPoint = 0;
            for (int i = 0; i < courseNum_str.length(); i++) {
                if (Character.isDigit(courseNum_str.charAt(i)) || Character.isSpaceChar(courseNum_str.charAt(i))) {
                    endPoint = i;
                    break;
                }
            }
            final String major;
            if(endPoint == 0)
                major = courseNum_str.toUpperCase();
            else
                major = courseNum_str.substring(0, endPoint).toUpperCase();


            db.collection("Books").document(school_str).collection(major)
                    .add(new_book)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());

                            db.collection("Books").document(school_str).collection(major).document(documentReference.getId())
                                    .update("Book_ID", documentReference.getId());
                            bookID = documentReference.getId();

                            Date date = new Date();
                            Timestamp timestamp = new Timestamp(date);
                            db.collection("Books").document(school_str).collection(major).document(documentReference.getId()) // Time stamp
                                    .update("Create_Date", timestamp);
                            Toast.makeText(getApplicationContext(), "Book Posted Successfully", Toast.LENGTH_SHORT).show();
                            UpdateUserTree(documentReference.getId(), bookName_str, price_str, timestamp, school_str, major);
                            UpdateImageUrl(documentReference.getId(), school_str, major);

                            db.collection("RecentlyAdded").document("Books")
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            Object temp_title = documentSnapshot.get("RecentTitles");
                                            if(temp_title == null){
                                                ArrayList<String> new_titles = new ArrayList<>();
                                                if(bookID != null){
                                                    String s = school_str + "_" + major + "_" + bookID + "_" + "A";
                                                    new_titles.add(s);
                                                    Map<String, Object> recentTitle = new HashMap<>();
                                                    recentTitle.put("RecentTitles", new_titles);
                                                    db.collection("RecentlyAdded").document("Books").set(recentTitle);
                                                }
                                            } else {
                                                ArrayList new_titles = (ArrayList)temp_title;
                                                if(bookID != null) {
                                                    String s = school_str + "_" + major + "_" + bookID + "_" + "A";
                                                    new_titles.add(s);
                                                    db.collection("RecentlyAdded").document("Books")
                                                            .update("RecentTitles", new_titles);
                                                }
                                            }
                                        }
                                    });

                            db.collection("Books").document(school_str) // Title array
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Object temp_title = documentSnapshot.get("Titles");
                                        if(temp_title == null){
                                            ArrayList<String> new_titles = new ArrayList<>();
                                            if(bookID != null) {
                                                String s = bookName_str + "_" + major + "_" + bookID;
                                                new_titles.add(s);
                                                Map<String, Object> title_values = new HashMap<>();
                                                title_values.put("Titles", new_titles);
                                                db.collection("Books").document(school_str)
                                                        .set(title_values);
                                            }
                                        } else {
                                            ArrayList new_titles = (ArrayList)temp_title;
                                            if(bookID != null) {
                                                String s = bookName_str + "_" + major + "_" + bookID;
                                                new_titles.add(s);
                                                db.collection("Books").document(school_str)
                                                        .update("Titles", new_titles);
                                            }
                                        }
                                        startActivity(new Intent(SellBookActivity.this, FindBookActivity.class));
                                        finish();
                                    }
                                });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Adding Book Failed, Please Check Your Connection",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void UpdateImageUrl(final String book_id, final String sch, final String major) {
        if(uri_list.size() >= 1){
            storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            for(int i = 0; i < uri_list.size(); i++) {
                Uri file = uri_list.get(i);
                if (file.getLastPathSegment() != null) {
                    final StorageReference imagesRef = storageRef.child("Images").child(file.getLastPathSegment()); // PATH IN GOOGLE STORAGE
                    UploadTask uploadTask = imagesRef.putFile(file);

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw Objects.requireNonNull(task.getException());
                            }

                            return imagesRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                System.out.println(downloadUri); //HTTP URL
                                db.collection("Books").document(sch).collection(major).document(book_id)
                                        .update("Image", downloadUri.toString());
                                db.collection("Users").document(uid).collection("Sell").document(book_id)
                                        .update("Image", downloadUri.toString());
                            } else {
                                Toast.makeText(getApplicationContext(), "Image Upload Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

        }
    }

    private void UpdateUserTree(final String book_id, final String book_name, final String price_str, final Timestamp timestamp,
                                final String school, final String major) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Map<String, Object> new_book = new HashMap<>();
            new_book.put("Book_ID", book_id);
            new_book.put("Book_Name", book_name);
            new_book.put("Create_Date", timestamp);
            new_book.put("Book_Price", price_str);
            new_book.put("School", school);
            new_book.put("Major", major);
            new_book.put("Image", null);
            new_book.put("Email", null);
            new_book.put("Username", null);
            new_book.put("BuyerUID", null);
            new_book.put("Status", "A");


            db.collection("Users").document(uid).collection("Sell").document(book_id)
                    .set(new_book);
        }
    }
}
