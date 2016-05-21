package com.south.openmrs.doctorsms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

/**
 * Created by angel on 5/20/16.
 */
public class GetMessagesTask extends AsyncTask<Long,Long,Cursor> {
    Context mContext;

    long mRemoteId;
    long mLocalId;

    GetMessagesTask(Context context, long remoteId, long localId){
        mContext = context;
        mRemoteId = remoteId;
        mLocalId = localId;
    }

    @Override
    protected Cursor doInBackground(Long... params) {
        SQLiteDatabase db = new MessageBlockStore(mContext).getReadableDatabase();
        String[] projection = {
                MessageBlockStore.FeedEntry.COLUMN_NAME_SENDER_ID,
                MessageBlockStore.FeedEntry.COLUMN_NAME_REC,
                MessageBlockStore.FeedEntry.COLUMN_NAME_MESSAGE,
                MessageBlockStore.FeedEntry.COLUMN_NAME_TIMESTAMP


        };

        // this is just simple
        // placeholder replacement
        // not switching cases or anything
        @SuppressLint("DefaultLocale")
                String selection = String.format("(%s = %d AND %s = %d) OR (%s = %d AND %s = %d)",
                MessageBlockStore.FeedEntry.COLUMN_NAME_SENDER_ID,mLocalId,
                MessageBlockStore.FeedEntry.COLUMN_NAME_REC,mRemoteId,
                MessageBlockStore.FeedEntry.COLUMN_NAME_SENDER_ID,mRemoteId,
                MessageBlockStore.FeedEntry.COLUMN_NAME_REC,mLocalId
                );



        String sortOrder=
                MessageBlockStore.FeedEntry.COLUMN_NAME_TIMESTAMP + " ASC";


        Cursor c =db.query(MessageBlockStore.FeedEntry.TABLE_NAME, // the table name
                projection,
                selection,
                null,
                null,
                null,
                sortOrder
                );
        //db.close();
        return c;
    }



}
