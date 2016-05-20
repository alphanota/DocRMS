package com.south.openmrs.doctorsms;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


/**
 * Created by angel on 12/26/15.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.genericViewHolder> implements  Comparable<MyAdapter>{


    public int compareTo(MyAdapter m){
        return 0;
    }

    public ArrayList<Item> mDataSet;
    Context context;
    LruCache<String,Bitmap> bitmapLruCache;
    public static final int HOLDER_TYPE_MESSAGE = 0;
    public static final int HOLDER_TYPE_IMAGE = 1;
    public static final int HOLDER_TYPE_CONTACT = 2;
    public static Bitmap mPlaceHolderBitmap;

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        // Raw height and width of the image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){
            final int halfHeight = height/2;
            final int halfWidth = width/2;

            //Calculate the largest inSampleSize value that is a power of 2 and keeps
            //height and width larger than the requested height and width;
            while((halfHeight / inSampleSize ) > reqHeight &&
                    (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;

    }

    public static Bitmap decodeSampledBitmapFromUri(Uri imgUri, int reqWidth, int reqHeight ){
        File photoFile = new File(imgUri.toString());
        if(photoFile.exists()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photoFile.getAbsolutePath(),options);

            //calculate insamplesize
            options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);

            //decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            Bitmap picture = BitmapFactory.decodeFile(photoFile.getAbsolutePath(),options);
            return picture;

        }
        return null;
    }
    class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap>{

        private final WeakReference<ImageView> imageViewReference;
        private Uri data;

        BitmapWorkerTask(ImageView imageView) {
            // use a weak reference to ensure the image can be garbage collected
            this.imageViewReference = new WeakReference<ImageView>(imageView);
        }

        //decode image in background
        @Override
        protected Bitmap doInBackground(Uri... params){
            data = params[0];
            Log.d("LOADING", params[0].toString() );
            //System.out.println(" Loading + "+ params[0].toString());
            Bitmap bitmap = decodeSampledBitmapFromUri(data,100,100);

            //Bitmap bitmap = getBitmapfromNetwork();
            //String imageKey = bitmap.toString();
            String imageKey = data.getPath();
            if( bitmapLruCache.get(imageKey) == null){
                if(imageKey != null && bitmap != null) bitmapLruCache.put(imageKey,bitmap);
            }
            return bitmap;
        }

        //once complete, see if ImageView is still around and set bitmap

        /* the last step is updating onPostExecute() in
           BitmapWorkerTask so that it checks if the task is cancelled and if the current task
           matches the one associated with the ImageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap){

            if (isCancelled()){
                bitmap = null;
                Log.d("WARNINING", "is cancelled ");
            }

            if (imageViewReference != null && bitmap != null){
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if(this == bitmapWorkerTask && imageView != null){
                    imageView.setImageBitmap(bitmap);

                }

            }

            else{
                Log.d("WARNING", "nulls ");
            }

        }
    }


    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskWeakReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask){
            super(res,bitmap);
            bitmapWorkerTaskWeakReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask(){
            return bitmapWorkerTaskWeakReference.get();
        }
    }

    public static boolean cancelPotentialWork (Uri data, ImageView imageView){
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if(bitmapWorkerTask != null){
            final Uri bitmapData = bitmapWorkerTask.data;
            // if bitmapData is not yet set or it differs from the new data
            if ( bitmapData == null || ( bitmapData.compareTo(data) == 0) ){
                //Cancel previous task
                bitmapWorkerTask.cancel(true);
            }
            else{
                // The same work is already in progress
                return false;
            }

        }
        // No task associated with the ImageView, or an exisitng task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageview){
        if(imageview != null){
            final Drawable drawable = imageview.getDrawable();
            if(drawable instanceof AsyncDrawable){
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }

        }
        return null;
    }



////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////Message Holders////////////////////////////////////////////////////////////////////////
    public static  class genericViewHolder extends RecyclerView.ViewHolder{
        View parentView;
        genericViewHolder(View v){
            super(v);
            parentView = v;
            v.setClickable(true);

        }
    }

    public static class messageViewHolder extends genericViewHolder {
        TextView message;
        TextView title;


        public messageViewHolder(View v) {
            super(v);

            message = (TextView) v.findViewById(R.id.line);
            title  = (TextView) v.findViewById(R.id.title);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast toast = Toast.makeText(v.getContext(), message.getText().toString(), Toast.LENGTH_SHORT);
                    toast.show();




                }
            });


        }

    }


    public static class ContactViewHolder extends genericViewHolder{
        TextView contactName;
        ImageView contactPicture;
        private int mPositionReference;
        ArrayList<Item> mDataSet;

        public  ContactViewHolder(View v, final ArrayList<Item> mDataSet){

            super(v);
            contactName = (TextView) v.findViewById(R.id.contact_name);
            contactPicture = (ImageView) v.findViewById(R.id.contact_image);


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast toast = Toast.makeText(v.getContext(), "Click", Toast.LENGTH_SHORT);
                    toast.show();

                    ContactItem cItem = (ContactItem)mDataSet.get(mPositionReference);



                    Intent intent = new Intent(v.getContext(), ChatActivity.class);

                    intent.putExtra("ContactName",cItem);

                    v.getContext().startActivity(intent);


                }
            });


        }

        public void setPositionReference(int p){
            mPositionReference = p;
        }

        public int getPositionReference(){
            return mPositionReference;
        }


    }


    public static class ImageViewHolder extends genericViewHolder {
        TextView message;
        TextView title;
        ImageView image;
        LinearLayout textHolder;
        public Uri imageUri;
        public static final String IMAGEVIEW_TAG = "bitmap";

        public ImageViewHolder(View v) {
            super(v);
            /*
            textHolder = (LinearLayout) v.findViewById(R.id.image_text_holder);
            message = (TextView) v.findViewById(R.id.image_op_message);
            title = (TextView) v.findViewById(R.id.image_op);
            imageUri = null;

            image  = (ImageView) v.findViewById(R.id.photo_view_container);
            image.setTag(IMAGEVIEW_TAG);

            image.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View view) {

                    // create new clip data item from the imageview object's tag
                    ClipData dragData = ClipData.newUri(image.getContext().getContentResolver(), IMAGEVIEW_TAG, imageUri);

                    View.DragShadowBuilder myShadow = new View.DragShadowBuilder(image);


                    boolean t = view.startDrag(dragData, myShadow, null, 0);

                    return t;
                }
                 });
            */

        }

    }

