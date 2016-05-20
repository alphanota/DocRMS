package com.south.openmrs.doctorsms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.LruCache;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    TextView usernameView;
    TextView passwordView;
    TextView serverURlView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        Button  loginButton = (Button)findViewById(R.id.login_submit);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkVerifyCredentials();
            }
        });
    }

    public void networkVerifyCredentials(){

        usernameView = (TextView)findViewById(R.id.username_field);
        passwordView = (TextView)findViewById(R.id.password_field);
        serverURlView = (TextView) findViewById(R.id.server_url);

        String unStr = usernameView.getText().toString();
        String pwStr = passwordView.getText().toString();
        final String serverUrl = serverURlView.getText().toString();

        List<NameValuePair> params = new LinkedList<NameValuePair>();

        params.add(new NameValuePair("userName",unStr));
        params.add(new NameValuePair("password",pwStr));
        //"http://10.0.0.16:8080/openmrsMessage/login"
        String urlResource = "/openmrsMessage/login";
        //HttpPost post = new HttpPost(this, url);
        //post.execute(params);

        HttpPost mPost = new HttpPost(this,serverUrl,urlResource){
            @Override
            protected void onPostExecute(String response) {
                if (response != null) {
                    System.out.println(response);
                    JSONObject loginResult = null;
                    try{
                         loginResult = new JSONObject(response);


                        String loginStatus = loginResult.getString("status");



                        if (loginStatus.equals("sucessful")){

                            Intent mainActivityLauncher = new Intent(parentContext,ContactsActivity.class);
                            mainActivityLauncher.putExtra("networkresponse",response);
                            mainActivityLauncher.putExtra("serverurl",mServerUrl);
                            Toast.makeText(parentContext, response, Toast.LENGTH_SHORT).show();


                            SharedPreferences sharedPref = parentContext.getSharedPreferences(
                                    getString(R.string.user_details_prefs), parentContext.MODE_PRIVATE);

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(getString(R.string.stored_user_token), loginResult.getString("authtoken"));
                            editor.putString(getString(R.string.stored_username), loginResult.getString("username"));
                            editor.putString(getString(R.string.stored_user_userid), loginResult.getString("userid"));
                            editor.putString(getString(R.string.stored_user_firstname), loginResult.getString("firstname"));
                            editor.putString(getString(R.string.stored_user_lastname), loginResult.getString("lastname"));
                            editor.putString(getString(R.string.stored_server_url), mServerUrl);


                            editor.commit();


                            parentActivity.startActivity(mainActivityLauncher);
                            //parentContext.startActivity();


                        }

                        else{
                            Toast.makeText(parentContext, "Login fail " + loginResult, Toast.LENGTH_SHORT).show();


                        }




                    } catch (JSONException e){
                        e.printStackTrace();
                    }



                }
            }
        };
        mPost.execute(params);


    }

    public void startContactsActivity(String networkResponse){

    }

}
