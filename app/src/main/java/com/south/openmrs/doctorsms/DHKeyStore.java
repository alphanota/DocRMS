package com.south.openmrs.doctorsms;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


/**
 * Created by angel on 5/22/16.
 */
public class DHKeyStore extends SQLiteOpenHelper {



    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dh_keys.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DHKeyEntry.TABLE_NAME + " (" +
                    DHKeyEntry.COLUMN_NAME_LOCALID + " INTEGER NOT NULL," +
                    DHKeyEntry.COLUMN_NAME_REMOTEID + " INTEGER NOT NULL,"+
                    DHKeyEntry.COLUMN_NAME_PUB_KEY + " TEXT NOT NULL," +
                    DHKeyEntry.COLUMN_NAME_PRIV_KEY + " TEXT NOT NULL," +
                    "PRIMARY KEY ("+ DHKeyEntry.COLUMN_NAME_LOCALID +"," +
                    DHKeyEntry.COLUMN_NAME_REMOTEID + ")" +
                    " )";

    public DHKeyStore(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



    public static abstract class DHKeyEntry implements BaseColumns {
        public static final String TABLE_NAME = "dhkey";
        public static final String COLUMN_NAME_LOCALID = "localid";
        public static final String COLUMN_NAME_REMOTEID = "remoteid";
        public static final String COLUMN_NAME_PUB_KEY = "public_key";
        public static final String COLUMN_NAME_PRIV_KEY = "private_key";

    }

}
