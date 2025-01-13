package com.example.contacts;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;

public class ContactDetails extends AppCompatActivity{

    String phoneNumber, crrVal = "", defVal = "";

    Toolbar toolbar;
    LottieAnimationView loader;
    AppCompatImageView imageView, defImageView;
    TextView nameView, companyView;
    ImageButton btnEdit, btnBack, btnStar;

    LinearLayoutCompat btnCall, btnEmail, btnMsg, btnSetUp,listAreaPhones, listAreaEmails, conPhones, conEmails;

    int idGotten = Gadgets.DEF_ID, indexGotten = 0;
    String action;
    DBHelper dbHelper;
    MainActivity mainActivity;
    ContactModel crrContact;

    Dialog dialogOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        toolbar = findViewById(R.id.details_toolbar);
        loader = findViewById(R.id.details_loader);
        btnBack = findViewById(R.id.details_btn_back);
        btnStar = findViewById(R.id.details_btn_star);
        btnEdit = findViewById(R.id.details_btn_edit);
        imageView = findViewById(R.id.details_imageView);
        defImageView = findViewById(R.id.details_def_imageView);
        nameView = findViewById(R.id.details_name);
        companyView = findViewById(R.id.details_company);
        btnCall = findViewById(R.id.details_btn_call);
        btnEmail = findViewById(R.id.details_btn_email);
        btnMsg = findViewById(R.id.details_btn_msg);
        btnSetUp = findViewById(R.id.detail_btn_setUp);
        conPhones = findViewById(R.id.details_phones_con);
        conEmails = findViewById(R.id.details_emails_con);
        listAreaPhones = findViewById(R.id.list_area_phones);
        listAreaEmails = findViewById(R.id.list_area_emails);


        // get data
        mainActivity = MainActivity.getMainActivity(MainActivity.UNIQUE_ID);
        Intent intent = getIntent();
        idGotten = intent.getIntExtra(Gadgets.KEY_ID, Gadgets.DEF_ID);
        action = intent.getStringExtra(Gadgets.ACTION);
        if (action.equals(Gadgets.ACTION_UPDATE) || action.equals(Gadgets.ACTION_DONE)) {
            indexGotten = intent.getIntExtra(Gadgets.KEY_INDEX, 0);
            //Log.d("INDEX", "onCreate: index: " + indexGotten);
        }

        dbHelper = new DBHelper(this);
        crrContact = dbHelper.readRow(idGotten);


        // set toolbar // and see bellow
        setSupportActionBar(toolbar);
        btnBack.setOnClickListener(v -> onBackPressed());

        if (crrContact.isFav == 1) {
            btnStar.setImageResource(R.drawable.baseline_star_true_24);
        }
        btnStar.setOnClickListener(v -> {
            int favState = crrContact.isFav;
            if (favState == 0) {
                dbHelper.updateFav(crrContact.id, 1);
                crrContact.isFav = 1;

                btnStar.setImageResource(R.drawable.baseline_star_true_24);
                Toast.makeText(ContactDetails.this, "Added to favorites", Toast.LENGTH_SHORT).show();
            } else if (favState == 1) {
                dbHelper.updateFav(crrContact.id, 0);
                crrContact.isFav = 0;

                btnStar.setImageResource(R.drawable.baseline_star_false_24);
                Toast.makeText(ContactDetails.this, "Deleted to favorites", Toast.LENGTH_SHORT).show();
            }

            mainActivity.setFavContactCount();
        });




        // set data
        if (crrContact != null) {
            setData(crrContact);
        }



        // options actions
        btnCall.setOnClickListener(v -> {
            if (!cheakeDef(Gadgets.FOR_PHONE)) {
                setOptions(Gadgets.FOR_PHONE);
            }
        });
        btnMsg.setOnClickListener(v -> {
            if (!cheakeDef(Gadgets.FOR_MSG)) {
                setOptions(Gadgets.FOR_MSG);
            }
        });
        btnEmail.setOnClickListener(v -> {
            if (!cheakeDef(Gadgets.FOR_EMAIL)) {
                setOptions(Gadgets.FOR_EMAIL);
            }

        });


        btnEdit.setOnClickListener(v -> {
            Gadgets.showProgress(loader, Gadgets.DELAY_TIME_INTENT); // show progress bar

            Intent intent1 = new Intent(ContactDetails.this, EditContact.class);
            intent1.putExtra(Gadgets.KEY_ID, idGotten);
            intent1.putExtra(Gadgets.KEY_INDEX, indexGotten);
            intent1.putExtra(Gadgets.ACTION, Gadgets.ACTION_UPDATE);
            startActivity(intent1);
            finish();
        });


