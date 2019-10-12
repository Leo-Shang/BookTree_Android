package com.book_project.usedbookfinder;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import io.opencensus.internal.StringUtil;

public class Book {
    private String book_name;
    private String edition;
    private String price;
    private Bitmap iv;
    private String school;
    private String course_num;
    private String bk_id;
    private String author;
    private String book_descrip;
    private Date create_date;
    private String seller;
    private String status;
    private String username;
    private String email;
    private String extraContact;
    private String buyerUID;

    public String getBuyerUID() {
        return buyerUID;
    }

    public void setBuyerUID(String buyerUID) {
        this.buyerUID = buyerUID;
    }



//    protected Book(Parcel in) {
//        book_name = in.readString();
//        edition = in.readString();
//        price = in.readString();
//        iv = in.readParcelable(Bitmap.class.getClassLoader());
//        school = in.readString();
//        course_num = in.readString();
//        bk_id = in.readString();
//        author = in.readString();
//        book_descrip = in.readString();
//        seller = in.readString();
//        status = in.readString();
//        username = in.readString();
//        email = in.readString();
//        extraContact = in.readString();
//        imageURL = in.readString();
//    }

//    public static final Creator<Book> CREATOR = new Creator<Book>() {
//        @Override
//        public Book createFromParcel(Parcel in) {
//            return new Book(in);
//        }
//
//        @Override
//        public Book[] newArray(int size) {
//            return new Book[size];
//        }
//    };

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    private String imageURL;

    public String getBook_name() {
        return book_name;
    }

    public void setBook_name(String book_name) {
        this.book_name = book_name;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Bitmap getIv() {
        return iv;
    }

    public void setIv(Bitmap iv) {
        this.iv = iv;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getCourse_num() {
        return course_num;
    }

    public void setCourse_num(String course_num) {
        this.course_num = course_num;
    }

    public String getBk_id() {
        return bk_id;
    }

    public void setBk_id(String bk_id) {
        this.bk_id = bk_id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBook_descrip() {
        return book_descrip;
    }

    public void setBook_descrip(String book_descrip) {
        this.book_descrip = book_descrip;
    }

    public Date getCreate_date() {
        return create_date;
    }

    public void setCreate_date(Date create_date) {
        this.create_date = create_date;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExtraContact() {
        return extraContact;
    }

    public void setExtraContact(String extraContact) {
        this.extraContact = extraContact;
    }

    public static String convertToTitleCaseIteratingChars(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }



    public Book(String book_name, String edition, String price, Bitmap iv, String school,
                String course_num, String bk_id, String author, String book_descrip, Date create_date,
                String seller, String status, String username, String email, String extraContact, String imageURL, String buyerUID) {
        this.book_name = convertToTitleCaseIteratingChars(book_name);
        this.edition = edition;
        this.price = price.toUpperCase();
        this.iv = iv;
        this.school = school;
        if(course_num != null)
            this.course_num = course_num.toUpperCase();
        else
            this.course_num = null;
        this.bk_id = bk_id;
        this.author = convertToTitleCaseIteratingChars(author);
        this.book_descrip = book_descrip;
        this.create_date = create_date;
        this.seller = seller;
        this.status = status;
        this.username = username;
        this.email = email;
        this.extraContact = extraContact;
        this.imageURL = imageURL;
        this.buyerUID = buyerUID;
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel parcel, int i) {
//        parcel.writeString(book_name);
//        parcel.writeString(edition);
//        parcel.writeString(price);
//        parcel.writeParcelable(iv, i);
//        parcel.writeString(school);
//        parcel.writeString(course_num);
//        parcel.writeString(bk_id);
//        parcel.writeString(author);
//        parcel.writeString(book_descrip);
//        parcel.writeSerializable(create_date);
//        parcel.writeString(seller);
//        parcel.writeString(status);
//        parcel.writeString(username);
//        parcel.writeString(email);
//        parcel.writeString(extraContact);
//    }
}
