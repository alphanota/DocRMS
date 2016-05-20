package com.south.openmrs.doctorsms;


import org.json.JSONException;
import org.json.JSONObject;

public class User {
	
	private long mUserId; // primary key
	private String mFirstName;
	private String mLastName;
	private String mUsername;
	private String mAuthToken;
	private String mServerUrl;
	
	
	
	
	public User(long id, String fn, String ln, String un, String auth){
		mUserId = id;
		mFirstName = fn;
		mLastName = ln;
		mUsername = un;
		mAuthToken = auth;
	}
	
	public long getId(){
		return mUserId;
	}
	
	public String getAuth(){
        return mAuthToken;
    }

    public String getFn(){
        return mFirstName;
    }

    public String getLn(){
        return mLastName;
    }

    public String getUn(){
        return mUsername;
    }


	public void setServerUrl(String url){
		mServerUrl = url;
	}

	public String getServerUrl(){
		return mServerUrl;
	}
	
	public JSONObject toJSONString(){
		JSONObject object;
		JSONObject resp = new JSONObject();
		try{
			resp.put("userid",mUserId);
			resp.put("firstname", mFirstName);
			resp.put("lastname", mLastName);
			resp.put("username", mUsername);
            resp.put("auth", mAuthToken);


		} catch (JSONException e){
			return null;
		}
		return resp;
		
	}
	
	
	

}
