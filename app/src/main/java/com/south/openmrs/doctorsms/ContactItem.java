package com.south.openmrs.doctorsms;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by angel on 4/25/16.
 */
public class ContactItem extends Item implements Serializable {

    String mContactName;
    boolean mNewMessage;

    private String mFn;
    private String mLn;
    private String mUn;
    private long mId;

    // format of json object
    //{"username":"hp7books","userid":23948234,"lastname":"Potter","firstname":"Harry"}
    ContactItem(Uri thumb, String un, String uid, String ln, String fn){


        mId = Long.parseLong(uid);
        mUn = un;
        mLn = ln;
        mFn = fn;
        mContactName = fn + " " + ln;
        mNewMessage =false;
    }

    ContactItem(Uri thumb, Uri orig,String ContactName){

        this.mContactName = ContactName;
    }

    public long getId(){
        return mId;
    }

    public String getUn(){
        return mUn;
    }

}
