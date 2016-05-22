package com.south.openmrs.doctorsms;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.LruCache;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    ArrayList<Item> postList;
    ArrayList<Item> images;
    Context context;
    private LruCache<String, Bitmap> mMemoryCache;
    View containerView;

    User mCurrentUser;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        context = this;
        postList = new ArrayList<Item>();


        final int maxMemory = (int) (Runtime.getRuntime().maxMemory()) / 1024;

        //use 1/8 of the available memory for this memory cache
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // the cache size will be measured in kilobytes rather
                // than number of items
                return bitmap.getByteCount() / 1024;

            }

        };


        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.user_details_prefs), MODE_PRIVATE);


        String storedUserToken =sharedPref.getString(getString(R.string.stored_user_token),"0");
        String storedUsername = sharedPref.getString(getString(R.string.stored_username),"0");
        String userIdStr =      sharedPref.getString(getString(R.string.stored_user_userid), "0");
        String storedFirstName =sharedPref.getString(getString(R.string.stored_user_firstname), "0");
        String storedLastname  =sharedPref.getString(getString(R.string.stored_user_lastname), "0");

        long userIdLong = Long.parseLong(userIdStr);

        mCurrentUser = new User(userIdLong,storedFirstName,storedLastname,storedUsername,storedUserToken);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.contact_toolbar);
        setSupportActionBar(myToolbar);

        init(null);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contact_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_item_add_contact:
                showAddContactDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showAddContactDialog(){

            AddContactDialog addContactDialog = new AddContactDialog(getApplicationContext());
            addContactDialog.show();

    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_contacts, container, false);
        //final PopupWindow popup = new PopupWindow( popupView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
        //popup.setAnimationStyle(R.style.PopupAnimation);
        containerView = v;

        init(v);

        return v;
    }

    private void init(View v) {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //mLayoutManager = new LinearLayoutManager(context);

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new MyAdapter(postList, context, mMemoryCache);
        mRecyclerView.setAdapter(mAdapter);

        networkGetContacts();

    }

    public static Uri ResourceToUri (Context context,int resID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(resID) + '/' +
                context.getResources().getResourceTypeName(resID) + '/' +
                context.getResources().getResourceEntryName(resID) );
    }


    public void testContacts(){

        ContactItem item = new ContactItem(ResourceToUri(this,R.mipmap.ic_launcher),null,"Dr. Kerry Constable");
        for(int i =0; i < 10; i++){
            postList.add(new ContactItem(ResourceToUri(this,R.mipmap.ic_launcher),null,"Dr. Kerry Constable"));
        }

        mAdapter.notifyDataSetChanged();

    }


    ContactItem parseContact(JSONObject jObj){
        // format of json object
        //{"username":"hp7books","userid":23948234,"lastname":"Potter","firstname":"Harry"}

        try{

            String username = jObj.getString("username");
            String userId = jObj.getString("userid");
            String lastname = jObj.getString("lastname");
            String firstname = jObj.getString("firstname");

            ContactItem item = new ContactItem(
                    ResourceToUri(this,R.mipmap.ic_launcher),
                    username,
                    userId,
                    lastname,
                    firstname


            );

            return item;

        } catch (JSONException e){
            return null;
        }



    }


    ArrayList<ContactItem> parseContacts(JSONArray jAry){

        System.out.println(jAry.toString());

        ArrayList<ContactItem> cItems = new ArrayList<ContactItem>();
        int jAryLength =jAry.length();

        for(int i =0; i < jAryLength; i++){

            try{
                JSONObject jObj = jAry.getJSONObject(i);

                ContactItem cItem = parseContact(jObj);
                if(cItem != null){
                    cItems.add(cItem);
                }

            } catch (JSONException e){

            }




        }

            return cItems;
    }



    private void networkGetContacts(){

        Intent intent = this.getIntent();
        String serverUrl = intent.getStringExtra("serverurl");

       Toast.makeText(this,"I got this url from calling activity: " + serverUrl,Toast.LENGTH_LONG).show();

        JSONObject responseJSON;

        List<NameValuePair> params = new LinkedList<NameValuePair>();
        String authToken;
        String userid;

        try{

            responseJSON = new JSONObject(
                    intent.getStringExtra("networkresponse")
            );

            authToken = responseJSON.getString("authtoken");
            userid =  responseJSON.getString("userid");

        } catch (JSONException e){

            return;

        }

            params.add(new NameValuePair("action","getcontacts"));
            params.add(new NameValuePair("userid",userid));
            params.add(new NameValuePair("auth",authToken));

        //"http://ip:port/openmrsMessage/Contacts?userid=1234&auth=sfKxjfdsdkfj2"
        String urlResource = "/openmrsMessage/Contacts";

        System.out.println("ContactsActivity: " + serverUrl+urlResource);
        HttpPost mPost = new HttpPost(this,serverUrl,urlResource){
            @Override
            protected void onPostExecute(String response) {
                if (response != null) {
                    System.out.println(response);
                    JSONObject loginResult = null;
                    try{
                        JSONArray jAry = new JSONArray(response);



                        ArrayList<ContactItem> items = parseContacts(jAry);

                        postList.addAll(items);
                        mAdapter.notifyDataSetChanged();



                    } catch (JSONException e){
                        e.printStackTrace();
                    }



                }
            }
        };
        mPost.execute(params);
    }








    public class AddContactDialog extends Dialog {

        TextView scoreView;
        Button submitButton;
       AddContactDialog(final Context context){
           super(context);

           requestWindowFeature(Window.FEATURE_NO_TITLE);
           setContentView(R.layout.add_contact_layout);

           scoreView = (TextView) findViewById(R.id.add_contact_friend_id);





           submitButton = (Button) findViewById(R.id.add_contact_friend_submit);
           submitButton.setOnClickListener(new View.OnClickListener(){


               @Override
               public void onClick(View view){

                   String friendIdStr = submitButton.getText().toString();
                   try {
                       long userId = Long.parseLong(friendIdStr);

                       getFriend(userId);

                   } catch (NumberFormatException e){
                       Toast.makeText(context,"Not a number!",Toast.LENGTH_SHORT).show();
                   }





                   dismiss();
               }
           });

       }



    }




    protected void getFriend( long fid ){


        Intent intent = this.getIntent();
        String serverUrl = intent.getStringExtra("serverurl");


        JSONObject responseJSON;

        List<NameValuePair> params = new LinkedList<NameValuePair>();
        String authToken;
        String userid;

        try{

            responseJSON = new JSONObject(
                    intent.getStringExtra("networkresponse")
            );

            authToken = responseJSON.getString("authtoken");
            userid =  responseJSON.getString("userid");

        } catch (JSONException e){

            return;

        }

        params.add(new NameValuePair("userid",userid));
        params.add(new NameValuePair("fid", ""+fid) );
        params.add(new NameValuePair("auth",authToken));

        //"http://ip:port/openmrsMessage/Contacts?userid=1234&auth=sfKxjfdsdkfj2"
        String urlResource = "/openmrsMessage/addContact";

        System.out.println("ContactsActivity: " + serverUrl+urlResource);
        HttpPost mPost = new HttpPost(this,serverUrl,urlResource){
            @Override
            protected void onPostExecute(String response) {
                if (response != null) {
                    System.out.println(response);
                    JSONObject loginResult = null;
                    try{
                        JSONArray jAry = new JSONArray(response);



                        ArrayList<ContactItem> items = parseContacts(jAry);

                        postList.addAll(items);
                        mAdapter.notifyDataSetChanged();



                    } catch (JSONException e){
                        e.printStackTrace();
                    }



                }
            }
        };
        mPost.execute(params);







    }





}