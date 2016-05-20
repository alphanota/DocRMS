package com.south.openmrs.doctorsms;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by angel on 12/27/15.
 */
public class ImageManager {

        public static File[] foo(){

        String path = Environment.getExternalStorageDirectory().toString();
        path = path + "/DCIM/100MEDIA";
        File f = new File(path);
        File[] images = f.listFiles();
        return images;

        }



        public static ArrayList<ImageItem> getThumbnails(Context context){
            final String[] projection = {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.TITLE


            };
            final String selection = String.format("%s = %d",MediaStore.Files.FileColumns.MEDIA_TYPE,MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE );

            Uri queryUri = MediaStore.Files.getContentUri("external");

            CursorLoader cursorLoader = new CursorLoader(
                    context,
                    queryUri,
                    projection,
                    selection,
                    null, // selection args(none)
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC" //sort order
            );


            ArrayList<ImageItem> result = new ArrayList<ImageItem>();
            // result clear

            Cursor thumbCursor = cursorLoader.loadInBackground();



            int thumbnailColumnIndex = thumbCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            int image_column_Index = thumbCursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
            if(thumbCursor.moveToFirst()) {

                do{
                    //generate tiny thumbnail version
                    //int thumbnailImageID = thumbCursor.getInt(thumbnailColumnIndex);
                    //String thumbnailPath = thumbCursor.getString(thumbnailImageID);
                    int id = thumbCursor.getInt(image_column_Index);
                    String path = thumbCursor.getString(thumbnailColumnIndex);
                    //create ImageItem
                    //Uri fullImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.withAppendedPath
                    //        (MediaStore.Images.Media.EXTERNAL_CONTENT_URI,String.valueOf(id));

                    Uri fullImageUri = Uri.parse(path);

                    ImageItem newItem = new ImageItem(null,fullImageUri);
                    result.add(newItem);
                } while(thumbCursor.moveToNext());

            } //if

            thumbCursor.close();
            return result;

        }


        public static Uri uriToFullImage(Cursor thumbCursor, Context context){

            String ImageId = thumbCursor.getString( thumbCursor.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID));

            // Request image related to this thumbnail
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor imagesCursor = context.getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // select from external content uri
                            filePathColumn,   // these columns
                            MediaStore.Images.Media._ID + "=?", // where column images.media data =
                            new String[]{ImageId}, // imageid (primary key?)
                            null); // sort by... null = no order specified


            if ( imagesCursor != null && imagesCursor.moveToFirst() ){
                int columnIndex = imagesCursor.getColumnIndex(filePathColumn[0]);

                String filePath = imagesCursor.getString(columnIndex);
                imagesCursor.close();
                return Uri.parse(filePath);

            }
            else{
                imagesCursor.close();
                return Uri.parse("");
            }

        }

}
