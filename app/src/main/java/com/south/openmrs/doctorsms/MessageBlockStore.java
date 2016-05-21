package com.south.openmrs.doctorsms;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by angel on 5/20/16.
 */
public class MessageBlockStore extends SQLiteOpenHelper{



    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "messages.db";




    private static final String TEXT_TYPE = " TEXT";
    private static final String BLOB_TYPE = " BLOB";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_NAME_SENDER_ID + " INTEGER NOT NULL," +
                    FeedEntry.COLUMN_NAME_REC + " INTEGER NOT NULL,"+
                    FeedEntry.COLUMN_NAME_MESSAGE + " TEXT NOT NULL," +
                    FeedEntry.COLUMN_NAME_ENCKEY + " TEXT NOT NULL," +
                    FeedEntry.COLUMN_NAME_TIMESTAMP + " INTEGER NOT NULL,"+
                    "PRIMARY KEY ("+FeedEntry.COLUMN_NAME_SENDER_ID  +"," +
                    FeedEntry.COLUMN_NAME_TIMESTAMP + ")" +
            " )";


    public MessageBlockStore(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



    public static abstract class FeedEntry implements BaseColumns{
        public static final String TABLE_NAME = "messages";
        public static final String COLUMN_NAME_SENDER_ID = "senderid";
        public static final String COLUMN_NAME_REC = "recieverid";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_ENCKEY = "enckey";
        public static final String COLUMN_NAME_TIMESTAMP = "time";


    }






}
