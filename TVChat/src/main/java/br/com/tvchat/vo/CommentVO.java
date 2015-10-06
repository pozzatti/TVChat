package br.com.tvchat.vo;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class CommentVO implements Serializable{
	private static final long serialVersionUID = -3923149533639795437L;
    private String threadId;
    private String threadUserId;
	private String id;
	private UserInfoVO userInfo;
	private String message;
	private Long time;
	
	public CommentVO(){}
	
	public CommentVO(String id, UserInfoVO userInfo, String message, Long time){
		this.id = id;
		this.userInfo = userInfo;
		this.message = message;
		this.time = time;
	}

    public String getThreadId(){
        return threadId;
    }

    public void setThreadId(String threadId){
        this.threadId = threadId;
    }

    public String getThreadUserId() {
        return threadUserId;
    }

    public void setThreadUserId(String threadUserId){
        this.threadUserId = threadUserId;
    }

	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public UserInfoVO getUserInfo(){
		return userInfo;
	}
	
	public void setUserInfo(UserInfoVO userInfo){
		this.userInfo = userInfo;
	}
	
	public String getMessage(){
		return message;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public Long getTime(){
		return time;
	}
	
	public void setTime(Long time){
		this.time = time;
	}
	
	public boolean equals(Object obj){
		if (obj instanceof CommentVO){
			return (id!=null&&id.equals(((CommentVO)obj).getId()));
		}
		return false;
	}
	
	public int hashCode(){
		return (id!=null?id.hashCode():0);
	}

    public JSONObject toJSONObject(){
        try {
            JSONObject json = new JSONObject();
            JSONObject facebookUser = new JSONObject();
            facebookUser.put("id", userInfo.getUserId());
            facebookUser.put("firstName", userInfo.getFirstName());
            facebookUser.put("lastName", userInfo.getLastName());
            facebookUser.put("fullName", userInfo.getFullName());
            facebookUser.put("email", userInfo.getEmail());
            json.put("id", id);
            json.put("user", facebookUser);
            json.put("threadId", threadId);
            json.put("threadUserId", threadUserId);
            json.put("message", message);

            return json;
        } catch (JSONException jsone){
            Log.e("JSON", jsone.getMessage(), jsone);
            return null;
        }
    }

    public static CommentVO toObject(JSONObject json){
        CommentVO comment = null;
        try {
            comment = new CommentVO();
            comment.setThreadId(json.optString("threadId", ""));
            comment.setThreadUserId(json.optString("threadUserId", ""));
            comment.setId(json.optString("id", ""));
            if (json.has("user")) {
                UserInfoVO userInfo = new UserInfoVO();
                comment.setUserInfo(userInfo);
                userInfo.setUserId(json.getJSONObject("user").optString("id", ""));
                userInfo.setEmail(json.getJSONObject("user").optString("email", ""));
                userInfo.setFirstName(json.getJSONObject("user").optString("firstName", ""));
                userInfo.setLastName(json.getJSONObject("user").optString("lastName", ""));
                userInfo.setFullName(json.getJSONObject("user").optString("fullName", ""));
            }
            comment.setMessage(json.optString("message", ""));
            comment.setTime(json.optLong("time", 0));
        } catch (JSONException jsone){
            Log.e("JSON", jsone.getMessage(), jsone);
        }

        return comment;
    }
}
