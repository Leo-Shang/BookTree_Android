package com.book_project.usedbookfinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;

public class BookListWithTransButtonAdapter extends ArrayAdapter<Book> {

    private Context mContext;
    private int mResource;
    private int lastPosition = -1;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String uid;
    private FirebaseStorage storage;

    private static class ViewHolder_withTrans {
        TextView bookname;
        TextView seller;
        TextView seller_email;
        TextView book_price;
        ImageView book_image;
        ImageButton cancel;
        ImageButton finish;
    }


    public BookListWithTransButtonAdapter(Context context, int resource, ArrayList<Book> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user != null){
            uid = user.getUid();
        }
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the persons information
        final String book_name = getItem(position).getBook_name();
        String author = getItem(position).getEdition();
        String price = getItem(position).getPrice();
        final Bitmap iv = getItem(position).getIv();
        final String school = getItem(position).getSchool();
        final String major = getItem(position).getCourse_num();
        final String bookID = getItem(position).getBk_id();
        String book_descrip = getItem(position).getBook_descrip();
        Date date = getItem(position).getCreate_date();
        String seller = getItem(position).getSeller();
        final String status = getItem(position).getStatus();
        String username = getItem(position).getUsername();
        String email = getItem(position).getEmail();
        String extraContact = getItem(position).getExtraContact();
        final String downloadURL = getItem(position).getImageURL();
        final String buyerUID = getItem(position).getBuyerUID();

        //Create the person object with the information
        Book bk = new Book(book_name, author, price, iv, school, major, bookID, author,
                book_descrip, date, seller, status, username, email, extraContact, downloadURL, buyerUID);

        //create the view result for showing the animation
        final View result;

        //ViewHolder object
        BookListWithTransButtonAdapter.ViewHolder_withTrans holder;


        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder= new BookListWithTransButtonAdapter.ViewHolder_withTrans();
            holder.bookname = (TextView) convertView.findViewById(R.id.book_name);
            holder.seller = (TextView) convertView.findViewById(R.id.seller);
            holder.seller_email = (TextView) convertView.findViewById(R.id.email);
            holder.book_price = (TextView) convertView.findViewById(R.id.price);
            holder.book_image = (ImageView) convertView.findViewById(R.id.book_image);
            holder.cancel = (ImageButton) convertView.findViewById(R.id.cancelTranX);
            holder.finish = (ImageButton) convertView.findViewById(R.id.finishTranX);



            result = convertView;

            convertView.setTag(holder);
        }
        else{
            holder = (BookListWithTransButtonAdapter.ViewHolder_withTrans) convertView.getTag();
            result = convertView;
        }


        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.load_down_anim : R.anim.load_up_anim);
        result.startAnimation(animation);
        lastPosition = position;

        holder.bookname.setText(bk.getBook_name());
        holder.seller.setText(bk.getUsername());
        holder.seller_email.setText(bk.getEmail());
        holder.book_price.setText(String.valueOf(bk.getPrice()));
        holder.book_image.setImageBitmap(bk.getIv());
        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(school + " " + major + " " + bookID);
                // Red Dot
                db.collection("CountOnBuyers").document(user.getUid())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                int CountsOnBuyersAllTime = Integer.valueOf(documentSnapshot.get("CountsOnBuyersAllTime").toString());
                                CountsOnBuyersAllTime -= 1;
                                db.collection("CountOnBuyers").document(user.getUid()).update("CountsOnBuyersAllTime", CountsOnBuyersAllTime);
                            }
                        });
                // End Red Dot
                db.collection("Books").document(school).collection(major).document(bookID)
                        .update("Status", "A")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                db.collection("RecentlyAdded").document("Books")
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                ArrayList<String> titles = (ArrayList)documentSnapshot.get("RecentTitles");
                                                for(int i = 0; i < titles.size(); i++){
                                                    String[] segments = titles.get(i).split("_");
                                                    if(segments[2].equals(bookID)){
                                                        titles.set(i, school + "_" + major + "_" + bookID + "_" + "A");
                                                        break;
                                                    }
                                                }
                                                db.collection("RecentlyAdded").document("Books").update("RecentTitles", titles);
                                            }
                                        });
                                db.collection("Users").document(uid).collection("Sell").document(bookID).update("Status", "A");
                                db.collection("Users").document(uid).collection("Sell").document(bookID).update("BuyerUID", null);
                                db.collection("Users").document(uid).collection("Sell").document(bookID).update("Username", null);
                                db.collection("Users").document(uid).collection("Sell").document(bookID).update("Email", null);
                                db.collection("Users").document(buyerUID).collection("Buy").document(bookID).delete();
                                Toast.makeText(getContext(), "Order Has Been Cancelled", Toast.LENGTH_SHORT).show();
                                //TODO REFRESH LIST
                            }
                        });
            }
        });

        holder.finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Red Dot
                db.collection("CountOnBuyers").document(user.getUid())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                int CountsOnBuyersAllTime = Integer.valueOf(documentSnapshot.get("CountsOnBuyersAllTime").toString());
                                CountsOnBuyersAllTime -= 1;
                                db.collection("CountOnBuyers").document(user.getUid()).update("CountsOnBuyersAllTime", CountsOnBuyersAllTime);
                            }
                        });
                // End Red Dot
                db.collection("Books").document(school).collection(major).document(bookID)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                db.collection("Books").document(school).get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                Object temp_title = documentSnapshot.get("Titles");
                                                ArrayList titles = (ArrayList)temp_title;
                                                for(int i = 0; i < titles.size(); i++) {
                                                    String[] segments = titles.get(i).toString().split("_");
                                                    if (segments[2].equals(bookID)) {
                                                        titles.remove(i);
                                                    }
                                                }
                                                db.collection("Books").document(school).update("Titles", titles);
                                            }
                                        });
                                System.out.println(school + " " + major + " " + bookID);
                                Toast.makeText(getContext(), "Transaction Completed", Toast.LENGTH_SHORT).show();
                                db.collection("RecentlyAdded").document("Books")
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                ArrayList<String> titles = (ArrayList)documentSnapshot.get("RecentTitles");
                                                for(int i = 0; i < titles.size(); i++){
                                                    String[] segments = titles.get(i).split("_");
                                                    if(segments[2].equals(bookID)){
                                                        titles.remove(i);
                                                    }
                                                }
                                                db.collection("RecentlyAdded").document("Books").update("RecentTitles", titles);
                                            }
                                        });
                                db.collection("Users").document(uid).collection("Sell").document(bookID).delete();
                                if(status.equals("N")){
                                    db.collection("Users").document(buyerUID).collection("Buy").document(bookID).delete();
                                }
                                if(iv != null){
                                    StorageReference imageRef = storage.getReferenceFromUrl(downloadURL);
                                    imageRef.delete();
                                }
                                //TODO REFRESH LIST
                            }
                        });
            }
        });


        return convertView;
    }
}
