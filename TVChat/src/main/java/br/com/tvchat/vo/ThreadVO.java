package br.com.tvchat.vo;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ThreadVO implements Serializable{
	private static final long serialVersionUID = 1829090222716997615L;
	
	private String id;
	private UserInfoVO userInfo;
	private String message;
    private String channel;
	private int commentNumber;
	private long time;

    private List<String> notUserComments;
	
	public ThreadVO(){
		notUserComments = new ArrayList<String>();
	}
	
	public ThreadVO(String channel, String id, UserInfoVO userInfo, String message, long time, int commentNumber){
		this.channel = channel;
        this.id = id;
		this.userInfo = userInfo;
		this.message = message;
		this.commentNumber = commentNumber;
		this.time = time;
	}

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel){
        this.channel = channel;
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
		
	public long getTime(){
		return time;
	}
	
	public void setTime(long time){
		this.time = time;
	}
	
	public int getCommentNumber(){
		return commentNumber;
	}
	
	public void setCommentNumber(int commentNumber){
		this.commentNumber = commentNumber;
	}

    public List<String>  getNotUserComments(){
        return notUserComments;
    }
	
	public boolean equals(Object obj){
		if (obj instanceof ThreadVO){
			return (id!=null&&id.equals(((ThreadVO)obj).getId()));
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
            json.put("channel", channel);
            json.put("message", message);

            return json;
        } catch (JSONException jsone){
            Log.e("ERR", jsone.getMessage(), jsone);
            return null;
        }
    }

    public static ThreadVO toObject(JSONObject json){
        ThreadVO thread = null;
        try {
            thread = new ThreadVO();
            thread.setChannel(json.optString("channel", ""));
            thread.setId(json.optString("id", ""));
            if (json.has("user")) {
                UserInfoVO userInfo = new UserInfoVO();
                thread.setUserInfo(userInfo);
                userInfo.setUserId(json.getJSONObject("user").optString("id", ""));
                userInfo.setEmail(json.getJSONObject("user").optString("email", ""));
                userInfo.setFirstName(json.getJSONObject("user").optString("firstName", ""));
                userInfo.setLastName(json.getJSONObject("user").optString("lastName", ""));
                userInfo.setFullName(json.getJSONObject("user").optString("fullName", ""));
            }
            thread.setMessage(json.optString("message", ""));
            thread.setTime(json.optLong("time", 0));
            thread.setCommentNumber(0);

            JSONArray commentsKey = json.getJSONArray("commentKeys");
            for (int i = 0; i < commentsKey.length(); i++) {
                thread.notUserComments.add(commentsKey.getString(i));
            }
        } catch (JSONException jsone){
            Log.e("JSON", jsone.getMessage(), jsone);
        }

        return thread;
    }
}
