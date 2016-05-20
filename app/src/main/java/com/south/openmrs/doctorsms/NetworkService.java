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

    Hashtable<Long,MessageBlock> messages;


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
        try{
            mServiceUser = SharedPrefsHelper.loadUserDataFromPrefs(getApplicationContext());
        } catch (Exception e){
            Log.d("onStartCommand: ",e.getLocalizedMessage(),e);
        }

        new DataRequestThread().start();
        //TODO: Implement additional methods if necessary
    }



    private void processResponse(String networkResponse){

        JSONObject jResponse;
        try{
            jResponse = new JSONObject(networkResponse);

            String status = jResponse.getString("status");

            if( "fail".equals(status)){


                System.out.println("Recieving says fail");


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

                MessageBlock msgBlock = new MessageBlock(sid,rid,msg);
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
    }



    private class DataRequestThread extends Thread {
        boolean MaintainConnection = true;

        DataRequestThread() {
            super();
        }


        public void run() {
            HttpPost serverConnection;

            // start server connection

            //example query:
            //http://localhost:8080/openmrsMessage/GetMessage?
            // sid=23948234&
            // rid=10920934&
            // msg=hello+world+lol&
            // auth=dontcare

            //"http://10.0.0.16:8080/openmrsMessage/login"
            String urlResource = "/openmrsMessage/GetMessage";
            List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new NameValuePair("userid",""+mServiceUser.getId()));
            params.add(new NameValuePair("auth",mServiceUser.getAuth()));

            while (MaintainConnection) {

               HttpPost mServerListener = new HttpPost(NetworkService.this, mServiceUser.getServerUrl(),urlResource){




                    @Override
                    protected void onPostExecute(String response) {
                        processResponse(response);
                    }
                };
                mServerListener.execute(params);


            }


        }
    }

    public void connect(Intent intent){

    }



}
