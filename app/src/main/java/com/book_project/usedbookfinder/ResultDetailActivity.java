package com.book_project.usedbookfinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class ResultDetailActivity extends Activity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private String uid;
    private String username;
    private FirebaseFirestore db;
    private String seller_uid;
    private String school;
    private String course_number;
    private String book_ID;
    private String email;
    private String bookName;
    private String price;
    private String seller_username;
    private String seller_email;
    private String extraContact;
    private String imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_detail);
        //setupNav();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        uid = user.getUid();
        email = user.getEmail();
        db = FirebaseFirestore.getInstance();
        db.collection("Users").document(uid)
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    username = documentSnapshot.getString("Username");
                }
            });


        bookName = getIntent().getStringExtra("bookName");
        final String edition = getIntent().getStringExtra("bookEdition");
        final String author = getIntent().getStringExtra("bookAuthor");
        price = getIntent().getStringExtra("bookPrice");
        book_ID = getIntent().getStringExtra("bookID");
        final String description = getIntent().getStringExtra("bookDescrip");
        course_number = getIntent().getStringExtra("bookCourse").toUpperCase();

        Date createDate = new Date();
        createDate.setTime(getIntent().getLongExtra("bookCreateDate", -1));

        school = getIntent().getStringExtra("bookSchool");
        seller_uid = getIntent().getStringExtra("bookSeller");
        seller_username = getIntent().getStringExtra("username");
        seller_email = getIntent().getStringExtra("email");
        extraContact = getIntent().getStringExtra("extraContact");
        imageURL = getIntent().getStringExtra("imageURL");

        final String status = getIntent().getStringExtra("bookStatus");

//        byte[] byteArray = getIntent().getByteArrayExtra("bookImage");
//
//
        if(imageURL != null) {
            StorageReference httpsReference = null;
            httpsReference = storage.getReferenceFromUrl(Objects.requireNonNull(imageURL));
            try {
                final File localFile = File.createTempFile("images", "jpg");
                httpsReference.getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                  @Override
                                                  public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                      ImageView iv = (ImageView) findViewById(R.id.book_detail_img);
                                                      iv.setImageBitmap(BitmapFactory.decodeFile(localFile.getPath()));
                                                  }
                                              });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TextView book_name_detail = (TextView) findViewById(R.id.book_name_detail);
        book_name_detail.setText(bookName);

        TextView price_detail = (TextView) findViewById(R.id.book_price_detail);
        price_detail.setText(price);

        TextView author_detail = (TextView) findViewById(R.id.author_detail);
        author_detail.setText(author);

        TextView edition_detail = (TextView) findViewById(R.id.edition_detail);
        edition_detail.setText(edition);

        TextView school_detail = (TextView) findViewById(R.id.school_detail);
        school_detail.setText(school);

        TextView course_detail = (TextView) findViewById(R.id.course_detail);
        course_detail.setText(course_number);

        TextView description_detail = (TextView) findViewById(R.id.description);
        if(description != null)
            description_detail.setText(description);

        TextView create_date_detail = (TextView) findViewById(R.id.create_date_detail);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.CANADA);
        formatter.setTimeZone(TimeZone.getTimeZone("America/Vancouver"));
        create_date_detail.setText(formatter.format(createDate));
    }

    private void setupNav() {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.findBook:
                        Intent findbook = new Intent(ResultDetailActivity.this, FindBookActivity.class);
                        startActivity(findbook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.sellBook:
                        Intent sellBook = new Intent(ResultDetailActivity.this, SellBookActivity.class);
                        startActivity(sellBook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myBuyers:
                        Intent myBuyers = new Intent(ResultDetailActivity.this, MyBuyersActivity.class);
                        startActivity(myBuyers);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myAccount:
                        Intent myAccount = new Intent(ResultDetailActivity.this, MyAccountActivity.class);
                        startActivity(myAccount);
                        overridePendingTransition(0, 0);
                        break;
                }
                return false;
            }
        });
    }

    public void clickOnBuyButton(View view){
        if(uid.equals(seller_uid)){
            Toast.makeText(getApplicationContext(), "You Cannot Buy Your Books", Toast.LENGTH_SHORT).show();
        } else {
            // Red Dot
            db.collection("CountOnBuyers").document(user.getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            int CountsOnBuyersAllTime = Integer.valueOf(documentSnapshot.get("CountsOnBuyersAllTime").toString());
                            CountsOnBuyersAllTime += 1;
                            db.collection("CountOnBuyers").document(user.getUid()).update("CountsOnBuyersAllTime", CountsOnBuyersAllTime);
                        }
                    });
            // End Red Dot
            int endPoint = 0;
            for (int i = 0; i < course_number.length(); i++) {
                if (Character.isDigit(course_number.charAt(i)) || Character.isSpaceChar(course_number.charAt(i))) {
                    endPoint = i;
                    break;
                }
            }
            final String major;
            if(endPoint == 0)
                major = course_number.toUpperCase();
            else
                major = course_number.substring(0, endPoint).toUpperCase();
            System.out.println(school + major + book_ID);

            final String[] status = new String[1];
            db.collection("Books").document(school).collection(major).document(book_ID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            status[0] = documentSnapshot.getString("Status");
                            if(status[0].equals("A")) {
                                db.collection("Books").document(school).collection(major).document(book_ID)
                                        .update("Status", "N");
                                db.collection("Users").document(seller_uid).collection("Sell").document(book_ID)
                                        .update("Status", "N");
                                db.collection("RecentlyAdded").document("Books")
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                ArrayList<String> titles = (ArrayList)documentSnapshot.get("RecentTitles");
                                                for(int i = 0; i < titles.size(); i++){
                                                    String[] segments = titles.get(i).split("_");
                                                    if(segments[2].equals(book_ID)){
                                                        titles.set(i, school + "_" + major + "_" + book_ID + "_" + "N");
                                                        break;
                                                    }
                                                }
                                                db.collection("RecentlyAdded").document("Books").update("RecentTitles", titles);
                                            }
                                        });
                                db.collection("Users").document(seller_uid).collection("Sell").document(book_ID)
                                        .update("Email", email);
                                db.collection("Users").document(seller_uid).collection("Sell").document(book_ID)
                                        .update("Username", username);
                                db.collection("Users").document(seller_uid).collection("Sell").document(book_ID)
                                        .update("BuyerUID", uid);
                                Map<String, Object> map = new HashMap<>();
                                map.put("Image", imageURL);
                                map.put("Bookname", bookName);
                                price = price.substring(5);
                                map.put("Price", price);
                                map.put("Username", seller_username);
                                map.put("Email", seller_email);
                                map.put("ExtraContact", extraContact);
                                db.collection("Users").document(uid).collection("Buy").document(book_ID)
                                        .set(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "Bought", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(ResultDetailActivity.this, FindBookActivity.class));
                                                finish();
                                            }
                                        });
                            } else{
                                Toast.makeText(getApplicationContext(), "Book Has Been Ordered By Others", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void clickSellBookButton(View view){
        startActivity(new Intent(ResultDetailActivity.this, SellBookActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickMyAccountButton(View view) {
        startActivity(new Intent(ResultDetailActivity.this, MyAccountActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickOnMyBuyers(View view){
        startActivity(new Intent(ResultDetailActivity.this, MyBuyersActivity.class));
        overridePendingTransition(0, 0);
    }
}
