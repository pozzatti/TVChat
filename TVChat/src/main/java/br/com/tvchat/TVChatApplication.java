package br.com.tvchat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.androidquery.AQuery;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

import br.com.tvchat.contants.TVChatConstants;
import br.com.tvchat.util.TVChatUtil;
import br.com.tvchat.vo.UserInfoVO;

public class TVChatApplication extends Application{
	private UserInfoVO userInfo;
    private Class<? extends Activity> activity;
    private String threadId;

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    private HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    @Override
    public void onCreate() {
        registerActivityLifecycleCallbacks(new MyLifecycleHandler());
    }

    public TVChatApplication() {
		super();
	}
	
	public UserInfoVO getUserInfo(){
        if (userInfo == null){
            SharedPreferences preferences = getSharedPreferences(TVChatConstants.PREFERENCES_BUNDLE, Context.MODE_PRIVATE);
            String facebookId = preferences.getString(TVChatConstants.FACEBOOK_ID, "");
            if (!facebookId.isEmpty()) {
                userInfo = new UserInfoVO();
                userInfo.setUserId(facebookId);
                userInfo.setFirstName(preferences.getString(TVChatConstants.FACEBOOK_FIRSTNAME, ""));
                userInfo.setLastName(preferences.getString(TVChatConstants.FACEBOOK_LASTNAME, ""));
                userInfo.setFullName(preferences.getString(TVChatConstants.FACEBOOK_FULLNAME, ""));
                userInfo.setEmail(preferences.getString(TVChatConstants.FACEBOOK_EMAIL, ""));

                TVChatUtil.populateProfileImage(new AQuery(this), userInfo);
            };
        }

		return userInfo;		
	}

    public void setActiveActivity(Class<? extends Activity> activity, String threadId){
        this.activity = activity;
        this.threadId = threadId;
    }

    public boolean isActivityActive(Class<? extends Activity> activity, String threadId){
        boolean ret = false;
        if (this.activity!=null && activity != null && this.activity.equals(activity)){
            ret = true;

            if (activity!=null && activity.getSimpleName().equals("CommentsActivity")){
                ret = (this.threadId!=null?this.threadId.equals(threadId):threadId==null);
            }
        }

        return ret;
    }
	
	public void setUserInfo(UserInfoVO userInfo){
        SharedPreferences preferences = getSharedPreferences(TVChatConstants.PREFERENCES_BUNDLE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(TVChatConstants.FACEBOOK_ID, userInfo.getUserId());
        editor.putString(TVChatConstants.FACEBOOK_FIRSTNAME, userInfo.getFirstName());
        editor.putString(TVChatConstants.FACEBOOK_LASTNAME, userInfo.getLastName());
        editor.putString(TVChatConstants.FACEBOOK_FULLNAME, userInfo.getFullName());
        editor.putString(TVChatConstants.FACEBOOK_EMAIL, userInfo.getEmail());
        editor.apply();

        this.userInfo = userInfo;
        TVChatUtil.populateProfileImage(new AQuery(this), this.userInfo);
	}

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = /*(trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    :*/ (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
}