        // for checking only
        imageView.setOnClickListener(v -> Log.d("EMAILS", "Emails: " + crrContact.emails));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // set menu
        new MenuInflater(ContactDetails.this).inflate(R.menu.details_lay_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_btn_help) {
            Toast.makeText(this, "Help & feedback", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_btn_delete) { // delete
            dbHelper.deleteRow(idGotten);
            mainActivity.resetRecycler();
            mainActivity.setContactCount();
            mainActivity.setFavContactCount();
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void makePhoneCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(ContactDetails.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ContactDetails.this, new String[]{Manifest.permission.CALL_PHONE}, Gadgets.REQUEST_CODE_CALL);
        }

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);

        Gadgets.showProgress(loader, Gadgets.DELAY_TIME_CALL);
    }

    private void sendSms(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(ContactDetails.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ContactDetails.this, new String[]{Manifest.permission.SEND_SMS}, Gadgets.REQUEST_CODE_SMS);
        }

        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
        startActivity(smsIntent);

        Gadgets.showProgress(loader, Gadgets.DELAY_TIME_SMS);
    }
    private void sendEmail(String email) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + email));
        startActivity(emailIntent);

        Gadgets.showProgress(loader, Gadgets.DELAY_TIME_EMAIL);
    }

    public void setData(ContactModel contactModel) {
        // set image
        if (!contactModel.img.equals("")) {
            defImageView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(Gadgets.getImgBitmap(contactModel.img));
        }

        // set name
        String nameFull = contactModel.nameFirst + " " + contactModel.nameLast;
        nameView.setText(nameFull);

        // set company
        if (!contactModel.company.equals("")) {
            companyView.setVisibility(View.VISIBLE);
            companyView.setText(contactModel.company);
        } else {
            companyView.setVisibility(View.GONE);
        }

        // set phones and emails
        ArrayList<InputModel> arrayPhones = contactModel.getPhones();
        ArrayList<InputModel> arrayEmails = contactModel.getEmails();


        if (!arrayPhones.isEmpty()) {// visible phones areas
            listAreaPhones.setVisibility(View.VISIBLE);
        } else { // disable
            disableActionBtn(btnCall, R.drawable.baseline_phone_24_d_light);
            disableActionBtn(btnMsg, R.drawable.baseline_message_24_d_light);
            disableActionBtn(btnSetUp, R.drawable.baseline_set_up_24_d_light);
        }
        if (!arrayEmails.isEmpty()) {  // make visible emails areas
            btnEmail.setVisibility(View.VISIBLE);
            listAreaEmails.setVisibility(View.VISIBLE);
        }

        // set views/elements
        while (!arrayPhones.isEmpty()) {
            InputModel crrPhone = arrayPhones.remove(0);
            Log.d("VALUES", "setData: tag " + crrPhone.tag + "    val: " + crrPhone.value );
            setElement(ContactDetails.this, conPhones, crrPhone, Gadgets.FOR_PHONE);
        }
        while (!arrayEmails.isEmpty()) {
            InputModel crrEmail = arrayEmails.remove(0);
            setElement(ContactDetails.this, conEmails, crrEmail, Gadgets.FOR_EMAIL);
        }

    }

    public void setElement(Context context, ViewGroup root, InputModel valsModel, byte for_item) {

        LayoutInflater inflater = LayoutInflater.from(context);

        ConstraintLayout rootField = (ConstraintLayout) inflater.inflate(R.layout.details_list_item, root, false);
        LinearLayoutCompat contactLay = rootField.findViewById(R.id.details_list_contact_lay);
        TextView valueTV = rootField.findViewById(R.id.details_list_value);
        TextView labelTV = rootField.findViewById((R.id.details_list_label));
        LinearLayoutCompat iconCon = rootField.findViewById(R.id.details_list_icon_area);
        AppCompatImageButton icon = rootField.findViewById(R.id.details_list_icon);

        if (valsModel != null) {
            valueTV.setText(valsModel.value);
            labelTV.setText(valsModel.tag);
        }

        if (for_item == Gadgets.FOR_EMAIL) {
            // remove icon when email
            iconCon.setVisibility(View.GONE);

            contactLay.setOnClickListener(v -> {
                if(valsModel != null){
                    sendEmail(valsModel.value);
                }
            });
        } else {
            contactLay.setOnClickListener(v -> { // make call
                if(valsModel != null){
                    phoneNumber = valsModel.value;
                    makePhoneCall(phoneNumber);
                }
            });

            icon.setOnClickListener(v -> {
//                    Toast.makeText(ContactDetails.this, "Send SMS", Toast.LENGTH_SHORT).show();
                if(valsModel != null){
                    phoneNumber = valsModel.value;
                    sendSms(phoneNumber);
                }
            });
        }

        root.addView(rootField);

    }

    private void setOptions(byte for_item) {
        LayoutInflater inflater = LayoutInflater.from(ContactDetails.this);
        LinearLayoutCompat rootField = (LinearLayoutCompat) inflater.inflate(R.layout.details_action_options, findViewById(R.id.details_main_root), false);

        RadioGroup radioGroup = rootField.findViewById(R.id.radio_group);
        TextView titleView = rootField.findViewById(R.id.details_opt_title);
        AppCompatButton btnJustOnce = rootField.findViewById(R.id.details_btn_just_once);
        AppCompatButton btnAlways = rootField.findViewById(R.id.detail_btn_always);

        // add options
        if (crrContact != null) {
            ArrayList<InputModel> arrayData = new ArrayList<>();
            if (for_item == Gadgets.FOR_PHONE || for_item == Gadgets.FOR_MSG){
                arrayData = crrContact.getPhones();
            } else if (for_item == Gadgets.FOR_EMAIL || for_item == Gadgets.FOR_SET_UP) {
                arrayData = crrContact.getEmails();
                String title = "Choose email";
                titleView.setText(title);
            }


            if (arrayData.size() < 2) { // don't make dialog
                return;
            }

            while (!arrayData.isEmpty()) {
                InputModel crrModel = arrayData.remove(0);

                AppCompatRadioButton singleOption = (AppCompatRadioButton) inflater.inflate(R.layout.details_action_single_option, radioGroup, false);
                String text = crrModel.value + "\n" + crrModel.tag;
                singleOption.setText(text);

                singleOption.setOnClickListener(v -> {
                    crrVal = crrModel.value;

                    // change btn color
                    int color = getResources().getColor(R.color.blue);
                    btnJustOnce.setTextColor(color);
                    btnAlways.setTextColor(color);
                });

                radioGroup.addView(singleOption);
            }
        }

        // make action
        btnJustOnce.setOnClickListener(v -> {
            if (makeAction(crrVal, for_item)){
                dialogOptions.dismiss();
            }
        });
        btnAlways.setOnClickListener(v -> {
            if (makeAction(crrVal, for_item)){
                //set def value
                dbHelper.setDefValue(crrContact.id, for_item, crrVal);
                dialogOptions.dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(ContactDetails.this);
        builder.setView(rootField);

        dialogOptions = builder.create();
        dialogOptions.show();
    }


    private boolean makeAction(String value, int for_item) {
        if (value.equals("")) {
            return false;
        }

        boolean actionMaked = true;
        switch (for_item) {
            case Gadgets.FOR_PHONE:
                makePhoneCall(value);
                break;
            case Gadgets.FOR_MSG:
                sendSms(value);
                break;
            case Gadgets.FOR_EMAIL:
                sendEmail(value);
                break;
            default:
                actionMaked = false;
        }

        return actionMaked;
    }

    private boolean cheakeDef(int for_item) {
        ArrayList<InputModel> valueList = new ArrayList<>();
        if (for_item == Gadgets.FOR_PHONE || for_item == Gadgets.FOR_MSG) {
            valueList = crrContact.getPhones();
        } else if (for_item == Gadgets.FOR_EMAIL || for_item == Gadgets.FOR_SET_UP) {
            valueList = crrContact.getEmails();
        }

        String defVal = dbHelper.readDefValue(crrContact.id, for_item);
        if (valueList.size() == 1) { // if value = 1
            defVal = valueList.get(0).value;
        }

        return makeAction(defVal, for_item);
    }

    private void disableActionBtn(LinearLayoutCompat btnView, int iconRes) {
        AppCompatImageView iconView = (AppCompatImageView) btnView.getChildAt(0);
        TextView textVies = (TextView) btnView.getChildAt(1);

        btnView.setBackgroundResource(R.color.def_bg_color);
        iconView.setImageResource(iconRes);
        textVies.setTextColor(ContextCompat.getColor(ContactDetails.this, R.color.def_txt_color_light));
    }

}