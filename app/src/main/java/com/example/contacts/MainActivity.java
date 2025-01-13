package com.example.contacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.navigation.NavigationView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements Serializable, ItemAdapter.OnServicesButtonClickLitener {
    String phoneNumber = "";
    boolean isDrawerOpen, isFavContactsOpen;
    AppCompatImageButton btnMenu, btnSelectAll, btnDeleteSelected, btnExportSelected;
    SearchView searchView;
    LottieAnimationView loader;
    RecyclerView recyclerView;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    LinearLayoutCompat navBtnContacts, navBtnFavContacts, navBtnImport, navBtnExport, navBtnHelp, selectionActionsCon;
    TextView navContactCount, navFavContactCount, selectionCount;
    Spinner navSpinnerSort;
    DBHelper dbHelper;
    ImageButton btnAdd;
    SharedPreferences sharedPreferences;
    public ArrayList<ContactModel> contacts, filteredList, contactsExport;
    ItemAdapter adapter;
    public static String UNIQUE_ID = UUID.randomUUID().toString();
    private static final HashMap<String, MainActivity> instances = new HashMap<>(); /////////////// add final


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Taking permissions from user
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, Gadgets.REQUEST_CODE_CALL);
        }
        // Request SEND_SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, Gadgets.REQUEST_CODE_SMS);
        }
        // Request for read external storage
//      //  if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Gadgets.REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
//        } else {
//            Toast.makeText(this, "Write permission granted", Toast.LENGTH_SHORT).show();
//        }

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);


        drawerLayout = findViewById(R.id.drawerLay);
        navigationView = findViewById(R.id.navigationView);
        navBtnContacts = findViewById(R.id.nav_btn_contacts);
        navContactCount = findViewById(R.id.nav_contact_count);
        navBtnFavContacts = findViewById(R.id.nav_btn_fav_contacts);
        navFavContactCount = findViewById(R.id.nav_fav_contact_count);
        navBtnImport = findViewById(R.id.nav_btn_import);
        navBtnExport = findViewById(R.id.nav_btn_export);
        navSpinnerSort = findViewById(R.id.nav_spinner_sort);
        navBtnHelp = findViewById(R.id.nav_btn_help);

        btnMenu = findViewById(R.id.main_menuBtn);
        searchView = findViewById(R.id.main_search_view);
        loader = findViewById(R.id.main_loader);
        recyclerView = findViewById(R.id.recyclerView);
        selectionActionsCon = findViewById(R.id.main_SelectionActionsCon);
        btnSelectAll = findViewById(R.id.main_selectAllBtn);
        selectionCount = findViewById(R.id.main_selectCount);
        btnDeleteSelected = findViewById(R.id.main_btnSelectDelete);
        btnExportSelected = findViewById(R.id.main_btnSelectExport);
        btnAdd = findViewById(R.id.btn_add);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contacts = new ArrayList<>();
        instances.put(UNIQUE_ID, this);


        // Database
        dbHelper = new DBHelper(this);
//        //SQLiteDatabase db = dbHelper.getWritableDatabase();
//       // dbHelper.onUpgrade(db, Gadgets.DB_VERSION, Gadgets.DB_VERSION);
        contacts = dbHelper.read();



        adapter = new ItemAdapter(this, getContacts(), this);
        recyclerView.setAdapter(adapter);


