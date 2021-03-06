package com.south.openmrs.doctorsms;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;


public class NetworkService extends Service {

    int mStartMode;        // Indicates how to behave if the service is killed
    IBinder mBinder;       // Interface for clients that bind
    boolean mAllowRebind;  // Indicates whether onRebind should be used
    private int mCount;    // The # of activities currently bound to this service instance
    private User mServiceUser;
    private HttpPost mServerListener;
    protected boolean keepConnecting = true;
    public final static String NEW_MESSAGE = "com.south.openmrs.doctorsms.service.reciever";
    public final static String NEW_MESSAGES = "com.south.openmrs.doctorsms.service.recievers";
    public final static String NETWORK_NOTIFICATION = "com.south.openmrs.doctorsms.service.netnotify";
    public final static String LOG_OUT = "com.south.openmrs.doctorsms.service.logout";
    boolean MaintainConnection = true;
    Hashtable<Long,MessageBlock> messages;
    protected static final String FAIL_RESPONSE = "No post parameters";
    public static final String CONTACT_LIST = "com.south.openmrs.doctorsms.service.contacts";
    public static final String CHAT_VISIBILITY = "com.south.openmrs.doctorsms.service.visible";


    Hashtable<Long,ContactItem> contactsTable;

    public NetworkService() {

    }

    public class NetworkBinder extends Binder {
        NetworkService getService() {
            // Return this instance of LocalService so clients can call public methods
            return NetworkService.this;
        }
    }

    @Override
    public void onCreate() {
        // The service is being created
        messages = new Hashtable<Long,MessageBlock> ();
        try{
            mServiceUser = SharedPrefsHelper.loadUserDataFromPrefs(getApplicationContext());
        } catch (Exception e){
            Log.d("onStartCommand: ",e.getLocalizedMessage(),e);
        }//=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-^=_-^-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        String urlResource = "/openmrsMessage/GetMessage";
        new DataRequestThread(urlResource).start();
        //TODO: Implement additional methods if necessary
        contactsTable = new Hashtable<Long,ContactItem>();
        registerReceiver(logout_receiver, new IntentFilter(NetworkService.LOG_OUT));
        registerReceiver(contact_receiver, new IntentFilter(NetworkService.CONTACT_LIST));
        registerReceiver(visibility_receiver, new IntentFilter(NetworkService.CHAT_VISIBILITY));

    }

