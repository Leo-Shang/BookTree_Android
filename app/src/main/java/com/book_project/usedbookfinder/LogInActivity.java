package com.book_project.usedbookfinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

public class LogInActivity extends Activity {
    private FirebaseAuth auth;
    private String TAG = "TAG";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void clickGoButton(View view){
        EditText userName = (EditText) findViewById(R.id.user_name);
        EditText pwd = (EditText) findViewById(R.id.password);
        final String userName_string = userName.getText().toString();
        String pwd_string = pwd.getText().toString();


        if (TextUtils.isEmpty(userName_string)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(pwd_string)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(userName_string, pwd_string)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser user = auth.getCurrentUser();
                            if(user != null) {
                                user.reload();
                                if (user.isEmailVerified()) {
                                    Intent intent = new Intent(LogInActivity.this, FindBookActivity.class);
                                    // New Code, Nov 25, 2018
                                    try {
                                        FirebaseInstanceId.getInstance().deleteInstanceId();
                                    } catch (IOException e) {
                                        System.out.println(e.toString());
                                    }
                                    FirebaseMessaging.getInstance().subscribeToTopic(userName_string.replaceAll("@", ""));
                                    // End New Code

                                    // Red Dot
                                    db.collection("CountOnBuyers").document(user.getUid())
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    int CountsDiff = Integer.valueOf(documentSnapshot.get("CountsDiff").toString());
                                                    int CountsOnBuyersAllTime = Integer.valueOf(documentSnapshot.get("CountsOnBuyersAllTime").toString());
                                                    int CountsOnBuyersLogOut = Integer.valueOf(documentSnapshot.get("CountsOnBuyersLogOut").toString());
                                                    int valueToBeUpdated = CountsOnBuyersAllTime - CountsOnBuyersLogOut + 1;
                                                    db.collection("CountOnBuyers").document(user.getUid()).update("CountsDiff", valueToBeUpdated);
                                                }
                                            });
                                    // End Red Dot

                                    startActivity(intent);
                                    finish();
                                } else {
                                    auth.signOut();
                                    Toast.makeText(getApplicationContext(), "Please Verify Your Email Address", Toast.LENGTH_SHORT).show();
                                }
                            } else
                                Toast.makeText(getApplicationContext(), "Application Error", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(LogInActivity.this, "Log In Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    public void forgotPassword(View view){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(LogInActivity.this);
        final View mView = getLayoutInflater().inflate(R.layout.dialog_email, null);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        Button ok = (Button) mView.findViewById(R.id.submit);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText email = (EditText) mView.findViewById(R.id.forgot_email);
                String emailAddress = email.getText().toString();
                if(!TextUtils.isEmpty(emailAddress)) {
                    auth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Email Sent", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                } else
                    Toast.makeText(getApplicationContext(), "Please Enter Your Email Address", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
