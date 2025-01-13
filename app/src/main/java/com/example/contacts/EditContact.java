package com.example.contacts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class EditContact extends AppCompatActivity {

    MainActivity mainActivity;
    int idGotten, indexGotten;
    String action, tbTitle;
    Uri selectedImageUri;
    private String imgBase64 = "", preImgBase64 = "";

    //boolean[] isLayoutAdded = new boolean[Gadgets.INPUT_LIMIT];
    HashMap<Integer, InputModel> hashM_phones = new HashMap<>();
    HashMap<Integer, InputModel> hashM_emails = new HashMap<>();
    Toolbar toolbar;
    ImageButton inputImg, btnCancel;
    AppCompatImageView imgEditIcon;
    LinearLayoutCompat phonesCon, emailsCon;
    AppCompatButton btnSave;
    TextInputEditText inputFirstName, inputLastName, inputCompany;
    byte pCount = 0, eCont = 0;
    TextView toolbarTitle;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        toolbar = findViewById(R.id.edit_toolbar);
        btnCancel = findViewById(R.id.edit_btn_cancel);
        toolbarTitle = findViewById(R.id.edit_tool_bar_title);
        btnSave = findViewById(R.id.edit_btn_save);
        phonesCon = findViewById(R.id.phones_con);
        emailsCon = findViewById(R.id.emails_con);
        inputImg = findViewById(R.id.edit_input_img);
        imgEditIcon = findViewById(R.id.edit_img_edit_icon);
        inputFirstName = findViewById(R.id.edit_input_name_first);
        inputLastName = findViewById(R.id.edit_input_name_last);
        inputCompany = findViewById(R.id.edit_input_company);


        // set toolbar edit_lay_options // and see bellow
        setSupportActionBar(toolbar);


        mainActivity = MainActivity.getMainActivity(MainActivity.UNIQUE_ID);
        Intent intent = getIntent();
        action = intent.getStringExtra(Gadgets.ACTION);
        // get id and index
        idGotten = intent.getIntExtra(Gadgets.KEY_ID, Gadgets.DEF_ID);
        indexGotten = intent.getIntExtra(Gadgets.KEY_INDEX, 0);


        if (action.equals(Gadgets.ACTION_ADD)) {
            tbTitle = "Create contact";
        } else {
            tbTitle = "Edit contact";
        }
        toolbarTitle.setText(tbTitle);


        // Set data for update
        dbHelper = new DBHelper(EditContact.this);
        if (action.equals(Gadgets.ACTION_UPDATE)) {
            ContactModel contactGotten = dbHelper.readRow(idGotten);

            // set img
            preImgBase64 = contactGotten.img;
            if (!preImgBase64.equals("")) {
                inputImg.setImageBitmap(Gadgets.getImgBitmap(preImgBase64));
                inputImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imgEditIcon.setVisibility(View.VISIBLE);

                imgBase64 = preImgBase64;
            }

            // set others
            inputFirstName.setText(contactGotten.nameFirst);
            inputLastName.setText(contactGotten.nameLast);
            inputCompany.setText(contactGotten.company);

            ///////////////////////////////////////////////////////////
            ArrayList<InputModel> listPhones = contactGotten.getPhones();
            ArrayList<InputModel> listEmails = contactGotten.getEmails();

            for (int i=0; i<listPhones.size(); i++) {
                InputModel values = listPhones.get(i);
                hashM_phones.put(i,values);

                setInputField(this, phonesCon, R.string.phone, InputType.TYPE_CLASS_PHONE, pCount++, hashM_phones,
                        Gadgets.INPUT_LABELS_PHONES, new boolean[Gadgets.INPUT_LIMIT], Gadgets.SPINNER_DEF_SELECTION, values);
            }
            for (int i=0; i<listEmails.size(); i++) {
                InputModel values = listEmails.get(i);
                hashM_emails.put(i, values);

                setInputField(this, emailsCon, R.string.email, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, eCont++, hashM_emails,
                        Gadgets.INPUT_LABELS_EMAILS, new boolean[Gadgets.INPUT_LIMIT], Gadgets.SPINNER_DEF_SELECTION, values);
            }

        }

        // phones and emails
        setInputField(this, phonesCon, R.string.phone, InputType.TYPE_CLASS_PHONE, pCount++, hashM_phones,
                Gadgets.INPUT_LABELS_PHONES, new boolean[Gadgets.INPUT_LIMIT], Gadgets.SPINNER_DEF_SELECTION, null);
        setInputField(this, emailsCon, R.string.email, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, eCont++, hashM_emails,
                Gadgets.INPUT_LABELS_EMAILS, new boolean[Gadgets.INPUT_LIMIT], Gadgets.SPINNER_DEF_SELECTION, null);


        inputImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.putExtra("fileName", "Filename");
                startActivityForResult(galleryIntent, Gadgets.REQUEST_CODE_PICK_IMG);
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                ContactModel crrContact;

                // Get Text
                String iNameFirst = Objects.requireNonNull(inputFirstName.getText()).toString();;
                String iNameLast = Objects.requireNonNull(inputLastName.getText()).toString();
                String iPhones = Gadgets.encodeVals(hashM_phones);
                String iEmails =  Gadgets.encodeVals(hashM_emails);
                String iCompany = Objects.requireNonNull(inputCompany.getText()).toString();;


                if (iNameFirst.equals("") || iPhones.equals("")) {
                    return;
                }

                // def values set / reset
                dbHelper.setDefValue(idGotten, Gadgets.FOR_PHONE, "");
                dbHelper.setDefValue(idGotten, Gadgets.FOR_EMAIL, "");


                if (selectedImageUri != null) {
                    // save crr img
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(EditContact.this.getContentResolver(), selectedImageUri);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, Gadgets.IMG_QUALITY, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        imgBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (action.equals(Gadgets.ACTION_ADD)) { // add new
                    idGotten++; // current last id
                    indexGotten++;// current last index

                    dbHelper.insert(iNameFirst, iNameLast, iPhones, iEmails,iCompany, imgBase64);
                    // get last contact from db for Add
                    crrContact = dbHelper.readRow(idGotten);

                    // set contact count
                    mainActivity.setContactCount();

                } else { // update // (action.equals(MainActivity.ACTION_UPDATE))
                    dbHelper.updateRow(idGotten, iNameFirst, iNameLast, iPhones, iEmails, iCompany, imgBase64);
                    // get last contact from db for Add
                    crrContact = dbHelper.readRow(idGotten);
                }

                // reset ArrList contacts
                mainActivity.resetRecycler();
                Toast.makeText(EditContact.this, "Saved", Toast.LENGTH_SHORT).show();

                // Go Details
                Intent intent = new Intent(EditContact.this, ContactDetails.class);
                intent.putExtra(Gadgets.KEY_ID, crrContact.id);
                intent.putExtra(Gadgets.KEY_INDEX, indexGotten);
                intent.putExtra(Gadgets.ACTION, Gadgets.ACTION_DONE);
                startActivity(intent);
                finish();
            }
        });



        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

