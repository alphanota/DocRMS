package com.south.openmrs.doctorsms;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by angel on 5/17/16.
 */
public class SharedPrefsHelper {


    public static User loadUserDataFromPrefs(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.user_details_prefs), context.MODE_PRIVATE);
         String sAuth=      context.getString(R.string.stored_user_token);


        String storedUserToken =sharedPref.getString(context.getString(R.string.stored_user_token),"0");
        String storedUsername = sharedPref.getString(context.getString(R.string.stored_username),"0");
        String userIdStr =      sharedPref.getString(context.getString(R.string.stored_user_userid), "0");
        String storedFirstName =sharedPref.getString(context.getString(R.string.stored_user_firstname), "0");
        String storedLastname  =sharedPref.getString(context.getString(R.string.stored_user_lastname), "0");
        String storedServerUrl = sharedPref.getString(context.getString(R.string.stored_server_url), "0");
        long userIdLong = Long.parseLong(userIdStr);

        User user = new User(userIdLong,storedFirstName,storedLastname,storedUsername,storedUserToken);
        user.setServerUrl(storedServerUrl);
        return user;
    }


}
