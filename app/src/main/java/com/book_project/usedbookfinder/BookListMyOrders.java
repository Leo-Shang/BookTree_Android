package com.book_project.usedbookfinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

public class BookListMyOrders extends ArrayAdapter<Book> {

    private Context mContext;
    private int mResource;
    private int lastPosition = -1;

    private static class ViewHolder {
        TextView bookname;
        TextView seller;
        TextView email;
        TextView extraContact;
        TextView book_price;
        ImageView book_image;
    }


    public BookListMyOrders(Context context, int resource, ArrayList<Book> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the persons information
        String book_name = getItem(position).getBook_name();
        String author = getItem(position).getEdition();
        String price = getItem(position).getPrice();
        Bitmap iv = getItem(position).getIv();
        String school = getItem(position).getSchool();
        String major = getItem(position).getCourse_num();
        String bookID = getItem(position).getBk_id();
        String book_descrip = getItem(position).getBook_descrip();
        Date date = getItem(position).getCreate_date();
        String seller = getItem(position).getSeller();
        String status = getItem(position).getStatus();
        String username = getItem(position).getUsername();
        String email = getItem(position).getEmail();
        String extraContact = getItem(position).getExtraContact();
        String downloadURL = getItem(position).getImageURL();
        String buyerUID = getItem(position).getBuyerUID();

        //Create the person object with the information
        Book bk = new Book(book_name, author, price, iv, school, major, bookID, author,
                book_descrip, date, seller, status, username, email, extraContact, downloadURL, buyerUID);

        //create the view result for showing the animation
        final View result;

        //ViewHolder object
        BookListMyOrders.ViewHolder holder;


        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder= new BookListMyOrders.ViewHolder();
            holder.bookname = (TextView) convertView.findViewById(R.id.book_name);
            holder.seller = (TextView) convertView.findViewById(R.id.seller);
            holder.email = (TextView) convertView.findViewById(R.id.email);
            holder.extraContact = (TextView) convertView.findViewById(R.id.extraContact);
            holder.book_price = (TextView) convertView.findViewById(R.id.price);
            holder.book_image = (ImageView) convertView.findViewById(R.id.book_image);

            result = convertView;

            convertView.setTag(holder);
        }
        else{
            holder = (BookListMyOrders.ViewHolder) convertView.getTag();
            result = convertView;
        }


        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.load_down_anim : R.anim.load_up_anim);
        result.startAnimation(animation);
        lastPosition = position;

        holder.bookname.setText(bk.getBook_name());
        holder.seller.setText(bk.getUsername());
        holder.email.setText(bk.getEmail());
        holder.extraContact.setText(bk.getExtraContact());
        holder.book_price.setText(String.valueOf(bk.getPrice()));
        holder.book_image.setImageBitmap(bk.getIv());

        return convertView;
    }
}
