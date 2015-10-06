package br.com.tvchat.vo;

import android.graphics.Bitmap;

import java.io.Serializable;

public class UserInfoVO implements Serializable{
	private static final long serialVersionUID = -5104558847774003732L;
	
	private String userId;
	private Bitmap profileImage;
    private String email;
	private String firstName;
    private String lastName;
    private String fullName;

	
	public UserInfoVO(String userId, Bitmap profileImage, String email, String firstName, String lastName, String fullName){
		this.userId = userId;
		this.profileImage = profileImage;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
		this.fullName = fullName;
	}
	
	public UserInfoVO(){}
	
	public String getUserId(){
		return userId;
	}
	
	public void setUserId(String userId){
		this.userId = userId;
	}
	
	public Bitmap getProfileImage(){
		return profileImage;
	}
	
	public void setProfileImage(Bitmap profileImage){
		this.profileImage = profileImage;
	}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getFirstName(){
        return firstName;
    }

    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public void setLastName(String lastName){
        this.lastName = lastName;
    }
	
	public String getFullName(){
		return fullName;		
	}
	
	public void setFullName(String fullName){
		this.fullName = fullName;
	}
}