    private void processResponse(String networkResponse){

        JSONObject jResponse;

        System.out.println("GOT NETWORK RESPONSE");

        try{
            jResponse = new JSONObject(networkResponse);

            String status = jResponse.getString("status");

            if( "fail".equals(status)){


                System.out.println("Receiving says fail");


            }

            else if ("success".equals(status)){

                String message = jResponse.getString("message");
                String act = null;
                JSONObject jobj = null;
                try {
                    jobj = new JSONObject(message);


                    act = jobj.getString("action");
                } catch (JSONException e){
                        System.out.println("Exception parsing msg as json");
                }


                if ( (act != null) && ( act.equals("request"))){
                    onFriendRequest(jobj);
                }

                else if ( (act != null) && ( act.equals("request_reply"))){
                    onFriendReplyRequest(jobj);
                }


                else deliverMessage(jResponse);

            }
        }  catch(JSONException e){

        } catch (NullPointerException e){
            Log.d("NetworkService",e.getMessage());
        }
    }
    private void onFriendRequest(JSONObject initiator_dh_rsa_pub_key_JSON){


        System.out.println("NetworkService: got friend request");

      try {
          String I_rsa_key = initiator_dh_rsa_pub_key_JSON.getString("rsa_pub_key");
          String I_dh_key = initiator_dh_rsa_pub_key_JSON.getString("dh_pub_key");
          long I_id = initiator_dh_rsa_pub_key_JSON.getLong("initiator_id");
          long R_id = initiator_dh_rsa_pub_key_JSON.getLong("rec_id");

          byte[] DH_PUB_KEY_Initiator_Bytes = Base64.decode(I_dh_key,Base64.URL_SAFE);



          KeyFactory myKeyFactory = KeyFactory.getInstance("DH");
          X509EncodedKeySpec X509KeySpec = new X509EncodedKeySpec(DH_PUB_KEY_Initiator_Bytes);
          PublicKey Initiator_DH_PUB_KEY = myKeyFactory.generatePublic(X509KeySpec);


          DHParameterSpec dhParamSpec = ((DHPublicKey) Initiator_DH_PUB_KEY ).getParams();

          // Create my own DH Keypair

          KeyPairGenerator myKeyPairGen = KeyPairGenerator.getInstance("DH");
          myKeyPairGen.initialize(dhParamSpec);
          KeyPair myKeyPair = myKeyPairGen.generateKeyPair();


          // generate new rsa key pair
          KeyPair initiator_key = RSAKeyPair.genRSAKeyPair();

          PrivateKey priv = initiator_key.getPrivate();
          PublicKey pub = initiator_key.getPublic();



          String privateKeyEncoded = Base64.encodeToString(priv.getEncoded(),Base64.URL_SAFE);
          String publicKeyEncoded = Base64.encodeToString(pub.getEncoded(),Base64.URL_SAFE);




          String dh_pub = Base64.encodeToString(myKeyPair.getPublic().getEncoded(),Base64.URL_SAFE);
          String dh_priv = Base64.encodeToString(myKeyPair.getPrivate().getEncoded(),Base64.URL_SAFE);



          /// save my public and private key in database
          RSAKeyPair.saveKeyPair(getApplicationContext(),mServiceUser.getId(),I_id,privateKeyEncoded,publicKeyEncoded);

          RSAKeyPair.saveDHKeyPair(getApplicationContext(),mServiceUser.getId(),I_id,dh_priv,dh_pub);

          // generate key agreement

          KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
          keyAgree.init(myKeyPair.getPrivate());


          keyAgree.doPhase(Initiator_DH_PUB_KEY,true);

          SecretKey aesSecret = keyAgree.generateSecret("AES");

          JSONObject dhCouplet = new JSONObject();

          dhCouplet.put("INITIATOR_DH_KEY",I_dh_key);
          dhCouplet.put("RECEIVER_DH_KEY",dh_pub);

          String dhCoupletStr = dhCouplet.toString();

          byte[] signed = RSAKeyPair.sign(priv,dhCoupletStr);

          String sign_str = Base64.encodeToString(signed,Base64.URL_SAFE);

          SecureRandom iv_random = new SecureRandom();

          byte[] iv_bytes = new byte[16];

          iv_random.nextBytes(iv_bytes);

          byte[] encrypted = RSAKeyPair.AESencrypt(sign_str,aesSecret.getEncoded(),iv_bytes);

          String iv_string = Base64.encodeToString(iv_bytes,Base64.URL_SAFE);

          String encrypted_string = Base64.encodeToString(encrypted,Base64.URL_SAFE);

          String secure_message_key = Base64.encodeToString(aesSecret.getEncoded(),Base64.URL_SAFE);


          // I_id = remote's id
          RSAKeyPair.saveAESKey(getApplicationContext(),mServiceUser.getId(),I_id,secure_message_key);



          // send my_pub_key_bytes

          JSONObject receiver_dh_rsa_pub_key_JSON = new JSONObject();

          receiver_dh_rsa_pub_key_JSON.put("rsa_pub_key",publicKeyEncoded);
          receiver_dh_rsa_pub_key_JSON.put("dh_pub_key",dh_pub);
          receiver_dh_rsa_pub_key_JSON.put("initiator_id",I_id);
          receiver_dh_rsa_pub_key_JSON.put("rec_id",R_id);
          receiver_dh_rsa_pub_key_JSON.put("enc_id",encrypted_string);
          receiver_dh_rsa_pub_key_JSON.put("iv",iv_string);
          receiver_dh_rsa_pub_key_JSON.put("action","request_reply");





          //===================================================
          SharedPreferences sharedPref = getSharedPreferences(
                  getString(R.string.user_details_prefs), MODE_PRIVATE);

          String serverUrl =sharedPref.getString(getString(R.string.stored_server_url),
                  "http://localhost:8080");


          List<NameValuePair> params = new LinkedList<NameValuePair>();
          //example query:
          //http://localhost:8080/openmrsMessage/sendMessage?
          // sid=23948234&
          // rid=10920934&
          // msg=hello+world+lol&
          // auth=dontcare
          params.add(new NameValuePair("action","request_reply"));
          params.add(new NameValuePair("sid",""+R_id));
          params.add(new NameValuePair("rid",""+I_id));
          params.add(new NameValuePair("msg",receiver_dh_rsa_pub_key_JSON.toString()));
          params.add(new NameValuePair("auth",mServiceUser.getAuth()));
          params.add(new NameValuePair("iv",iv_string));
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





          Intent intent = new Intent(this.NETWORK_NOTIFICATION);
          sendBroadcast(intent);

          createNotification(I_id,"Friend Request","Friend request from:"+I_id,ContactItem.class);


          //=====================================================


      } catch (JSONException e){

      } catch (InvalidKeyException e) {
          e.printStackTrace();
      } catch (NoSuchAlgorithmException e) {
          e.printStackTrace();
      } catch (InvalidAlgorithmParameterException e) {
          e.printStackTrace();
      } catch (InvalidKeySpecException e) {
          e.printStackTrace();
      } catch (Exception e) {
          e.printStackTrace();
      }


    }

