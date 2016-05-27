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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    TextView usernameView;
    TextView passwordView;
    AutoCompleteTextView serverURlView;
    SharedPreferences sharedPref;
    ArrayList<String> urlChoices;
    ArrayAdapter<String> urlAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        Button  loginButton = (Button)findViewById(R.id.login_submit);
        sharedPref = getSharedPreferences(
                getString(R.string.user_details_prefs), this.MODE_PRIVATE);

        String def = sharedPref.getString("userinfo",null);

        if(def != null){

            JSONObject userinfo;

            try{

                userinfo = new JSONObject(def);

                String token = userinfo.getString(getString(R.string.stored_user_token));
                String un = userinfo.getString(getString(R.string.stored_username));
                String uid = userinfo.getString(getString(R.string.stored_user_userid));
                String fn = userinfo.getString(getString(R.string.stored_user_firstname));
                String ln = userinfo.getString(getString(R.string.stored_user_lastname));
                String serverUrl = userinfo.getString(getString(R.string.stored_server_url));
                /**
                if ((token != null) && (un != null) &&
                        (uid != null) && (fn != null) && (ln != null) && (serverUrl != null)
                {
                }**/

                Intent mainActivityLauncher = new Intent(getApplicationContext(),ContactsActivity.class);
                mainActivityLauncher.putExtra("networkresponse",def);
                mainActivityLauncher.putExtra("serverurl",serverUrl);
                Toast.makeText(getApplicationContext(), def, Toast.LENGTH_SHORT).show();
                mainActivityLauncher.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainActivityLauncher);



            } catch (JSONException e){

            }




        }


        urlChoices = new ArrayList<String>(sharedPref.getStringSet("SAVED_URLS",new HashSet<String>()));
        urlAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_dropdown_item_1line,urlChoices);

        serverURlView = (AutoCompleteTextView) findViewById(R.id.server_url);
        serverURlView.setThreshold(2);
        serverURlView.setAdapter(urlAdapter);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkVerifyCredentials();
            }
        });


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_item_register:
                showRegisterDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showRegisterDialog(){
        Toast.makeText(getApplicationContext(),"Register now",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,RegisterDialogActivity.class);
        startActivity(intent);
    }

    public void networkVerifyCredentials(){

        usernameView = (TextView)findViewById(R.id.username_field);
        passwordView = (TextView)findViewById(R.id.password_field);
        //serverURlView = (AutoCompleteTextView) findViewById(R.id.server_url);

        String unStr = usernameView.getText().toString();
        String pwStr = passwordView.getText().toString();
        final String serverUrl = serverURlView.getText().toString();

        List<NameValuePair> params = new LinkedList<NameValuePair>();

        params.add(new NameValuePair("userName",unStr));
        params.add(new NameValuePair("password",pwStr));
        //"http://10.0.0.16:8080/openmrsMessage/login"
        final String urlResource = "/openmrsMessage/login";
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
                            mainActivityLauncher.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            Toast.makeText(parentContext, response, Toast.LENGTH_SHORT).show();




                            SharedPreferences.Editor editor = sharedPref.edit();

                            if(!urlChoices.contains(mServerUrl)){
                                urlChoices.add(mServerUrl);
                                editor.putStringSet("SAVED_URLS",new HashSet<String>(urlChoices) );
                            }

                            editor.putString(getString(R.string.stored_user_token), loginResult.getString("authtoken"));
                            editor.putString(getString(R.string.stored_username), loginResult.getString("username"));
                            editor.putString(getString(R.string.stored_user_userid), loginResult.getString("userid"));
                            editor.putString(getString(R.string.stored_user_firstname), loginResult.getString("firstname"));
                            editor.putString(getString(R.string.stored_user_lastname), loginResult.getString("lastname"));
                            editor.putString(getString(R.string.stored_server_url), mServerUrl);

                            loginResult.put(getString(R.string.stored_server_url), mServerUrl);
                            editor.putString("userinfo",loginResult.toString());


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
