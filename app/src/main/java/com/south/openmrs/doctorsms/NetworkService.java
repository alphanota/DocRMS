package com.south.openmrs.doctorsms;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;



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
    boolean MaintainConnection = true;
    Hashtable<Long,MessageBlock> messages;
    protected static final String FAIL_RESPONSE = "No post parameters";


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
        }
        String urlResource = "/openmrsMessage/GetMessage";
        new DataRequestThread(urlResource).start();
        //TODO: Implement additional methods if necessary
    }



    private void processResponse(String networkResponse){

        JSONObject jResponse;
        try{
            jResponse = new JSONObject(networkResponse);

            String status = jResponse.getString("status");

            if( "fail".equals(status)){


                System.out.println("Receiving says fail");


            }

            else if ("success".equals(status)){

                deliverMessage(jResponse);

            }




        }  catch(JSONException e){

        }
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

                MessageBlock msgBlock = new MessageBlock(sid,rid,msg, System.currentTimeMillis());

                SharedPrefsHelper.saveMessageOnBackground(getApplicationContext(),msgBlock);





                Intent intent = new Intent(this.NEW_MESSAGE);
                intent.putExtra("message",msgBlock);
                sendBroadcast(intent);

            }
            catch (JSONException e){

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





            long id = intent.getLongExtra("senderid",0L);



        MessageBlock block = messages.get(id);
        if (block != null){

                Intent rIntent = new Intent(this.NEW_MESSAGES);
                rIntent.putExtra("messages",block);
                sendBroadcast(rIntent);


        }

        mCount++;
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        mCount--;
        return mAllowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called

        // do samething as onBind
    }
    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed

        //make sure connections are closed
        // set loop conditions to false
        MaintainConnection = false;
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

    public void connect(Intent intent){

    }



}
