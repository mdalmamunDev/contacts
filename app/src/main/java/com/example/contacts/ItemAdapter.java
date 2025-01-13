package com.example.contacts;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder>{

    public interface OnServicesButtonClickLitener {
        void onCallButtonClick(String phoneNumber);

        void onMsgButtonClick(String phoneNumber);
    }

    LinearLayoutCompat selectionActionsCon;
    TextView selectionCountView;
    private int defBg = R.drawable.button_on_press;
    private HashSet<Integer> selectedItems = new HashSet<>();
    private final OnServicesButtonClickLitener onServicesButtonClickLitener;
    private final Context context;
    private ArrayList<ContactModel> items;

    public ItemAdapter(Context context, ArrayList<ContactModel> items, OnServicesButtonClickLitener onServicesButtonClickLitener) {
        this.context = context;
        this.items = items;
        this.onServicesButtonClickLitener = onServicesButtonClickLitener;

        selectionActionsCon = ((MainActivity) context).findViewById(R.id.main_SelectionActionsCon);
        selectionCountView = ((MainActivity) context).findViewById(R.id.main_selectCount);
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_item_lay, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        int p = position;

        ContactModel contactModel = items.get(position);
        int crrId = contactModel.id;
        String nameFull = contactModel.nameFirst + " " + contactModel.nameLast;

        ///////////////////////////////////////////////////////////
        String phoneFirst = Gadgets.getPhone(contactModel, 0);


        // set bg color on contactInnerCon
        if (!selectedItems.contains(crrId)) {
            holder.contactInnerCon.setBackground(ContextCompat.getDrawable(holder.contactInnerCon.getContext(), defBg));
        } else {
            holder.contactInnerCon.setBackgroundColor(ContextCompat.getColor(holder.contactInnerCon.getContext(), R.color.blue_light));
        }

        // set img
        if (!contactModel.img.equals("")) {
            byte[] decodedBytes = Base64.decode(contactModel.img, Base64.DEFAULT);
            try {
                // create a file to save the decoded image
                File file = new File(context.getCacheDir(), "temp_contact_img "+ contactModel.getValues() + contactModel.id +"_______"+ Gadgets.getTime() +".jpg");
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(decodedBytes);
                outputStream.close();

                // load img
                Glide.with(context)
                        .load(file)
                        .into(holder.imgView);

            } catch (IOException e) {
                e.printStackTrace();
            }

//            Bitmap bitmap = BitmapFactory.decodeFile(contactModel.imgPath);
//            holder.imgView.setImageBitmap(bitmap);
        } else {
            holder.imgView.setImageResource(R.drawable.def_contact_img);
        }

        holder.nameView.setText(nameFull);
        holder.phoneView.setText(phoneFirst);

        // call
        holder.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onServicesButtonClickLitener.onCallButtonClick(phoneFirst);
            }
        });

        // sms
        holder.btnMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onServicesButtonClickLitener.onMsgButtonClick(phoneFirst);
            }
        });


        // for contactInnerCon click
        holder.contactInnerCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedItems.contains(crrId)) { // dis select
                    selectedItems.remove(crrId);
                    v.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.button_on_press));

                    // make action con visibility gone
                    if (selectedItems.isEmpty()) {
                        selectionActionsCon.setVisibility(View.GONE);
                    }
                } else if (!selectedItems.isEmpty()){ // select
                    selectedItems.add(crrId);
                    v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.blue_light));
                } else {
                     //go ContactDetails
                    Gadgets.showProgress(((MainActivity) context).loader, Gadgets.DELAY_TIME_INTENT); // show progress bar

                    Intent intent = new Intent(context, ContactDetails.class);
                    intent.putExtra(Gadgets.KEY_ID, crrId);
                    intent.putExtra(Gadgets.KEY_INDEX, p);
                    intent.putExtra(Gadgets.ACTION, Gadgets.ACTION_UPDATE);
                    context.startActivity(intent);
                }

                // set data
                selectionCountView.setText(String.valueOf(selectedItems.size()));
                if (selectedItems.size() == new DBHelper(context).read().size()) {
                    ((MainActivity) context).btnSelectAll.setImageResource(R.drawable.baseline_check_box_checked_24);
                } else {
                    ((MainActivity) context).btnSelectAll.setImageResource(R.drawable.baseline_check_box_outline_blank_24);
                }
            }
        });
        // long press select
        holder.contactInnerCon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectedItems.add(crrId);
                v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.blue_light));

                // set
                selectionCountView.setText(String.valueOf(selectedItems.size()));
                selectionActionsCon.setVisibility(View.VISIBLE);

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgView;
        AppCompatImageButton btnCall, btnMsg;
        public TextView nameView, phoneView;

        public LinearLayoutCompat contactInnerCon, contactDetails;

        public ViewHolder(View view) {
            super(view);
            contactInnerCon = view.findViewById(R.id.contact_innerCon);
            imgView = view.findViewById(R.id.contact_img);
            nameView = view.findViewById(R.id.contact_name);
            phoneView = view.findViewById(R.id.contact_phone);
            btnCall = view.findViewById(R.id.btn_call); // call
            btnMsg = view.findViewById(R.id.btn_msg); // msg
            contactDetails = view.findViewById(R.id.contact_details); // for details

        }
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setFilterList(ArrayList<ContactModel> filteredList) {
        items = filteredList;
        notifyDataSetChanged();
    }

    public void makeAllSelectedOrDeselected(int defBg, HashSet<Integer> selectedItems) {
        this.defBg = defBg;
        this.selectedItems = selectedItems;
    }

    public HashSet<Integer> getSelected() {
        return selectedItems;
    }


}
