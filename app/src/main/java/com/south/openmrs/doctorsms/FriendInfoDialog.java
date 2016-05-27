package com.south.openmrs.doctorsms;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by angel on 5/26/16.
 */
public class FriendInfoDialog extends Dialog {

    TextView username;
    TextView fullname;
    TextView id;



    Button okButton;

    FriendInfoDialog(final Context context, ContactItem friend,
                     User user,
                     String friend_dhKey, String friend_rsaKey,
                     String your_dhKey, String your_rsaKey){
        super(context);

        setTitle("User information");
        setContentView(R.layout.friend_info_layout);

        TextView friendDhKeyView = (TextView)findViewById(R.id.their_dh_key_info);
        TextView friendDhKeyPromptView = (TextView)findViewById(R.id.user_info_friend_dh_text);

        TextView friendRSAKeyView = (TextView)findViewById(R.id.their_rsa_key_info);
        TextView friendRSAKeyPromptView = (TextView)findViewById(R.id.user_info_friend_rsa_text);

        TextView yourDhKeyView = (TextView)findViewById(R.id.your_dh_key_info);
        TextView yourRSAKeyView = (TextView)findViewById(R.id.your_rsa_key_info);


        friendDhKeyView.setText(friend_dhKey);
        friendDhKeyPromptView.setText(friend.getUn() + " DH key info");

        friendRSAKeyView.setText(friend_rsaKey);
        friendRSAKeyPromptView.setText(friend.getUn() + " RSA key info");


        yourDhKeyView.setText(your_dhKey);
        yourRSAKeyView.setText(your_rsaKey);

        Button okButton = (Button) findViewById(R.id.user_info_button_dismiss);
        okButton.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View view){

                dismiss();
            }
        });

    }

}
