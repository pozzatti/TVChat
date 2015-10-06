package br.com.tvchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import br.com.tvchat.async.GCMRegistrationTask;
import br.com.tvchat.util.TVChatUtil;
import br.com.tvchat.vo.UserInfoVO;

public class LoginActivity extends Activity implements Observer{
	private ProgressBar progress;
    private TVChatApplication application;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

        application = (TVChatApplication)getApplication();
        UserInfoVO userInfo = application.getUserInfo();
        if (userInfo!=null && !TVChatUtil.getGCMRegistrationId(this).isEmpty()){
            Intent intent = new Intent(this, ChannelSelectionActivity.class);
            startActivity(intent);
            finish();
        }
		
		progress = (ProgressBar)findViewById(R.id.progressBar);
		progress.setVisibility(View.INVISIBLE);
	}

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void ensureSessionOpen(final boolean retry){
        List<String> permissions = new ArrayList<String>();
        permissions.add("email");

		Session.openActiveSession(this, true, permissions, new Session.StatusCallback() {
            boolean control = true;

			@Override
			public void call(Session session, SessionState state, Exception exception) {
                if (control) {
                    if (session.isOpened()) {
                        Request.newMeRequest(session, new Request.GraphUserCallback() {
                            @Override
                            public void onCompleted(final GraphUser user, Response response) {
                                if (user != null) {
                                    application.setUserInfo(new UserInfoVO(user.getId(), null, ""+user.getProperty("email"), user.getFirstName(), user.getLastName(), user.getName()));

                                    loginCompleted();
                                    control = false;
                                } else {
                                    if (retry) {
                                        ensureSessionOpen(false);
                                    }
                                }
                            }
                        }).executeAsync();
                    }
                }
			}
		});
	}

	public void loginFacebook(View v){
		if (progress.getVisibility()==View.INVISIBLE){
			progress.setVisibility(View.VISIBLE);

			ensureSessionOpen(true);
		}
	}

	private void loginCompleted(){
        String gcmRegId = TVChatUtil.getGCMRegistrationId(this);
        Log.w("GCMID", gcmRegId);
        if (gcmRegId.isEmpty()){
            progress.setVisibility(View.VISIBLE);
            GCMRegistrationTask task = new GCMRegistrationTask(this, this, (TVChatApplication)getApplication());
            task.execute();
        } else {
            Tracker t = application.getTracker(TVChatApplication.TrackerName.APP_TRACKER);
            t.set("&uid", application.getUserInfo().getUserId());
            t.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Usuario Logado").build());

            progress.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(this, ChannelSelectionActivity.class);
            startActivity(intent);
            finish();
        }
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}


    @Override
    public void update(Observable observable, Object o) {
        if (o.equals("GCMRegistered")){
            progress.setVisibility(View.INVISIBLE);

            Intent intent = new Intent(this, ChannelSelectionActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Erro ao registrar no GCM", Toast.LENGTH_SHORT).show();
        }
    }
}
