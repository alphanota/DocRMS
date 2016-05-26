package com.south.openmrs.doctorsms;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

/**
 * Created by angel on 5/17/16.
 */
public class SharedPrefsHelper {


    public static User loadUserDataFromPrefs(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.user_details_prefs), context.MODE_PRIVATE);
         String sAuth=      context.getString(R.string.stored_user_token);


        String storedUserToken =sharedPref.getString(context.getString(R.string.stored_user_token),"0");
        String storedUsername = sharedPref.getString(context.getString(R.string.stored_username),"0");
        String userIdStr =      sharedPref.getString(context.getString(R.string.stored_user_userid), "0");
        String storedFirstName =sharedPref.getString(context.getString(R.string.stored_user_firstname), "0");
        String storedLastname  =sharedPref.getString(context.getString(R.string.stored_user_lastname), "0");
        String storedServerUrl = sharedPref.getString(context.getString(R.string.stored_server_url), "0");
        long userIdLong = Long.parseLong(userIdStr);

        User user = new User(userIdLong,storedFirstName,storedLastname,storedUsername,storedUserToken);
        user.setServerUrl(storedServerUrl);
        return user;
    }


    public static long saveMessage(Context context, MessageBlock msg){

        MessageBlockStore  msgStore = new MessageBlockStore(context);

        //get the data repository in write mode
        SQLiteDatabase db = msgStore.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MessageBlockStore.FeedEntry.COLUMN_NAME_SENDER_ID, msg.getSenderId());
        values.put(MessageBlockStore.FeedEntry.COLUMN_NAME_REC,msg.getRecipient());
        values.put(MessageBlockStore.FeedEntry.COLUMN_NAME_MESSAGE, msg.getMessage());
        values.put(MessageBlockStore.FeedEntry.COLUMN_NAME_ENCKEY,"PLAINTEXT");
        values.put(MessageBlockStore.FeedEntry.COLUMN_NAME_TIMESTAMP,msg.getTime());
        long newRowId;

        newRowId = db.insert(MessageBlockStore.FeedEntry.TABLE_NAME, null,
                values);



        //db.close();

        return newRowId;


    }
    public static void saveMessageOnBackground(Context context, MessageBlock block){

        final Context mContext = context;
        new AsyncTask<MessageBlock,Long,Long>(){

            @Override
            protected Long doInBackground(MessageBlock... params) {


                long result = SharedPrefsHelper.saveMessage(mContext,params[0]);
                return result;
            }


        }.execute(block);
    }



}