///////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // provide a suitable constructor
    public MyAdapter(ArrayList<Item> list, Context context, LruCache<String,Bitmap> cache){
        this.mDataSet = list;
        this.context = context;
        this.bitmapLruCache = cache;
        mPlaceHolderBitmap = BitmapFactory.decodeResource(context.getResources(),R.mipmap.blank_square);
    }
    @Override
    public genericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView;
        switch(viewType){
            case HOLDER_TYPE_MESSAGE:
                rowView  = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.display_post, parent, false);
                // set the view size, margins, paddings, and layout parameters
                messageViewHolder mvh = new messageViewHolder(rowView);
                return mvh;

            case HOLDER_TYPE_IMAGE:
                rowView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_contact_list, parent, false);

                ImageViewHolder ivh = new ImageViewHolder(rowView);
                return ivh;

            case HOLDER_TYPE_CONTACT:
                rowView  = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_contact_list, parent, false);
                // set the view size, margins, paddings, and layout parameters
                ContactViewHolder cvh = new ContactViewHolder(rowView,mDataSet);
                return cvh;



            default:
                rowView  = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerview_contact_list, parent, false);
                // set the view size, margins, paddings, and layout parameters
                messageViewHolder default_mvh = new messageViewHolder(rowView);
                return default_mvh;

        }

    }

    @Override
    public int getItemViewType(int position){
        if ( mDataSet.get(position) instanceof ContactItem ){
            return HOLDER_TYPE_CONTACT;
        }
        else if (mDataSet.get(position) instanceof MessageItem){
            return HOLDER_TYPE_MESSAGE;
        }
        else return 0;
    }
    @Override
    public void onBindViewHolder(genericViewHolder holder, int position) {
        // get element from your dataset at this position
        // replace the contents of the view with that element

        int type = getItemViewType(position);

        switch(type){

            case HOLDER_TYPE_CONTACT:
                ContactItem contact = (ContactItem) mDataSet.get(position);
                ((ContactViewHolder)holder).contactName.setText(contact.mContactName );
                ((ContactViewHolder)holder).setPositionReference(position);
                //((ContactViewHolder)holder).title.setText(mesg.username);
                ((ContactViewHolder)holder).contactPicture.setImageResource(R.mipmap.ic_launcher);
                //((ContactViewHolder)holder).contactPicture.setClipToOutline(true);
                break;

            case HOLDER_TYPE_MESSAGE:
                MessageItem mesg = (MessageItem) mDataSet.get(position);
                ((messageViewHolder)holder).title.setText(mesg.username);
                ((messageViewHolder)holder).message.setText(mesg.msg);
                //TODO: remove println
                System.out.println("Added msg:  " + mesg.username + " " + mesg.msg);

               // ((messageViewHolder)holder).image.setImageResource(R.mipmap.ic_launcher);
                break;

            case HOLDER_TYPE_IMAGE:
                //image.setImageResource(R.mipmap.ic_launcher);
                ImageItem imageItem = ((ImageItem)mDataSet.get(position));
                Uri photo = imageItem.originalURI;
                final String imageKey = photo.getPath();

                Bitmap bitmap = bitmapLruCache.get(imageKey);
                /**
                 *
                 *  Convert bitmap to byte array
                 * Bitmap bmp = intent.getExtras().get("Data");
                 * int size = bmp.getRowBytes() * bmp.getHeight()
                 * ByteBuffer b = ByteBuffer.allocate(size);
                 * bmp.copyPixelsToBuffer(b);
                 * b.rewind();
                 * try{
                 *   b.get(bytes, 0, bytes.length);
                 * } catch (BufferUnderflowException e){
                 *      // whatever
                 * }
                 */
                if(bitmap != null){
                    ((ImageViewHolder) holder).image.setImageBitmap(bitmap);
                }
                else {
                    //final Bitmap bitmap = bitmapLruC
                    if (cancelPotentialWork(photo, ((ImageViewHolder) holder).image)) {
                        final BitmapWorkerTask task = new BitmapWorkerTask(((ImageViewHolder) holder).image);

                        final AsyncDrawable asyncdrawable = new AsyncDrawable(context.getResources(), mPlaceHolderBitmap, task);
                        ((ImageViewHolder) holder).image.setImageDrawable(asyncdrawable);
                        task.execute(photo);

                    }
                }

                if (imageItem.optional_message != null){
                    ((ImageViewHolder) holder) .message.setText(imageItem.optional_message.msg);
                    ((ImageViewHolder) holder) .title.setText(imageItem.optional_message.username);
                }

                else{
                    ((ImageViewHolder) holder) .message.setVisibility(View.GONE);
                    ((ImageViewHolder) holder) .title.setVisibility(View.GONE);
                }

                ((ImageViewHolder)(holder)).imageUri = photo;



                break;

             default:

        }



    }


    void swap(int i, int j){

        Item I =  mDataSet.get(i);
        Item J = mDataSet.get(j);
        mDataSet.remove(i);
        mDataSet.add(i, J);
        mDataSet.add(j, I);

        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void insert(Item post){
        mDataSet.add(post);
        notifyItemInserted(getItemCount()-1);
    }

    public void remove(Item data){
        int position = mDataSet.indexOf(data);
        mDataSet.remove(position);
        notifyItemRemoved(position);
    }



}
