package br.com.tvchat.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Observer;

import br.com.tvchat.TVChatApplication;
import br.com.tvchat.contants.TVChatConstants;
import br.com.tvchat.util.TVChatUtil;

public class GCMRegistrationTask extends AsyncTask<Void, Void, Boolean>{
    private Observer observer;
    private Context context;
    private TVChatApplication application;

    public GCMRegistrationTask(Observer observer, Context context, TVChatApplication application){
        this.observer = observer;
        this.context = context;
        this.application = application;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return doInBackground(true);
    }

    private Boolean doInBackground(boolean retry){
        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            String registrationId = gcm.register(TVChatConstants.TVCHAT_ID);


            JSONObject json = new JSONObject();
            json.put("registrationId", registrationId);
            json.put("facebookId", application.getUserInfo().getUserId());
            AQuery aq = new AQuery(context);
            AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
            cb.url("http://tv-chat.appspot.com/receiveGCMRegistrationId.do").param(AQuery.POST_ENTITY, new StringEntity(json.toString())).type(JSONObject.class);
            aq.sync(cb);

            JSONObject res = cb.getResult();
            if (res!=null && res.getBoolean("result")){
                TVChatUtil.storeGCMRegistrationId(context, registrationId);


                Log.w("GCM", "OK");
                return true;
            } else {
                Log.w("GCM", "ERROR");
            }

            return false;
        } catch (IOException ioe){
            if (retry) {
                return doInBackground(false);
            } else {
                return false;
            }
        } catch (JSONException jsone){
            Log.e("JSONERROR", ""+jsone);
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            observer.update(null, "GCMRegistered");
        } else {
            observer.update(null, "GCMRegistrationError");
        }
    }
}
