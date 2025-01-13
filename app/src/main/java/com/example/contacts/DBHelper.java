package com.example.contacts;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    public String
            tableContext = Gadgets.TABLE_CONTEXT,
            key_id = Gadgets.KEY_ID,
            key_nameFirst = Gadgets.KEY_NAME_FIRST,
            key_nameLast = Gadgets.KEY_NAME_LAST,
            key_company = Gadgets.KEY_COMPANY,
            key_phones = Gadgets.KEY_PHONES,
            key_defPhone = Gadgets.KEY_DEF_PHONE,
            key_emails = Gadgets.KEY_EMAILS,
            key_defEmail = Gadgets.KEY_DEF_EMAIL,
            key_img = Gadgets.KEY_IMG,
            key_isFav = Gadgets.KEY_IS_FAV;

    public DBHelper(Context context) {
        super(context, Gadgets.DB_NAME, null, Gadgets.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREATE TABLE contexts_table (id INTEGER PRIMARY KEY, name_first TEXT, name_last TEXT, phone TEXT, email TEXT, company TEXT, img TEXT)
        db.execSQL("CREATE TABLE " + tableContext + " ("
                + key_id + " INTEGER PRIMARY KEY, "
                + key_nameFirst + " TEXT, "
                + key_nameLast + " TEXT,"
                + key_phones + " TEXT, "
                + key_defPhone + " TEXT, "
                + key_emails + " TEXT, "
                + key_defEmail + " TEXT, "
                + key_company + " TEXT, "
                + key_img +" TEXT,"
                + key_isFav + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + tableContext);
        onCreate(db);
    }

    // Insert Data
    public void insert(String nameFirst, String nameLast, String phones, String emails, String company, String img) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "INSERT INTO " + tableContext + " ("
                + key_nameFirst + ", "
                + key_nameLast + ", "
                + key_phones + ", "
                + key_emails + ", "
                + key_company + ", "
                + key_img + ") VALUES ( '"
                + nameFirst + "', '"
                + nameLast + "', '"
                + phones + "', '"
                + emails + "', '"
                + company + "', '"
                + img + "')";
        db.execSQL(sql);

        closeDB(db);
    }

    // Read Data
    public ArrayList<ContactModel> read() {
        int id, isFav;
        String nameFirst, nameLast, phones, emails, company, img;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableContext, null);

        ArrayList<ContactModel> arrContexts = new ArrayList<ContactModel>();

        while (cursor.moveToNext()) {
            id = cursor.getInt(0);
            nameFirst = cursor.getString(1);
            nameLast = cursor.getString(2);
            phones = cursor.getString(3);
            emails = cursor.getString(5);
            company = cursor.getString(7);
            img = cursor.getString(8);
            isFav = cursor.getInt(9);

            ContactModel contact = new ContactModel(id, nameFirst, nameLast, phones, emails, company, img, isFav);
            arrContexts.add(contact);
        }

        closeDB(db);
        return arrContexts;
    }
    public ArrayList<ContactModel> readFavs() {
        int id, isFav;
        String nameFirst, nameLast, phones, emails, company, img;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableContext, null);

        ArrayList<ContactModel> arrContexts = new ArrayList<ContactModel>();

        while (cursor.moveToNext()) {
            id = cursor.getInt(0);
            nameFirst = cursor.getString(1);
            nameLast = cursor.getString(2);
            phones = cursor.getString(3);
            emails = cursor.getString(5);
            company = cursor.getString(7);
            img = cursor.getString(8);
            isFav = cursor.getInt(9);

            if (isFav == 1) {
                ContactModel contact = new ContactModel(id, nameFirst, nameLast, phones, emails, company, img, isFav);
                arrContexts.add(contact);
            }
        }

        closeDB(db);
        return arrContexts;
    }
    public ContactModel readRow(int id) {
        String nameFirst = "", nameLast = "", phones = "", emails = "", company = "", img = "";
        int isFav = 0;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ tableContext +" WHERE " + this.key_id + " = " + id, null);

        if (cursor != null && cursor.moveToFirst()) {
            nameFirst = cursor.getString(1);
            nameLast = cursor.getString(2);
            phones = cursor.getString(3);
            emails = cursor.getString(5);
            company = cursor.getString(7);
            img = cursor.getString(8);
            isFav = cursor.getInt(9);
        }

        closeDB(db);
        return new ContactModel(id, nameFirst, nameLast, phones, emails, company, img, isFav);
    }
    public String readDefValue(int id, int for_item) {
        String value = "";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ tableContext +" WHERE " + this.key_id + " = " + id, null);

        if (cursor != null && cursor.moveToFirst()) {
            if (for_item == Gadgets.FOR_PHONE || for_item == Gadgets.FOR_MSG) {
                value = cursor.getString(4);
            } else if (for_item == Gadgets.FOR_EMAIL || for_item == Gadgets.FOR_SET_UP) {
                value = cursor.getString(6);
            }
        }

        closeDB(db);

        if (value == null) {
            return "";
        }
        return value;
    }

    // Update Data
    public void updateRow(int id, String nameFirst, String nameLast, String phones, String emails, String company, String img) {
        SQLiteDatabase db = getWritableDatabase();
        // "UPDATE contexts_table set nameFirst = 'nameFirst', phones = '25545'img = '0' WHERE id = 25"
        String sql = "UPDATE " + tableContext + " set "
                + key_nameFirst + " = '" + nameFirst + "', "
                + key_nameLast + " = '" + nameLast + "', "
                + key_phones + " = '" + phones + "', "
                + key_emails + " = '" + emails + "', "
                + key_phones + " = '" + phones + "', "
                + key_company + " = '" + company + "', "
                + key_img +" = '" + img +"' WHERE "
                + key_id + " = " + id;
        db.execSQL(sql);

        closeDB(db);
    }
    public void updateFav(int id, int favState) {
        SQLiteDatabase db = getWritableDatabase();
        // "UPDATE contexts_table set nameFirst = 'nameFirst', phones = '25545'img = '0' WHERE id = 25"
        String sql = "UPDATE " + tableContext + " set "
                + key_isFav +" = '" + favState +"' WHERE "
                + key_id + " = " + id;
        db.execSQL(sql);

        closeDB(db);
    }
    public void setDefValue(int id, byte for_item, String defValue) {
        String key = "";
        if (for_item == Gadgets.FOR_PHONE || for_item == Gadgets.FOR_MSG) {
            key = key_defPhone;
        } else if (for_item == Gadgets.FOR_EMAIL || for_item == Gadgets.FOR_SET_UP) {
            key = key_defEmail;
        }

        SQLiteDatabase db = getWritableDatabase();
        // "UPDATE contexts_table set key = 'defValue' WHERE id = 25"
        String sql = "UPDATE " + tableContext + " set "
                +key +" = '" + defValue +"' WHERE "
                + key_id + " = " + id;
        db.execSQL(sql);

        closeDB(db);
    }


    // Delete
    public void deleteRow(int id) {
        //DELETE FROM users WHERE id = 1;
        SQLiteDatabase db = getWritableDatabase();
        String sql = "DELETE FROM " + tableContext + " WHERE " + key_id + " = " + id;
        db.execSQL(sql);

        closeDB(db);
    }

    private void closeDB(SQLiteDatabase db) {
        db.close();
    }
}