//        for (int i = 0; i <= 20; i++) {
//            contacts.add(new ContactModel(i ,"Md. AL", "Mamun" + i, "Home:01796031589,", "Home:mdalmamun@gmail.com", "TISI", "", 0));
//        }

        // search view
        btnMenu.setOnClickListener(v -> openDrawer());

        filteredList = new ArrayList<>();

        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<ContactModel> c = dbHelper.read(); // all contacts
                if (isFavContactsOpen) {
                    c = dbHelper.readFavs(); // favs only
                }

                filteredList.clear();
                for (ContactModel contact : c) {
                    if (contact.getValues().toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(contact);
                    }
                }
                adapter.setFilterList(filteredList);
                return true;
            }
        });




        //Selection
        //select all
        btnSelectAll.setOnClickListener(v -> {
            HashSet<Integer> selectedItems = new HashSet<>();
            boolean isAllSelected = adapter.getSelected().size() == dbHelper.read().size();
            boolean isAnySelected = !adapter.getSelected().isEmpty();

            if (!isAllSelected && isAnySelected) { // not selected
                int n = Gadgets.getTotalCount(dbHelper);

                for (ContactModel contact : dbHelper.read()) { // take all id
                    selectedItems.add(contact.id);
                }

                adapter.makeAllSelectedOrDeselected(R.color.blue_light, selectedItems);

                Toast.makeText(MainActivity.this, Gadgets.getTotalCount(dbHelper) + " contacts are selected", Toast.LENGTH_SHORT).show();
                btnSelectAll.setImageResource(R.drawable.baseline_check_box_checked_24);
                selectionCount.setText(String.valueOf(n));
            } else { // selected
                adapter.makeAllSelectedOrDeselected(R.drawable.button_on_press, selectedItems);
                btnSelectAll.setImageResource(R.drawable.baseline_check_box_outline_blank_24);
                selectionActionsCon.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
        });
        // delete items
        btnDeleteSelected.setOnClickListener(v -> {
            HashSet<Integer> selectedItemsSet = adapter.getSelected();
            for (int crrId : selectedItemsSet) {
                // delete contact
                Gadgets.deleteContact(MainActivity.this, dbHelper, crrId);
            }

            Toast.makeText(MainActivity.this, "All contacts are deleted", Toast.LENGTH_SHORT).show();
            // reset
            selectionActionsCon.setVisibility(View.GONE); // set visibility gone for SelectionActionsCon
            adapter.setFilterList(getContacts());
            setContactCount();
            setFavContactCount();
        });
        // export selected
        btnExportSelected.setOnClickListener(v -> {
            ArrayList<ContactModel> contactsSelected = new ArrayList<>();
            HashSet<Integer> selectedItemsSet = adapter.getSelected();

            for (int crrId : selectedItemsSet) { // add contactsSelected
                ContactModel contact = dbHelper.readRow(crrId);
                contactsSelected.add(contact);
            }

            exportContacts(contactsSelected);
        });




        // Drawer
        // set contact count
        setContactCount();
        setFavContactCount();
        // set sort spinner
        ArrayAdapter<String> adapterSpnr = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Gadgets.SORT_ITEMS);
        navSpinnerSort.setAdapter(adapterSpnr);
        if (getShortStatus().equals(Gadgets.SORT_ITEMS[1])) { // date
            navSpinnerSort.setSelection(1);
        }

        navBtnContacts.setOnClickListener(v -> {
            if (isFavContactsOpen) {
                adapter.setFilterList(getContacts()); // load all contacts
                isFavContactsOpen = false;
            }
            closeDrawer();
        });
        navBtnFavContacts.setOnClickListener(v -> {
            adapter.setFilterList(getContacts(dbHelper.readFavs()));
            Toast.makeText(MainActivity.this, "Favorites Contacts", Toast.LENGTH_SHORT).show();

            closeDrawer();
            isFavContactsOpen = true;
        });
        navBtnImport.setOnClickListener(v -> importFilePicker());
        navBtnExport.setOnClickListener(v -> exportContacts(dbHelper.read()));
        navSpinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Gadgets.SORT_BY, Gadgets.SORT_ITEMS[position]);
                editor.apply();

                if (isFavContactsOpen) { // fav was opened
                    adapter.setFilterList(getContacts(dbHelper.readFavs()));
                } else { // normal mode
                    adapter.setFilterList(getContacts());
                }

                closeDrawer();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        // add btn
        btnAdd.setOnClickListener(v -> {
            int lastId = 0;
            int lastIndex = 0;
            if (!contacts.isEmpty()) {
                lastId = contacts.get(contacts.size() - 1).id;
                lastIndex = contacts.size() - 1;
            }
            Gadgets.showProgress(loader, Gadgets.DELAY_TIME_INTENT); // show progress bar

            Intent intent = new Intent(MainActivity.this, EditContact.class);
            intent.putExtra(Gadgets.ACTION, Gadgets.ACTION_ADD);
            intent.putExtra(Gadgets.KEY_ID, lastId);
            intent.putExtra(Gadgets.KEY_INDEX, lastIndex);
            startActivity(intent);
        });



    }    /******         end of onCreate         *******/



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gadgets.REQUEST_CODE_IMPORT && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            importContacts(uri);
        }

        if (requestCode == Gadgets.REQUEST_CODE_EXPORT && resultCode == RESULT_OK) {
            // Get the Uri of the selected file and write to it
            Uri uri = data.getData();
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                String exData = exportData();
                outputStream.write(exData.getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBackPressed() {
        if(!adapter.getSelected().isEmpty()){ // if any item have selected
            adapter.makeAllSelectedOrDeselected(R.drawable.button_on_press, new HashSet<>());
            adapter.notifyDataSetChanged();
            selectionActionsCon.setVisibility(View.GONE);
        }else if (isFavContactsOpen) {
            openDrawer();
            adapter.setFilterList(getContacts());
            isFavContactsOpen = false;
        } else if (isDrawerOpen) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instances.remove(UNIQUE_ID);
    }

    @Override
    public void onCallButtonClick(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        makePhoneCall(phoneNumber);
    }

    @Override
    public void onMsgButtonClick(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        sendSms(phoneNumber);
    }

    private void makePhoneCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, Gadgets.REQUEST_CODE_CALL);
        }

        Gadgets.showProgress(loader, Gadgets.DELAY_TIME_CALL);

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }

    private void sendSms(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, Gadgets.REQUEST_CODE_SMS);
        }

        Gadgets.showProgress(loader, Gadgets.DELAY_TIME_SMS);

        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
        startActivity(smsIntent);
    }


    public void resetRecycler() {
        adapter.setFilterList(getContacts());
    }
    private ArrayList<ContactModel> getContacts() { // only for none fav
        return getContacts(new ArrayList<>());
    }
    private ArrayList<ContactModel> getContacts(ArrayList<ContactModel> favContacts) { // for all contacts (fav + none fav)
        ArrayList<ContactModel> contacts;

        if (getShortStatus().equals(Gadgets.SORT_ITEMS[0])) { // sort status is name
            if (!favContacts.isEmpty()) { // for fav
                contacts = Gadgets.getSortedContactsByName(favContacts, dbHelper);
            } else { // all contacts
                contacts = Gadgets.getSortedContactsByName(dbHelper);
            }
        } else { // sort status is date
            if (!favContacts.isEmpty()) {
                contacts = dbHelper.readFavs();
            } else {
                contacts = dbHelper.read();
            }
        }
        return contacts;
    }
    private String getShortStatus() {
        return sharedPreferences.getString(Gadgets.SORT_BY, "");
    }

    public static MainActivity getMainActivity(String uniqueId) {
        return instances.get(uniqueId);
    }

    private void openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
        isDrawerOpen = true;
    }

    private void closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START);
        isDrawerOpen = false;
    }

    public void setContactCount() {
        navContactCount.setText(String.valueOf(dbHelper.read().size()));
    }

    public void setFavContactCount() {
        navFavContactCount.setText(String.valueOf(dbHelper.readFavs().size()));
    }
    private void importFilePicker() { // pick file
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/x-vcard");
        startActivityForResult(intent, Gadgets.REQUEST_CODE_IMPORT);
    }

    private void exportContacts(ArrayList<ContactModel> contacts) {
        contactsExport = contacts; // update contacts

        // Create a new file with the desired name in the public directory
        String fileName = "contacts-"+ Gadgets.getTime() +".vcf";

        // Ask the user to select the directory where the file should be saved
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/x-vcard");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, Gadgets.REQUEST_CODE_EXPORT);
    }

    private void importContacts(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            StringBuilder nameLast = new StringBuilder();
            StringBuilder nameFirst = new StringBuilder();
            StringBuilder phones = new StringBuilder();
            StringBuilder emails = new StringBuilder();
            String company = "";
            StringBuilder img = new StringBuilder();
            boolean isImgOpen = false;

            while ((line = reader.readLine()) != null) {
                String vcTag = getVcTag(line);

                // name
                // name
                if (vcTag.equals("N")) {
                    boolean nameLastTaken = false;

                    for (int i = vcTag.length() + 1; i < line.length(); i++) {
                        if (line.charAt(i) == ';') {
                            if (nameLastTaken) { // already nameLast and nameFirst taken
                                break;
                            }
                            nameLastTaken = true;
                        } else if (!nameLastTaken) { // take nameLast
                            nameLast.append(line.charAt(i));
                        } else { // take nameFirs
                            nameFirst.append(line.charAt(i));
                        }
                    }

                    nameFirst = new StringBuilder(nameFirst.toString().replaceAll("'", "''"));
                }

                // mobiles
                else if (vcTag.contains("TEL")) {
                    int mTagStartIndx = 4;
                    phones.append(getVcValue(mTagStartIndx, vcTag, line));
                }

                // emails
                else if (vcTag.contains("EMAIL")) {
                    int eTagStartIndx = 6;
                    emails.append(getVcValue(eTagStartIndx, vcTag, line));

                }

                // company
                else if (vcTag.equals("ORG") && line.length() > vcTag.length() + 1) {
                    company = line.substring(vcTag.length() + 1);
                }

                // img
                else if (vcTag.contains("PHOTO;ENCODING=BASE64;JPEG")) {
                    img.append(line.substring(vcTag.length() + 1));
                    isImgOpen = true;
                }
                else if (isImgOpen && vcTag.equals("")) {
                    int start = 0;
                    if (line.length() != 0 && line.charAt(0) == ' '){ // if first space present avoid it
                        start = 1;
                    }
                    img.append(line.substring(start));
                }

                // end
                else if (vcTag.equals("END")) {
                    dbHelper.insert(nameFirst.toString(), nameLast.toString(), phones.toString(), emails.toString(), company, img.toString());
                    nameFirst = new StringBuilder();
                    nameLast = new StringBuilder();
                    phones = new StringBuilder();
                    emails = new StringBuilder();
                    company = "";
                    img = new StringBuilder();

                }
            }

            Toast.makeText(this, "All contacts imported successfully", Toast.LENGTH_SHORT).show();
            adapter.setFilterList(getContacts());
            setContactCount();
            closeDrawer();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getVcTag(String vcData) {
        StringBuilder vcTag = new StringBuilder();

        for (int i = 0; i < vcData.length(); i++) {
            if (vcData.charAt(i) == ':') {
                return vcTag.toString();
            }
            vcTag.append(vcData.charAt(i));
        }

        return "";
    }

    private static String getVcValue(int tagStartIndx, String vcTag, String line) {
        // int tagStartIndx = 4 for phone and 6 for email;
        String tag = "", value = "";

        if (vcTag.length() > tagStartIndx + 2 && vcTag.startsWith("X-", tagStartIndx)) {
            tagStartIndx += 2;
        }

        if (vcTag.length() > tagStartIndx) { // tag
            tag += vcTag.charAt(tagStartIndx);
            if (vcTag.length() > tagStartIndx + 1) {
                tag += vcTag.substring(tagStartIndx + 1).toLowerCase();
            }
        }

        if (line.length() > vcTag.length() + 1) { // value
            value = line.substring(vcTag.length() + 1);
        }

        if (tag.contains("Cell")) { // check tag
            tag = "Mobile";
        }

        return tag + ":" + value + ",";
    }

    private String exportData() {

        StringBuilder data = new StringBuilder();

        while (!contactsExport.isEmpty()) {
            ContactModel crrContact = contactsExport.remove(0);
            String nameFirst = crrContact.nameFirst;
            String nameLast = crrContact.nameLast;
            ArrayList<InputModel> crrPhones = crrContact.getPhones();
            ArrayList<InputModel> crrEmails = crrContact.getEmails();
            String company = crrContact.company;
            String img = Gadgets.preparedPhotoStr(crrContact.img, "PHOTO;ENCODING=BASE64;JPEG".length());

            data.append("BEGIN:VCARD\nVERSION:2.1\n");
            data.append("N:").append(nameLast).append(";").append(nameFirst).append(";;;\n");
            data.append("FN:").append(nameFirst).append(" ").append(nameLast).append("\n");
            for (int i = 0; i < crrPhones.size(); i++) {
                String tag = crrPhones.get(i).tag, number = crrPhones.get(i).value;
                if (tag.equalsIgnoreCase("mobile")) {
                    tag = "CELL";
                }

                data.append("TEL;").append(tag.toUpperCase()).append(":").append(number).append("\n");
            }
            for (int i = 0; i < crrEmails.size(); i++) {
                String tag = crrEmails.get(i).tag, email = crrEmails.get(i).value;
                if (tag.equalsIgnoreCase("email")) {
                    tag = "X-Email";
                }

                data.append("EMAIL;").append(tag.toUpperCase()).append(":").append(email).append("\n");
            }
            data.append("PHOTO;ENCODING=BASE64;JPEG:").append(img).append("\n");
            data.append("ORG:").append(company).append("\n");
            data.append("END:VCARD\n");
//            Log.d("PHOTO_PR", "exportData: " + "PHOTO;ENCODING=BASE64;JPEG:" + img + "\n");
        }
        //Log.d("PHOTO_PR", "exportData: " + data);

        return data.toString();
    }
}