package br.com.tvchat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import br.com.tvchat.contants.TVChatConstants;
import br.com.tvchat.vo.UserInfoVO;

public class TVChatUtil {
    public static String getGCMRegistrationId(Context context){
        SharedPreferences preferences = context.getSharedPreferences(TVChatConstants.PREFERENCES_BUNDLE, Context.MODE_PRIVATE);
        String gcmRegId = preferences.getString(TVChatConstants.GCM_REG_ID, "");
        int storedVersion = preferences.getInt(TVChatConstants.APP_VERSION, 0);
        int currentVersion = getAppVersion(context);

        if (!gcmRegId.isEmpty() && storedVersion!=currentVersion){
            gcmRegId = "";
        }

        return gcmRegId;
    }

    private static int getAppVersion(Context context){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException nnfe){
            throw new RuntimeException("Could not get package name: " + nnfe);
        }
    }

    public static void populateProfileImage(AQuery aq, final UserInfoVO userInfo, final ImageView iv){
        final AjaxCallback<Bitmap> cb = new AjaxCallback<Bitmap>() {
            @Override
            public void callback(String url, Bitmap bm, AjaxStatus status) {
                userInfo.setProfileImage(GraphicsUtil.getCircleBitmap(bm));
                if (iv!=null){
                    iv.setImageBitmap(userInfo.getProfileImage());
                }
            }
        };
        aq.ajax(String.format(TVChatConstants.FACEBOOK_IMAGE_URL, userInfo.getUserId()), Bitmap.class, cb);
    }

    public static void populateProfileImage(AQuery aq, final UserInfoVO userInfo){
        populateProfileImage(aq, userInfo, null);
    }

    public static void storeGCMRegistrationId(Context context, String registrationId){
        SharedPreferences preferences = context.getSharedPreferences(TVChatConstants.PREFERENCES_BUNDLE, Context.MODE_PRIVATE);
        int appVersion = getAppVersion(context);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TVChatConstants.GCM_REG_ID, registrationId);
        editor.putInt(TVChatConstants.APP_VERSION, appVersion);
        editor.apply();
    }
}
