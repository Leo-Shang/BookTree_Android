package com.book_project.usedbookfinder;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends Activity {
    private FirebaseAuth auth;
    private String TAG = "TAG";
    private FirebaseFirestore db;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void clickSignupButton(View view){
        EditText email = (EditText) findViewById(R.id.forgot_email);
        EditText userName = (EditText) findViewById(R.id.user_name);
        EditText pwd = (EditText) findViewById(R.id.password);
        EditText confirm_pwd = (EditText) findViewById(R.id.confirm_password);
        final String email_string = email.getText().toString();
        String pwd_string = pwd.getText().toString();
        String confirm_pwd_string = confirm_pwd.getText().toString();
        final String userName_str = userName.getText().toString();


        if (TextUtils.isEmpty(email_string)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        else if (TextUtils.isEmpty(pwd_string)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        else if (TextUtils.isEmpty(userName_str)) {
            Toast.makeText(getApplicationContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        else if (!confirm_pwd_string.equals(pwd_string)) {
            Toast.makeText(getApplicationContext(), "Check Your Passwords", Toast.LENGTH_SHORT).show();
            return;
        }

        else if (pwd_string.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        //create user
        auth.createUserWithEmailAndPassword(email_string, pwd_string)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "Sign Up Succeed, Please Verify Your Email", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                Map<String, Object> data = new HashMap<>();
                                data.put("Username", userName_str);
                                data.put("Email", email_string);
                                db.collection("Users").document(user.getUid())
                                        .set(data);

                                // Red Dot
                                Map<String, Integer> countOnBuyers = new HashMap<>();
                                countOnBuyers.put("CountsDiff", 0);
                                countOnBuyers.put("CountsOnBuyersAllTime", 0);
                                countOnBuyers.put("CountsOnBuyersLogOut", 0);
                                db.collection("CountOnBuyers").document(user.getUid()).set(countOnBuyers);
                                // End Red Dot

                                finish();
                            }
                        }
                    }
                });

    }
}
