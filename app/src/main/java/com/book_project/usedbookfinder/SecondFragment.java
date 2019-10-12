package com.book_project.usedbookfinder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class SecondFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private String uid = user.getUid();
    private ArrayList<Book> bookList = new ArrayList<>();
    private BookListMyOrders adapter;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private ListView lv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_my_orders, container, false);
        lv = (ListView) v.findViewById(R.id.listView);

        final Task<QuerySnapshot> task = db.collection("Users").document(uid).collection("Buy").get();
        task.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                for (final QueryDocumentSnapshot document : task.getResult()){
                    String username = document.getString("Username");
                    String email = document.getString("Email");
                    if(username != null && email != null){
                        final String downloadURL = document.getString("Image");
                        if(downloadURL != null){
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
                                                Book temp_book = new Book(document.getString("Bookname"), document.getString("Edition"), "CAD $" + document.getString("Price"), bitmap[0],
                                                        document.getString("School"), document.getString("Course_Number"), document.getId(), document.getString("Author"),
                                                        document.getString("Book_Description"), document.getDate("Create_Date"), document.getString("Username"), document.getString("Status"), document.getString("Username"),
                                                        document.getString("Email"), document.getString("ExtraContact"), document.getString("Image"), document.getString("BuyerUID"));
                                                bookList.add(temp_book);

                                                //TODO:
                                                // Temp workaround
                                                adapter = new BookListMyOrders(getContext(), R.layout.adapter_view_my_orders, bookList);
                                                lv.setAdapter(adapter);

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
                        } else{
                            Book temp_book = new Book(document.getString("Bookname"), "Username: " + document.getString("Username") + "\nEmail: " +  document.getString("Email")
                                    + "\nExtra Contact: " + document.getString("ExtraContact"), "CAD $" + document.getString("Price"), null,
                                    null, null, null, null, null, null, null, null,
                                    null, null, null, null, null);
                            bookList.add(temp_book);
                            adapter =  new BookListMyOrders(getContext(), R.layout.adapter_view_my_orders, bookList);
                            lv.setAdapter(adapter);
                        }
                    }
                }
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            public void onFailure(Exception e) {
                // handle any errors here
            }
        });

        return v;
    }

    public static SecondFragment newInstance(String text) {

        SecondFragment f = new SecondFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }
}
