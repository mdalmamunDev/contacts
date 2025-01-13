package com.example.contacts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Base64;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

public class Gadgets  extends AppCompatActivity {
    // permission
    public static final int REQUEST_CODE_CALL = 101;
    public static final int REQUEST_CODE_SMS = 102;
    public static final int REQUEST_CODE_PICK_IMG = 200;
    public static final int REQUEST_CODE_EXPORT = 300;
    public static final int REQUEST_CODE_IMPORT = 303;


    public static final String[] SORT_ITEMS = {"Sort by name", "Sort by date"};
    public static final String SORT_BY = "sort_by";
    public static final String ACTION = "action";
    public static final String ACTION_ADD = "add";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_DONE = "done";
    public static final String KEY_INDEX = "index";
    public static final byte FOR_PHONE = 1;
    public static final byte FOR_EMAIL = 2;
    public static final byte FOR_MSG = 3;
    public static final byte FOR_SET_UP = 4;
    public static final int DELAY_TIME_INTENT = 1000;
    public static final int DELAY_TIME_CALL = 2000;
    public static final int DELAY_TIME_SMS = 4000;
    public static final int DELAY_TIME_EMAIL = 3000;


    // input
    public static final int INPUT_LIMIT = 20;
    public static final String[] INPUT_LABELS_PHONES = {"No label", "Mobile", "Work", "Home", "Main", "Work Fax", "Pager", "Other"};
    public static final String[] INPUT_LABELS_EMAILS = {"No label", "Email", "Work", "Home", "Main", "Work Fax", "Pager", "Other"};
    public static final int SPINNER_DEF_SELECTION = 1;
    public static final int REMOVE_DURATION = 100;



    // dadaBase
    public static final String DB_NAME = "context_db";
    public static final int DB_VERSION = 5;
    public static final String TABLE_CONTEXT = "contexts_table";
    public static final String KEY_ID = "id";
    public static final int DEF_ID = 0;
    public static final String KEY_NAME_FIRST = "name_first";
    public static final String KEY_NAME_LAST = "name_last";
    public static final String KEY_COMPANY = "company";
    public static final String KEY_PHONES = "phones";
    public static final String KEY_DEF_PHONE = "def_phone";
    public static final String KEY_EMAILS = "emails";
    public static final String KEY_DEF_EMAIL = "def_email";
    public static final String KEY_IMG = "img";
    public static final String KEY_IS_FAV = "is_fav";
    public static final int IMG_QUALITY = 20;


    //public static final int DEF_CONTACT_IMG = R.drawable.def_contact_img;

    //String imagePath = "/data/data/com.example.myapp/files/image.jpg";
    @SuppressLint("SdCardPath")
    //public static final String FILE_DIRECTORY_PATH = "/data/data/com.example.contacts/files/";


    public static ArrayList<ContactModel> getSortedContactsByName(DBHelper dbHelper) {
        return sortedContactsByName(dbHelper.read(), dbHelper);
    }
    public static ArrayList<ContactModel> getSortedContactsByName(ArrayList<ContactModel> contacts ,DBHelper dbHelper) {
        return sortedContactsByName(contacts, dbHelper);
    }
    public static int getTotalCount(DBHelper dbHelper) {
        return dbHelper.read().size();
    }

    public static String getPhone(ContactModel contact, int i) {
        if (!contact.getPhones().isEmpty() && contact.getPhones().size() > i) {
            return contact.getPhones().get(i).value;
        }

        return "";
    }


    public static String encodeVals(HashMap<Integer, InputModel> hashed_inputs) {
        return encodeVals(hashed_inputs, 0).toString();
    }

    private static StringBuilder encodeVals(HashMap<Integer, InputModel> hashed_inputs, int start) {
        if(start == hashed_inputs.size()) {
            return new StringBuilder();
        }


        StringBuilder sb = new StringBuilder();
        InputModel inputModel = hashed_inputs.get(start);
        assert inputModel != null;
        String tag = inputModel.tag;
        String value = inputModel.value;

        if(tag.equals("")) {
            tag = "No label";
        }

        if(value.equals("")) {  // if value is "" then it is a invalid values
            return encodeVals(hashed_inputs, ++start);
        } else {
            sb.append(tag).append(":").append(value).append(",");
        }

        return sb.append(encodeVals(hashed_inputs, ++start));
    }

    public static Bitmap getImgBitmap(String base64Image) {
        // Decode Base64 string to bitmap
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static String preparedPhotoStr(String data, int tagLen) {
        StringBuilder sb = new StringBuilder();
        int cout = tagLen;

        for (int i=0; i<data.length(); i++) {
            char c = data.charAt(i);

            if (c != '\n') {
                sb.append(c);
                cout++;
            }

            if (cout == 73) { // line end
                sb.append("\n ");
                cout = 0;
            }
        }

        return sb.toString();
    }

    public static void showProgress(LottieAnimationView loader, int delayTime) {
        loader.setVisibility(View.VISIBLE);

        // make delay
        Handler handler = new Handler();
        Runnable runnable = () -> loader.setVisibility(View.GONE);
        handler.postDelayed(runnable, delayTime);
    }

    public static void deleteContact(Context context, DBHelper dbHelper, int id) {
        // delete data from db
        dbHelper.deleteRow(id);
    }

    public static String getTime() {
        LocalDate date = null; LocalTime time = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            date = LocalDate.now();
            time = LocalTime.now();
        }

        return date + "_" + time;
    }




    private static ArrayList<ContactModel> sortedContactsByName(ArrayList<ContactModel> contacts , DBHelper dbHelper) {
        Trie contactDec = new Trie();
        ArrayList<ContactModel> sortedContacts = new ArrayList<>();

        // insert value in dec
        for (ContactModel contact : contacts) {
            String fullName = contact.nameFirst + " " + contact.nameLast;
            int id = contact.id;
            contactDec.insert(fullName, id);
        }

        // sort contacts
        ArrayList<Integer> sortedIDs = contactDec.getSortedIDs();
        for (int id : sortedIDs) {
            sortedContacts.add(dbHelper.readRow(id));
        }

        return sortedContacts;
    }


}
