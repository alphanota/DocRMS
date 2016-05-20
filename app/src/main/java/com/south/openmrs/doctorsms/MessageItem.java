package com.south.openmrs.doctorsms;



public class MessageItem extends Item {
	public String username;
	public String msg;

	MessageItem(String userName, String msg){
		this.username = userName;
		this.msg = msg;

	}

	public MessageItem(User mCurrentUser, ContactItem mRemoteContact, String message) {
		this.username = mCurrentUser.getUn();
		this.msg = message;
	}

	String postInfo(){
        
		return "";
		
	}
	
	String postDisplayInfo(){
		
		String s =  (username + ": " + msg);
		
		return s;
	}


}
