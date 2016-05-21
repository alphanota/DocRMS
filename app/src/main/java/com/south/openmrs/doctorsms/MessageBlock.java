package com.south.openmrs.doctorsms;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class MessageBlock implements Serializable {
	
	
	private final long mSenderId;
	private final long mRecipientId;
	private final String mMessage;
	private final long mTime;
	
	
	MessageBlock(long sid, long rid, String msg, long time){
		mSenderId = sid;
		mRecipientId = rid;
		mMessage = msg;
		mTime = time;
	}
	
	public JSONObject toJSON(){
		JSONObject resp = new JSONObject();
		try{
			resp.put("status", "sucessfull");
			resp.put("senderid",mSenderId);
			resp.put("recipientid", mRecipientId);
			resp.put("message", mMessage);
			resp.put("time", mTime);
		} catch (JSONException e){

		}
		return resp;
		
	}
	
	public long getRecipient(){
		return mRecipientId;
	}
    public long getSenderId(){
        return mSenderId;
    }
	public  long getTime() {return mTime;}
	public String toJSONString(){
		return toJSON().toString();
	}

    public String getMessage(){
        return mMessage;
    }
	
	
}
