package com.south.openmrs.doctorsms;

import java.util.LinkedList;

public class MessageStore {
	
	
	private Long recipientId;
	
	private LinkedList<MessageBlock> messages;
	//private LinkedList<MessageBlock> unFetchedMessages;

	MessageStore(Long p){
		recipientId = p;
		messages = new LinkedList<MessageBlock>();
	}
	
	
	public void push(MessageBlock msg){
		messages.push(msg);
	}
	
	
	
	public long getRecipientId(){
		return recipientId;
	}
	
	
	private LinkedList<MessageBlock> getMessage(){
		return messages;
	}
	
	public MessageBlock getAMessage(){
		if (messages.isEmpty()) return null;
		else return messages.pop();
	}
}