//        inputImg.setOnClickListener(new View.OnClickListener() {  // this is for just check look up
//            @Override
//            public void onClick(View v) {
//                Log.d("HASH_MAP", "onClick: " + hashM_phones.size());
//                for (int i = 0; i < hashM_emails.size(); i++) {
//                    InputModel inputModel = hashM_emails.get(i);
//                    assert inputModel != null;
//                    Log.d("PHONES", "onCreate: p_" + i + "    Label: " + inputModel.tag + ":" + inputModel.value);
//                }
//            }
//        });



    } /************ End of onCreate() ************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.edit_lay_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_btn_help) {
            Toast.makeText(this, "Help & feedback", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == Gadgets.REQUEST_CODE_PICK_IMG && data != null) {
            selectedImageUri = data.getData();
            inputImg.setImageURI(selectedImageUri);
            inputImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgEditIcon.setVisibility(View.VISIBLE);

            // save img in storage
//            data.getStringExtra()
//            imageFileName = "image_" + System.currentTimeMillis() + ".jpg";
//            FileOutputStream outputStream;
//            try {
//                outputStream = openFileOutput(imageFileName, Context.MODE_PRIVATE);
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outputStream);
//                outputStream.flush();
//                outputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }


    private void setInputField(Context context, ViewGroup root, int hintRes, int inputType, int n, HashMap<Integer, InputModel> hashM_inputs,
                               String[] inputLabels, boolean[] isLayoutAdded, int defLabelIndx, InputModel values) {

        String v = "";
        defLabelIndx = labelIndx(hashM_inputs, inputLabels, defLabelIndx, 1);
        if(values != null){
            v = values.value;
            defLabelIndx = labelIndx(values.tag, inputLabels,defLabelIndx, 0);
            isLayoutAdded[n] = true;
        }

        LayoutInflater inflater = LayoutInflater.from(context);

        LinearLayoutCompat rootField = (LinearLayoutCompat) inflater.inflate(R.layout.edit_single_field, root, false);
            LinearLayoutCompat rootRight = (LinearLayoutCompat) rootField.getChildAt(1);

        // middle
        LinearLayoutCompat rootMiddle = (LinearLayoutCompat) rootField.getChildAt(0);

            TextInputLayout inputLayout = (TextInputLayout) rootMiddle.getChildAt(0);
                inputLayout.setHint(hintRes);

                    TextInputEditText  editText = (TextInputEditText ) inputLayout.findViewById(R.id.text_input_view);
                        editText.setInputType(inputType);
                        editText.setText(v);

                            addTextChLsnr(editText, root, hintRes, inputType, rootField, rootRight, n, hashM_inputs, inputLabels, isLayoutAdded, defLabelIndx);

            TextInputLayout labelLayout = (TextInputLayout) rootMiddle.getChildAt(1);
                AutoCompleteTextView spinner = (AutoCompleteTextView) labelLayout.findViewById(R.id.spinner_input);
                    // set label
                    spinner.setText(inputLabels[defLabelIndx]);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(EditContact.this, android.R.layout.simple_spinner_dropdown_item, inputLabels);
                    spinner.setAdapter(adapter);

                    spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            CheckedTextView v = (CheckedTextView) view;
                            String label = v.getText().toString(); // taking label / tag

                            InputModel inputModel = new InputModel();
                            inputModel.tag = label;

                            try {
                                InputModel crrModel = Objects.requireNonNull(hashM_inputs.get(n));
                                if (!crrModel.value.equals("")) {
                                    // get and set tag
                                    inputModel.value = crrModel.value;
                                }
                            }
                            catch(Exception e) {
                                Log.d("InputModel", "onItemClick: crrModel exception:" + e);
                            }

                            hashM_inputs.put(n, inputModel);
                        }
                    });
        // right
        if (isLayoutAdded[n]) {
            rootRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteField(rootField, hashM_inputs, n);
                }
            });

            AppCompatImageView rightIconView = (AppCompatImageView) rootRight.getChildAt(0);
            rightIconView.setImageResource(R.drawable.baseline_cross);
        }

        root.addView(rootField);

    }

    private void addTextChLsnr(TextInputEditText editText, ViewGroup root, int hintRes, int inputType, LinearLayoutCompat parentOfRootRight , LinearLayoutCompat rootRight, int n, HashMap<Integer, InputModel> hashM_inputs, String[] inputLabels, boolean[] isLayoutAdded, int defLabelIndx) {

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Log.d("CharSequence", "beforeTextChanged: CharSequence: " + s);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("CharSequence", "onTextChanged: CharSequence: " + s);


                String tag = inputLabels[defLabelIndx];
                String value = s.toString();

                try {
                    InputModel crrModel = Objects.requireNonNull(hashM_inputs.get(n));
                    if (!crrModel.tag.equals("")) {
                        // get tag
                        tag = crrModel.tag;
                    }
                }
                catch(Exception e) {
                    Log.d("InputModel", "onItemClick: crrModel exception:" + e);
                }

                hashM_inputs.put(n, new InputModel(tag, value));
                Log.d("hashMap_inputs", "onTextChanged: Text is HashMap at: " + n + ",  " + tag + ": "+ value);

                if (s.length() > 0 && !isLayoutAdded[n]) {
                    int newDefL = defLabelIndx + 1;

                    if (newDefL != inputLabels.length){
                        setInputField(EditContact.this, root, hintRes, inputType, n + 1, hashM_inputs, inputLabels, isLayoutAdded, newDefL, null);
                    }else{
                        setInputField(EditContact.this, root, hintRes, inputType, n + 1, hashM_inputs, inputLabels, isLayoutAdded, defLabelIndx, null);
                    }

                    isLayoutAdded[n] = true;

                    rootRight.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteField(parentOfRootRight, hashM_inputs, n);
                        }
                    });
                    AppCompatImageView rightIconView = (AppCompatImageView) rootRight.getChildAt(0);
                    rightIconView.setImageResource(R.drawable.baseline_cross);

                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            //    Log.d("CharSequence", "afterTextChanged: CharSequence: " + s.toString());
            }
        });
    }


    private void deleteField(LinearLayoutCompat rootField, HashMap<Integer, InputModel> hashM_inputs, int n) {
        rootField.animate()
                .alpha(0f)
                .setDuration(Gadgets.REMOVE_DURATION)
                .y(0f)
                .withEndAction(() -> rootField.setVisibility(View.GONE))
                .start();

        // reset value
        hashM_inputs.put(n, new InputModel());
    }
    
    private int labelIndx(String label, String[] inputLabels, int index, int i) {
        if (i == inputLabels.length) {
            return index;
        }

        if (inputLabels[i].equals(label)) {
            return i;
        }

        return labelIndx(label, inputLabels, index, ++i);
    }

    private int labelIndx(HashMap<Integer, InputModel> hashM_inputs, String[] inputLabels, int indx, int crrIndx) {
        if (crrIndx == inputLabels.length) {
            return indx;
        }

        if (!labelIsPresent(hashM_inputs, inputLabels[crrIndx], 0)) {
            return crrIndx;
        }

        return labelIndx(hashM_inputs, inputLabels, indx, ++crrIndx);
    }

    private boolean labelIsPresent(HashMap<Integer, InputModel> hashM_inputs, String label, int i) {
        if (i == hashM_inputs.size()) {
            return false;
        }

        if (Objects.requireNonNull(hashM_inputs.get(i)).tag.equals(label)) {
            return true;
        }

        return labelIsPresent(hashM_inputs, label, ++i);
    }
}