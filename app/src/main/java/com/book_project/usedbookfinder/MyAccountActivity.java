package com.book_project.usedbookfinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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

import com.astuetz.PagerSlidingTabStrip;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MyAccountActivity extends FragmentActivity {
    private FirebaseUser user;
    private FirebaseAuth auth;
    String TAG = "TAG";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        setupNav();
        setupSpinner();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsStrip.setViewPager(viewPager);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.bold);
        tabsStrip.setTypeface(typeface, 0);
    }

    private void setupSpinner() {
        Spinner dotSpinner = (Spinner) findViewById(R.id.dot_spinner);
        final List<String> categories = new ArrayList<String>();
        categories.add(" ");
        categories.add("Change Password");
        categories.add("Log Out");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item, categories){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent)
            {
                View v = null;

                // If this is the initial dummy entry, make it hidden
                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                }
                else if (position == 1){
                    v = super.getDropDownView(position, null, parent);
                    ((TextView) v).setGravity(Gravity.CENTER);
                }
                else if(position == 2){
                    v = super.getDropDownView(position, null, parent);
                    ((TextView) v).setGravity(Gravity.CENTER);
                    ((TextView) v).setTextColor(Color.parseColor("#FF0000"));
                }
                parent.setVerticalScrollBarEnabled(false);
                return v;
            }
        };
        //dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dotSpinner.setAdapter(dataAdapter);

        AdapterView.OnItemSelectedListener actionSelectedListener = new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> spinner, View container, int position, long id) {
                if(position == 1) {
                    clickChangePasswordButton();
                } else if (position == 2){
                    logoutUser();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        };
        dotSpinner.setOnItemSelectedListener(actionSelectedListener);
    }

    private void setupNav() {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.findBook:
                        Intent findbook = new Intent(MyAccountActivity.this, FindBookActivity.class);
                        startActivity(findbook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.sellBook:
                        Intent sellBook = new Intent(MyAccountActivity.this, SellBookActivity.class);
                        startActivity(sellBook);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myBuyers:
                        Intent myBuyers = new Intent(MyAccountActivity.this, MyBuyersActivity.class);
                        startActivity(myBuyers);
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.myAccount:
                        Intent myAccount = new Intent(MyAccountActivity.this, MyAccountActivity.class);
                        startActivity(myAccount);
                        overridePendingTransition(0, 0);
                        break;
                }
                return false;
            }
        });
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[] {"My Books", "My Orders"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {

                case 0: return FirstFragment.newInstance("FirstFragment, Instance 1");
                case 1: return SecondFragment.newInstance("SecondFragment, Instance 1");
                default: return FirstFragment.newInstance("FirstFragment, Default");
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

    public void clickFindBookButton (View view) {
        Log.i(TAG, "clicked find book button");
        startActivity(new Intent(MyAccountActivity.this, FindBookActivity.class));
        overridePendingTransition(0, 0);
    }

    public void clickSellBookButton(View view){
        Log.i(TAG, "clicked Sell Book Button");
        startActivity(new Intent(MyAccountActivity.this, SellBookActivity.class));
        overridePendingTransition(0, 0);
    }

    private void clickOnMyBuyers(View view){
        Log.i(TAG, "clicked on my buyers button");
        startActivity(new Intent(MyAccountActivity.this, MyBuyersActivity.class));
        overridePendingTransition(0, 0);
    }

    private void logoutUser(){
        // Red Dot
        db.collection("CountOnBuyers").document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        int CountsOnBuyersAllTime = Integer.valueOf(documentSnapshot.get("CountsOnBuyersAllTime").toString());
                        db.collection("CountOnBuyers").document(user.getUid()).update("CountsOnBuyersLogOut", CountsOnBuyersAllTime);
                    }
                });
        // End Red Dot

        String email = user.getEmail();
        String topic = email.replaceAll("@", "");
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
        auth.signOut();
        finish();
        startActivity(new Intent(MyAccountActivity.this, MainActivity.class));
    }

    private void clickChangePasswordButton(){
        if(user != null) {
            if (user.getEmail() != null) {
                auth.sendPasswordResetEmail(user.getEmail())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                } else {
                                    Toast.makeText(getApplicationContext(), "Please Retry Later", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        } else
            Toast.makeText(getApplicationContext(), "Cannot find email", Toast.LENGTH_SHORT).show();
    }
}
