package com.south.openmrs.doctorsms;

import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by angel on 4/30/16.
 */
public class NetworkCommunicator {

    public static void networkLogin(String username, String password){

        URL url = null;
        try {
            url = new URL("http://10.255.177.202:8080/openmrsMessage");
        } catch(MalformedURLException e){

        }




    }









}
