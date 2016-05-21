package com.south.openmrs.doctorsms;



public class MessageItem extends Item {
	public String username;
	public String msg;
	long mSid;
	long mRid;
	long timeMillis;
	boolean incoming;

	public MessageItem(User mCurrentUser, ContactItem friend, MessageBlock block){

		mSid = block.getSenderId();
		mRid = block.getRecipient();
		msg = block.getMessage();
		timeMillis = block.getTime();

		if (mCurrentUser.getId() == mSid){
			incoming = false;
			username = mCurrentUser.getUn();
		}
		else {
			incoming = true;
			username = friend.getUn();
		}

	}

	
	String postDisplayInfo(){
		String s =  (username + ": " + msg);
		return s;
	}


}