    private void onFriendReplyRequest(JSONObject receiver_dh_rsa_pub_key_JSON){


        /*
          receiver_dh_rsa_pub_key_JSON.put("rsa_pub_key",publicKeyEncoded);
          receiver_dh_rsa_pub_key_JSON.put("dh_pub_key",dh_pub);
          receiver_dh_rsa_pub_key_JSON.put("initiator_id",I_id);
          receiver_dh_rsa_pub_key_JSON.put("rec_id",R_id);
          receiver_dh_rsa_pub_key_JSON.put("enc_id",encrypted_string);
          receiver_dh_rsa_pub_key_JSON.put("action","request_reply");

         */


        //Toast.makeText(this,"HELLO I GOT A FRIEND REQUEST REPLY",Toast.LENGTH_LONG).show();


        System.out.println("NetworkService: got friend reply request");

        try {
            String R_rsa_key = receiver_dh_rsa_pub_key_JSON.getString("rsa_pub_key");
            String R_dh_key = receiver_dh_rsa_pub_key_JSON.getString("dh_pub_key");
            long I_id = receiver_dh_rsa_pub_key_JSON.getLong("initiator_id"); // me
            long R_id = receiver_dh_rsa_pub_key_JSON.getLong("rec_id"); // them
            String iv_string = receiver_dh_rsa_pub_key_JSON.getString("iv");// their iv
            String DH_SIGNATURE = receiver_dh_rsa_pub_key_JSON.getString("enc_id"); // them

            byte[] REC_PUB_KEY_Initiator_Bytes = Base64.decode(R_dh_key,Base64.URL_SAFE);
            byte[] rec_rsa_public_key = Base64.decode(R_rsa_key,Base64.URL_SAFE);


            KeyFactory myKeyFactory = KeyFactory.getInstance("DH");
            X509EncodedKeySpec X509KeySpec = new X509EncodedKeySpec(REC_PUB_KEY_Initiator_Bytes);
            PublicKey REC_DH_PUB_KEY = myKeyFactory.generatePublic(X509KeySpec);



            //GET MY DH KEY

              // array { pub_key, priv_key}
            String[] res = RSAKeyPair.getDHKey(getApplicationContext(),mServiceUser.getId(),R_id);

            KeyFactory myPrivDHKeyFac = KeyFactory.getInstance("DH");
            byte[] DH_private_key_BYTES = Base64.decode(res[1],Base64.URL_SAFE);
            PKCS8EncodedKeySpec myPub = new PKCS8EncodedKeySpec(DH_private_key_BYTES);
            PrivateKey DH_Private_key = myPrivDHKeyFac.generatePrivate(myPub);






            // generate key agreement

            KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
            keyAgree.init(DH_Private_key);


            keyAgree.doPhase(REC_DH_PUB_KEY,true);

            SecretKey aesSecret = keyAgree.generateSecret("AES");

            String aes_string = Base64.encodeToString(aesSecret.getEncoded(),Base64.URL_SAFE);


            RSAKeyPair.saveAESKey(getApplicationContext(),mServiceUser.getId(),R_id,aes_string);



            Intent intent = new Intent(this.NETWORK_NOTIFICATION);
            sendBroadcast(intent);

            createNotification(R_id,"Friend Request Accepted","Friend accepted from:"+R_id,ContactItem.class);


        } catch (JSONException e){
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    public void sendItems(ArrayList<ContactItem> items){

            for( ContactItem cItem: items){

                cItem.setVisible(false);
                contactsTable.put(cItem.getId(),cItem);

            }

        Toast.makeText(getApplicationContext(),"Got "+ items.size() + " items",Toast.LENGTH_LONG).show();


    }

    public void sendChatVisibility(ContactItem mRemoteContact,boolean visibility){

        ContactItem contact = contactsTable.get(mRemoteContact.getId());
        if(contact != null){
            contact.setVisible(visibility);
            contactsTable.put(contact.getId(),contact);
        }



    }






    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void createNotification(long senderid, String title, String text, Class <?> cls){
        ContactItem contact = contactsTable.get(senderid);


        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text);
        // Creates an explicit intent for an Activity in your app

        Intent resultIntent = new Intent(this, cls);
        if (contact != null) resultIntent.putExtra("ContactName",contact);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ChatActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//              mId allows you to update the notification later on.
        mNotificationManager.notify(3, mBuilder.build());






    }








    private void deliverMessage(JSONObject jResponse){
            /*
                 resp.put("status", "success");
                resp.put("senderid",mSenderId);
                resp.put("recipientid", mRecipientId);
                resp.put("message", mMessage);



             */
            try{
                long sid =   Long.parseLong(jResponse.getString("senderid"));
                long rid =   Long.parseLong(jResponse.getString("recipientid"));
                String msg =  jResponse.getString("message");
                String iv_str = jResponse.getString("iv");
                String aes_key = RSAKeyPair.getAESKey(getApplicationContext(),mServiceUser.getId(),sid);

                byte[] msgBytes = Base64.decode(msg,Base64.URL_SAFE);
                byte[] ivBytes = Base64.decode(iv_str,Base64.URL_SAFE);
                byte[] aesKeyBytes = Base64.decode(aes_key,Base64.URL_SAFE);

                String plainMsg = "decrypt error";
                try {
                    byte[] plainenc = RSAKeyPair.AESdecrypt(msgBytes, aesKeyBytes, ivBytes);
                   plainMsg = new String(plainenc);

                }  catch (Exception e){
                    e.printStackTrace();

                }






                MessageBlock msgBlock = new MessageBlock(sid,rid,plainMsg, System.currentTimeMillis());

                SharedPrefsHelper.saveMessageOnBackground(getApplicationContext(),msgBlock);

                Intent intent = new Intent(this.NEW_MESSAGE);
                intent.putExtra("message",msgBlock);
                sendBroadcast(intent);

                ContactItem item = contactsTable.get(sid);
                if(item != null){
                    item.getUn();
                    if (!item.isVisible()){
                        createNotification(sid,item.getUn(),"New message from " + item.getUn(),ChatActivity.class);
                    }

                }

            }
            catch (JSONException e){
                e.printStackTrace();
            }


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return mStartMode;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        /*
            checkconnections();

            request_posts(intent);
            return mBinder;
         */

        /*

         For this method. When an activitiy
         is bound. Verify that the app is connected
         to the client.
         In addition ... verify that the app's login
         credentials are correct and up to date

          ie. logintoken, username logged in etc

          if there is an issue with the connection
          or user info is not correct/ not logged
          in... broadcast error code

         */
        // update userinfo... especially remote server URL

        try{
            mServiceUser = SharedPrefsHelper.loadUserDataFromPrefs(getApplicationContext());
        } catch (Exception e){
            Log.d("onStartCommand: ",e.getLocalizedMessage(),e);
        }

            long id = mServiceUser.getId();



        MessageBlock block = messages.get(id);
        //---------------------------//
        if (block != null){
                Intent rIntent = new Intent(this.NEW_MESSAGES);
                rIntent.putExtra("messages",block);
                sendBroadcast(rIntent);
        }
        mCount++;


        try{
            ContactItem item = (ContactItem)intent.getExtras().getSerializable("ContactName");
            if (item !=  null){

                ContactItem cItem = contactsTable.get(item.getId());
                if(cItem != null){
                    cItem.setVisible(true);
                    contactsTable.put(cItem.getId(),cItem);
                }


            }
        } catch (Exception e){

        }

        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {

        // All clients have unbound with unbindService()
        mCount--;

        return true;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
        // do samething as onBind
        onBind(intent);
    }
    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        //make sure connections are closed
        // set loop conditions to false
        MaintainConnection = false;
        unregisterReceiver(logout_receiver);

        unregisterReceiver(contact_receiver);
        unregisterReceiver(visibility_receiver);
    }

    private class DataRequestThread extends Thread {

        private String mUrlString;
        private String mUrlResource;
        private String mUrlRequest;
        DataRequestThread(String urlResource) {
            super();
            mUrlString =mServiceUser.getServerUrl();
            mUrlResource = urlResource;
            List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new NameValuePair("userid",""+mServiceUser.getId()));
            params.add(new NameValuePair("auth",mServiceUser.getAuth()));
            mUrlRequest = HttpPost.buildParamString(params);
            // hopefully gc'ed soon
            params = null;
        }


        public void run() {

            // start server connection

            //example query:
            //http://localhost:8080/openmrsMessage/GetMessage?
            // sid=23948234&
            // rid=10920934&
            // msg=hello+world+lol&
            // auth=dontcare

            //"http://10.0.0.16:8080/openmrsMessage/login"

            while (MaintainConnection) {

                String response = getMessage();
                try{
                    if ( (response == null) | response.equals(FAIL_RESPONSE) ){
                        return;
                    }
                } catch (NullPointerException e){

                }

                processResponse(response);

            }


        }



        protected String getMessage(){

            String serverResponse = null;
            long responseCode = -51278;

            DataOutputStream wr = null;
            BufferedReader in = null;


            URL url;
            try {
                url = new URL(mUrlString + mUrlResource);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                //connection.setChunkedStreamingMode(0);

                //connection.setRequestMethod("GET");
                // no space in param args
                wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(mUrlRequest);
                wr.flush();
                wr.close();

                responseCode = connection.getResponseCode();
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                System.out.println("NetworkService: waiting for "+url.getHost()+ " "+mUrlRequest);

                // block
                serverResponse = in.readLine();

                System.out.println("Responsecode = "+responseCode);
                if(serverResponse != null ) System.out.println(serverResponse);

            } catch (MalformedURLException malUrl) {
                malUrl.printStackTrace();


            } catch (IOException e) {

            } finally {
                try{
                    if (wr != null){
                        wr.close();
                    }
                    if (in != null){
                        in.close();
                    }

                    url = null;
                }
                catch (IOException e){
                    e.printStackTrace();
                }

            }

            return serverResponse;
        }




    }

