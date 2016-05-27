package com.south.openmrs.doctorsms;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by angel on 5/26/16.
 */
public class UserInfoDialog extends Dialog {

    TextView username;
    TextView fullname;
    TextView id;



    Button okButton;

    UserInfoDialog(final Context context, User user){
        super(context);

        setTitle("User information");
        setContentView(R.layout.user_info);

        TextView name = (TextView) findViewById(R.id.user_info_name);
        TextView userid = (TextView) findViewById(R.id.user_info_userid);
        TextView username = (TextView) findViewById(R.id.user_info_name);

        name.setText(user.getFn() + " " + user.getLn());
        userid.setText(""+user.getId());
        username.setText(user.getUn());

        Button okButton = (Button) findViewById(R.id.user_info_button_dismiss);
        okButton.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View view){

                dismiss();
            }
        });

    }

}
