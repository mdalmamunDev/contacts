package com.example.contacts;

import java.util.ArrayList;

public class ContactModel {

    char[] invalidPhoneChars = {'(', ')', '[', ']', '{', '}', '<', '>', ';', '/', '\\', '|', '?', '%', '=', '^'}; //edited

    char[] invalidEmailChars = {' ', '\"', '\'', '(', ')', '[', ']', '{', '}', '<', '>', ';', '/', '\\', '|', '?', '#', '%', '*', '+', '=', '^'}; //edited


    int id, isFav = 0;
    String nameFirst = "", nameLast = "", phones = "", emails = "", company = "", img = "";

    public ContactModel(int id, String nameFirst, String nameLast, String phones, String emails, String company, String img) {
        this.id = id;
        this.nameFirst = nameFirst;
        this.nameLast = nameLast;
        this.phones = phones;
        this.emails = emails;
        this.company = company;
        this.img = img;

    }
    public ContactModel(int id, String nameFirst, String nameLast, String phones, String emails, String company, String img, int isFav) {
        this.id = id;
        this.nameFirst = nameFirst;
        this.nameLast = nameLast;
        this.phones = phones;
        this.emails = emails;
        this.company = company;
        this.img = img;
        this.isFav = isFav;

    }
    public ContactModel() {
        // default constructor
    }


    public String getValues() {
        return nameFirst + " " + nameLast + phones;
    }

    public ArrayList<InputModel> getPhones(){
        return decodeValues(phones, invalidPhoneChars);
    }
    public ArrayList<InputModel> getEmails(){
        return decodeValues(emails, invalidEmailChars);
    }

    private ArrayList<InputModel> decodeValues(String encodedVals, char[] invalidChars) {
        ArrayList<InputModel> arrDecoded = new ArrayList<InputModel>();
        StringBuilder temp = new StringBuilder();
        String tag = "";
        String val = "";
        boolean tagClosed = false;

        for(int i=0; i<encodedVals.length(); i++) {
            char c = encodedVals.charAt(i);

            if(isPresent(c, invalidChars)) {
                continue;
            }

            if(c == ':') {
                tag = temp.toString();

                if(i == encodedVals.length()-1) { // if value is empty
                    arrDecoded.add(new InputModel(tag, ""));
                }

                temp = new StringBuilder();
                tagClosed = true;

            } else if(c == ',' || (i == encodedVals.length()-1 && tagClosed)) {

                if(i == encodedVals.length()-1 && c != ',') {
                    temp.append(c);
                }

                val = temp.toString();

                arrDecoded.add(new InputModel(tag, val));

                // reset for new one
                temp = new StringBuilder();
                tag = "";
                val = "";

                tagClosed = false;

            } else {
                temp.append(c);
            }
        }

        if (!(temp.length() == 0) && arrDecoded.isEmpty()) {
            val = temp.toString();
            arrDecoded.add(new InputModel(tag, val));
        }

        return arrDecoded;
    }

    private boolean isPresent(char c, char[] array) {
        return isPresentUtil(c, array, 0, array.length-1);
    }

    private boolean isPresentUtil(char c, char[] array, int start, int end) {
        if(start == end+1) {
            return false;
        }
        if(array[start] == c) {
            return true;
        }

        return isPresentUtil(c, array, ++start, end);
    }
}
