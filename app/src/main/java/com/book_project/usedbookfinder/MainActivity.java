package com.book_project.usedbookfinder;

import android.app.Activity;
import android.content.Intent;
import android.media.session.MediaSessionManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    //private FirebaseFirestore db = null;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
//        MyFirebaseInstanceIDService id = new MyFirebaseInstanceIDService();
//        id.onNewToken(FirebaseInstanceId.getInstance().getToken());
    }

    public void clickSigninButton(View view) {
        startActivity(new Intent(MainActivity.this, SignUpActivity.class));
    }


    public void clickLogonButton(View view){
        startActivity(new Intent(MainActivity.this, LogInActivity.class));
    }
}
