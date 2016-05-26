package com.south.openmrs.doctorsms;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


/**
 * Created by angel on 5/22/16.
 */
public class AesKeyStore extends SQLiteOpenHelper {



    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "aes_keys.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + AesKeyEntry.TABLE_NAME + " (" +
                    AesKeyEntry.COLUMN_NAME_LOCALID + " INTEGER NOT NULL," +
                    AesKeyEntry.COLUMN_NAME_REMOTEID + " INTEGER NOT NULL,"+
                    AesKeyEntry.COLUMN_NAME_AES_KEY + " TEXT NOT NULL," +
                    "PRIMARY KEY ("+ AesKeyEntry.COLUMN_NAME_LOCALID +"," +
                    AesKeyEntry.COLUMN_NAME_REMOTEID + ")" +
                    " )";

    public AesKeyStore(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



    public static abstract class AesKeyEntry implements BaseColumns {
        public static final String TABLE_NAME = "aes_keys";
        public static final String COLUMN_NAME_LOCALID = "localid";
        public static final String COLUMN_NAME_REMOTEID = "remote";
        public static final String COLUMN_NAME_AES_KEY = "aes_key";

    }

}