    public void onLogOut(Intent intent){
        SharedPreferences sharedPref =
                getSharedPreferences(
                        getString(R.string.user_details_prefs), MODE_PRIVATE
                );

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();

        MaintainConnection = false;
        stopSelf();
    }

    public void connect(Intent intent){

    }

    private BroadcastReceiver logout_receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
           onLogOut(null);
        }
    };

    private BroadcastReceiver contact_receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String  contacts = intent.getExtras().getString("ContactName");

            try {
                JSONArray jary = new JSONArray(contacts);
                ArrayList<ContactItem> t = parseContacts(jary);
                sendItems(t);
            } catch(JSONException e){

            }

        }
    };

    private BroadcastReceiver visibility_receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            long sid = intent.getExtras().getLong("fid");
            boolean isVisible = intent.getBooleanExtra("visibility",false);

            ContactItem cItem = contactsTable.get(sid);
            if (cItem != null){
                cItem.setVisible(isVisible);
                contactsTable.put(sid,cItem);
            }

        }
    };





    public  ContactItem parseContact(JSONObject jObj){
        // format of json object
        //{"username":"hp7books","userid":23948234,"lastname":"Potter","firstname":"Harry"}

        try{

            String username = jObj.getString("username");
            String userId = jObj.getString("userid");
            String lastname = jObj.getString("lastname");
            String firstname = jObj.getString("firstname");

            ContactItem item = new ContactItem(
                    null,
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

    public ArrayList<ContactItem> parseContacts(JSONArray jAry){

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

}
