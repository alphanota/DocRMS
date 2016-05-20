package com.south.openmrs.doctorsms;

import android.net.Uri;

/**
 * Created by angel on 12/27/15.
 */
public class ImageItem extends Item {


    Uri thumbnailURI;
    Uri originalURI;
    MessageItem optional_message;

    ImageItem(Uri thumb, Uri orig){
        this.thumbnailURI = thumb;
        this.originalURI = orig;
    }


    public void setOptionalMessage(MessageItem item){
        optional_message = item;
    }


}
