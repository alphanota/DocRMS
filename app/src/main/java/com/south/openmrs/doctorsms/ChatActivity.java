package com.south.openmrs.doctorsms;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.south.openmrs.doctorsms.NetworkService.NetworkBinder;

public class ChatActivity extends AppCompatActivity {

    ArrayList<Item> postList;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LruCache<String,Bitmap> mMemoryCache;
    private Context context;
    User mCurrentUser;
    ContactItem mRemoteContact;

    Intent serviceIntent;

    NetworkService mService;
    boolean mBound = false;


    GetMessagesTask getMsgTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_activity);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.user_details_prefs), MODE_PRIVATE);


        String storedUserToken =sharedPref.getString(getString(R.string.stored_user_token),"0");
        String storedUsername = sharedPref.getString(getString(R.string.stored_username),"0");
        String userIdStr =      sharedPref.getString(getString(R.string.stored_user_userid), "0");
        String storedFirstName =sharedPref.getString(getString(R.string.stored_user_firstname), "0");
        String storedLastname  =sharedPref.getString(getString(R.string.stored_user_lastname), "0");

        long userIdLong = Long.parseLong(userIdStr);

        mCurrentUser = new User(userIdLong,storedFirstName,storedLastname,storedUsername,storedUserToken);

        mRemoteContact = (ContactItem) getIntent().getSerializableExtra("ContactName");


        postList = new ArrayList<Item>();

        context =this;


        // get max available VM memory, exceeding this amount will throw
        // an OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory()) / 1024;

        //use 1/8 of the available memory for this memory cache
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap){
                // the cache size will be measured in kilobytes rather
                // than number of items
                return bitmap.getByteCount() / 1024;

            }

        };

        ImageButton submitPostButton = (ImageButton) findViewById(R.id.chat_send_message);
        submitPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        init(null);

        serviceIntent = new Intent(this, NetworkService.class);
        serviceIntent.putExtra("senderid",mRemoteContact.getId());
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(myToolbar);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.chat_menu_conversation_info:
                //showFriendDialog();
                Toast.makeText(context,"Not implemented!",Toast.LENGTH_LONG).show();
                return true;

            case R.id.chat_menu_logout:
                sendBroadcast(new Intent(NetworkService.LOG_OUT));
                Intent intent = new Intent(this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void showFriendDialog(){

        String myRsaPubKey = RSAKeyPair.getMyRSAPublicKey(context,mCurrentUser.getId(),mRemoteContact.getId());
        String[] myDhKey = RSAKeyPair.getDHKey(context,mCurrentUser.getId(),mRemoteContact.getId());


        String theirDHPubKey = RSAKeyPair.getDHPublicKey(context,mCurrentUser.getId(),mRemoteContact.getId());

        String theirRSAPubKey = RSAKeyPair.getRSAPublicKey(context,mCurrentUser.getId(),mRemoteContact.getId());


        byte[] mrsabytes = Base64.decode(myRsaPubKey,Base64.URL_SAFE);
        byte[] mdhbytes = Base64.decode(myDhKey[0],Base64.URL_SAFE);
        byte[] tdhkey = Base64.decode(theirDHPubKey,Base64.URL_SAFE);
        byte[] trsakey = Base64.decode(theirRSAPubKey,Base64.URL_SAFE);

        /*
         FriendInfoDialog(final Context context, ContactItem friend,
                     User user,
                     String friend_dhKey, String friend_rsaKey,
                     String your_dhKey, String your_rsaKey){
        super(context);
         */

        FriendInfoDialog friendInfoDialog = new FriendInfoDialog(context,
                mRemoteContact,
                mCurrentUser,
                RSAKeyPair.fingerPrintFormat(tdhkey),
                RSAKeyPair.fingerPrintFormat(trsakey),
                RSAKeyPair.fingerPrintFormat(mrsabytes),
                RSAKeyPair.fingerPrintFormat(mdhbytes)


        );
        friendInfoDialog.show();





    }

    @Override
    protected void onPause() {


        Intent vIntent = new Intent(NetworkService.CHAT_VISIBILITY);
        vIntent.putExtra("sid",mRemoteContact.getId());
        vIntent.putExtra("visibility",false);

        sendBroadcast(vIntent);

        this.unregisterReceiver(lreceiver);
        this.unregisterReceiver(lreceivers);
        unbindService(mConnection);
        super.onPause();
    }

    private void init(View v) {
        mRecyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view);
        //mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        //mLayoutManager = new LinearLayoutManager(context);

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new MyAdapter(postList, context, mMemoryCache);
        mRecyclerView.setAdapter(mAdapter);

        getMsgTask = new GetMessagesTask(getApplicationContext(),mRemoteContact.getId(),mCurrentUser.getId()){

            @Override
            public void onPostExecute(Cursor result){


                result.moveToPosition(-1);
                while(result.moveToNext()){

                    int sidPosition = result.getColumnIndexOrThrow(MessageBlockStore.FeedEntry.COLUMN_NAME_SENDER_ID);
                    int ridPosition = result.getColumnIndexOrThrow(MessageBlockStore.FeedEntry.COLUMN_NAME_REC);
                    int timePosition = result.getColumnIndexOrThrow(MessageBlockStore.FeedEntry.COLUMN_NAME_TIMESTAMP);
                    int msgPosition = result.getColumnIndexOrThrow(MessageBlockStore.FeedEntry.COLUMN_NAME_MESSAGE);

                    String msg = result.getString(msgPosition);
                    long sid = result.getLong(sidPosition);
                    long rid = result.getLong(ridPosition);
                    long time = result.getLong(timePosition);


                    MessageBlock block = new MessageBlock(sid,rid,msg,time);

                    MessageItem item = new MessageItem(mCurrentUser,mRemoteContact,block);

                    postList.add(item);

                }


                mAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(postList.size()-1);
                





            }
        };

        getMsgTask.execute(0L);



    }

    public void sendMessage() {
        //containerView
        // gets message from UI fields
        EditText editText = (EditText) findViewById(R.id.chat_edit_message);
        String message = editText.getText().toString();

        //===================================================================

        // we create a new MessageItem
        // update the recycler view holding the messages etc..
        sendMessageToScreen(message,editText);

        //====================================================================
        // And then
        sendMessageToNetwork(message);

    }


    @Override
    protected void onResume() {
        super.onResume();
        //bindService(mConnection);
        registerReceiver(lreceiver, new IntentFilter(NetworkService.NEW_MESSAGE));
        registerReceiver(lreceivers, new IntentFilter(NetworkService.NEW_MESSAGES));

        Intent vIntent = new Intent(NetworkService.CHAT_VISIBILITY);
        vIntent.putExtra("sid",mRemoteContact.getId());
        vIntent.putExtra("visibility",true);

        sendBroadcast(vIntent);

    }

    private void sendMessageToScreen(String message, EditText editText){


        MessageBlock msgBlock = new MessageBlock(mCurrentUser.getId(),mRemoteContact.getId(),message, System.currentTimeMillis());

        MessageItem post = new MessageItem(mCurrentUser,mRemoteContact,msgBlock);


        SharedPrefsHelper.saveMessageOnBackground(getApplicationContext(),msgBlock);

        postList.add(post);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(postList.size()-1);
        editText.setText("");
    }


    private void sendForeignMessageToScreen(MessageBlock msg){

        long sid = msg.getSenderId();

        if (sid == mRemoteContact.getId()){
            MessageItem post = new MessageItem(mCurrentUser, mRemoteContact,msg);
            postList.add(post);
            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(postList.size()-1);

        }
    }

    private void sendMessageToNetwork(String message){

        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.user_details_prefs), MODE_PRIVATE);

        String serverUrl =sharedPref.getString(getString(R.string.stored_server_url),
                "http://localhost:8080");



        String aes_key = RSAKeyPair.getAESKey(getApplicationContext(),mCurrentUser.getId(), mRemoteContact.getId());

        byte[] msgBytes = message.getBytes();

        SecureRandom rand = new SecureRandom();

        byte[] ivBytes = new byte[16];

        rand.nextBytes(ivBytes);

        byte[] aesKeyBytes = Base64.decode(aes_key,Base64.URL_SAFE);

        String encMsg = "send error";
        try {

            byte[] enc = RSAKeyPair.AESencrypt(message,aesKeyBytes,ivBytes);
            encMsg = Base64.encodeToString(enc,Base64.URL_SAFE);

        }  catch (Exception e){

        }
        String iv_str = Base64.encodeToString(ivBytes,Base64.URL_SAFE);

        List<NameValuePair> params = new LinkedList<NameValuePair>();

        //example query:
        //http://localhost:8080/openmrsMessage/sendMessage?
        // sid=23948234&
        // rid=10920934&
        // msg=hello+world+lol&
        // auth=dontcare

        params.add(new NameValuePair("action","message"));
        params.add(new NameValuePair("sid",""+mCurrentUser.getId()));
        params.add(new NameValuePair("rid",""+mRemoteContact.getId()));
        params.add(new NameValuePair("msg",encMsg));
        params.add(new NameValuePair("auth",mCurrentUser.getAuth()));
        params.add(new NameValuePair("iv",iv_str));

        //"http://10.0.0.16:8080/openmrsMessage/login"
        String urlResource = "/openmrsMessage/sendMessage";

        HttpPost mPost = new HttpPost(this,serverUrl,urlResource){



            @Override
            protected void onPostExecute(String response) {
                if (response != null) {
                    Toast.makeText(parentContext,response,Toast.LENGTH_SHORT).show();

                }
            }
        };
        mPost.execute(params);
    }

    private BroadcastReceiver lreceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //System.out.println("Loginreciever caught ");

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                //int subj = bundle.getInt(ActivityCodes.CODEPROTOCOL);

                MessageBlock msgBlock = (MessageBlock)bundle.getSerializable("message");

                long sid = msgBlock.getSenderId();

                //if (sid == mRemoteContact.getId()){

                    sendForeignMessageToScreen(msgBlock);

                //}




            }//bundle!=null
        } //overr. onReceive
    }; //new broadcast


    private BroadcastReceiver lreceivers = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //System.out.println("Loginreciever caught ");

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                //int subj = bundle.getInt(ActivityCodes.CODEPROTOCOL);

                MessageStore msgStore = (MessageStore)bundle.getSerializable("messages");

                //long sid = msgBlock.getSenderId();

                //if (sid == mRemoteContact.getId()){


                MessageBlock t = null;
                while( (t = msgStore.getAMessage() )!=null){
                    sendForeignMessageToScreen(t);

                }

                //}

            }//bundle!=null
        } //overr. onReceive
    }; //new broadcast



    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            NetworkBinder binder = (NetworkBinder) service;
            mService = binder.getService();
            mBound = true;

            mService.connect(serviceIntent);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


}
