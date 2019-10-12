package com.book_project.usedbookfinder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FirstFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private String uid = user.getUid();
    private ArrayList<Book> bookList = new ArrayList<>();
    private BookListWithDeleteButtonAdapter adapter;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private ListView lv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_my_books, container, false);

        lv = (ListView) v.findViewById(R.id.listView);

        fillInContent();

        return v;
    }

    private void fillInContent() {
        db.collection("Users").document(user.getUid()).collection("Sell")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                String downloadURL = document.getString("Image");
                                if(downloadURL == null){


                                    Book temp_book = new Book(document.getString("Book_Name"), document.getString("Edition"), "CAD $" + document.getString("Book_Price"), null,
                                            document.getString("School"), document.getString("Major"), document.getString("Book_ID"), document.getString("Author"),
                                            document.getString("Book_Description"), document.getDate("Create_Date"), document.getString("Seller"), document.getString("Status"), document.getString("Username"),
                                            document.getString("Email"), document.getString("ExtraContact"), document.getString("Image"), document.getString("BuyerUID"));
                                    bookList.add(temp_book);
                                } else{
                                    getImage(downloadURL, document);
                                }
//                                adapter = new BookListWithDeleteButtonAdapter(getContext(), R.layout.adapter_view_with_delete_button, bookList);
//                                lv.setAdapter(adapter);
                            }


                        } else {
                            Toast.makeText(getContext(), "Database Error", Toast.LENGTH_SHORT).show();
                        }
                        adapter = new BookListWithDeleteButtonAdapter(getContext(), R.layout.adapter_view_with_delete_button, bookList);
                        lv.setAdapter(adapter);
                    }
                });
    }

    public static FirstFragment newInstance(String text) {

        FirstFragment f = new FirstFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    private void getImage(final String downloadURL, final QueryDocumentSnapshot document) {
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

                                Book temp_book = new Book(document.getString("Book_Name"), document.getString("Edition"), "CAD $" + document.getString("Book_Price"), bitmap[0],
                                        document.getString("School"), document.getString("Major"), document.getString("Book_ID"), document.getString("Author"),
                                        document.getString("Book_Description"), document.getDate("Create_Date"), document.getString("Seller"), document.getString("Status"), document.getString("Username"),
                                        document.getString("Email"), document.getString("ExtraContact"), document.getString("Image"), document.getString("BuyerUID"));
                                bookList.add(temp_book);

//                                //TODO:
//
                                adapter = new BookListWithDeleteButtonAdapter(getContext(), R.layout.adapter_view_with_delete_button, bookList);
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
        }
    }
}
