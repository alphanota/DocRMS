package com.south.openmrs.doctorsms;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by angel on 4/30/16.
 */
public class HttpPost extends AsyncTask<List<NameValuePair>, Integer, String>{

    Context parentContext;
    String mUrlResource;
    String mServerUrl;
    Activity parentActivity;

    HttpPost(Context context, String serverUrl, String urlResource){
        parentContext = context;
        mUrlResource = urlResource;
        mServerUrl = serverUrl;
        try {
            parentActivity = (Activity) context;
        }
        catch (Exception e){
            parentActivity = null;
        }
    }

    @Override
    protected String doInBackground(List<NameValuePair>... mParams) {
        return doServerRequest(mParams);
    }

    protected String doServerRequest(List<NameValuePair>... mParams){
        String serverResponse = null;
        long responseCode = -51278;

        List<NameValuePair> params = mParams[0];
        DataOutputStream wr = null;
        BufferedReader in = null;


        URL url;
        try {
            url = new URL(mServerUrl + mUrlResource);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            //connection.setChunkedStreamingMode(0);

            connection.setRequestMethod("GET");
            // no space in param args
            String urlParameters = buildParamString(params);

            wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            responseCode = connection.getResponseCode();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            serverResponse = in.readLine();

            System.out.println("Reponsecode = "+responseCode);
            //System.out.println(urlParameters);
            //System.out.println(serverResponse);

        } catch (MalformedURLException malUrl) {
            malUrl.printStackTrace();
            return null;

        } catch (IOException e) {

        } finally {
                try{
                    if (wr != null){
                        wr.close();
                    }
                    if (in != null){
                        in.close();
                    }
                }
                catch (IOException e){

                }


        }


        return serverResponse;
    }

    public static String buildParamString( List<NameValuePair> params){
        // "userName=" + username + "&password=" + password;
        StringBuilder builder = new StringBuilder();
        boolean secondParam = false;


        NameValuePair arg = params.get(0);
        builder.append(arg.name + "=" + arg.value);

        System.out.println("param "+0 +" " + arg.name + "=" + arg.value );

        for(int i =1; i < params.size(); i++){
            arg = params.get(i);
            builder.append("&");
            builder.append(arg.name + "=" + arg.value);
            System.out.println("param "+i +" " + arg.name + "=" + arg.value );

        }
        /*
        while( !params.isEmpty() ) {
            NameValuePair arg = params.remove(0);
            if (secondParam){
                builder.append("&");
            }
            builder.append(arg.name + "=" + arg.value);
            secondParam = true;
        }
        */
        String str = builder.toString();

        System.out.println("stringbuilder: " + str);

        return str;
    }

    @Override
    protected void onPostExecute(String response) {
            if (response != null) {
                System.out.println(response);
                Toast.makeText(parentContext, response, Toast.LENGTH_SHORT).show();
            }
    }


}
