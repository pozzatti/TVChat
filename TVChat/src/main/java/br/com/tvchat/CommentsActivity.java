package br.com.tvchat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import br.com.tvchat.adapter.CommentAdapter;
import br.com.tvchat.contants.TVChatConstants;
import br.com.tvchat.db.TVChatOpenHelper;
import br.com.tvchat.util.GraphicsUtil;
import br.com.tvchat.util.TVChatUtil;
import br.com.tvchat.vo.CommentVO;

public class CommentsActivity extends Activity implements Observer {
	private String threadId;
    private String userId;
	private CommentAdapter adapter;
	private ListView listView;
	private TVChatApplication application;

    private TVChatOpenHelper db;
	
	private AjaxCallback<JSONObject> commentsCb = new AjaxCallback<JSONObject>() {
        @Override
        public void callback(String url, JSONObject json, AjaxStatus status) {        
        	if(json != null){			
    			try {
    				JSONArray array = json.getJSONArray("comments");
                    List<String> commentIds = new ArrayList<String>();
    				for (int i = 0; i < array.length(); i++) {
                        CommentVO comment = CommentVO.toObject(array.getJSONObject(i));
                        if (!comment.getUserInfo().getUserId().equals(application.getUserInfo().getUserId())){
                            commentIds.add(comment.getId());
                        }
    					adapter.addComment(comment);
    				}
                    if (commentIds.size()>0) {
                        db.addReadedComments(threadId, commentIds);
                    }
    				
					ProgressBar prgLoading = (ProgressBar)findViewById(R.id.prgLoading);
					prgLoading.setVisibility(View.GONE);
    			} catch (Exception jsone){
    				jsone.printStackTrace();
    			}
    			
    			listView.setAdapter(adapter);
    		}
        }
    };

    @Override
    public void update(Observable observable, Object o) {

    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
		
		application = (TVChatApplication)getApplication();
		db = new TVChatOpenHelper(this);

		if (getIntent().getExtras()!=null){
			threadId = (String)getIntent().getExtras().get("threadId");
            userId = (String)getIntent().getExtras().get("userId");
        }

        registerReceiver(mHandleMessageReceiver, new IntentFilter(TVChatConstants.COMMENT_INTENT+"."+threadId));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());

		setupActionBar();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (adapter==null){
			ProgressBar prgLoading = (ProgressBar)findViewById(R.id.prgLoading);
			prgLoading.setVisibility(View.VISIBLE);
			
			listView = (ListView) findViewById(R.id.list_comments);
			adapter = new CommentAdapter(application);
			AQuery aq = new AQuery(this);
			aq.ajax(String.format("http://tv-chat.appspot.com/getThreadComments.do?registrationId=%s&threadId=%s&userId=%s", TVChatUtil.getGCMRegistrationId(this), threadId, userId), JSONObject.class, commentsCb);
		}

        application.setActiveActivity(getClass(), threadId);

        Tracker t = application.getTracker(TVChatApplication.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory("Conversa")
                .setAction("Conversa Escolhida")
                .setLabel((String)getIntent().getExtras().get("message"))
                .build());
	}

    @Override
    protected void onPause(){
        super.onPause();

        application.setActiveActivity(null, null);
    }

	@Override
	protected void onDestroy() {		
		super.onDestroy();

        unregisterReceiver(mHandleMessageReceiver);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		String avatar = (String)getIntent().getExtras().get("userId");
		String message = (String)getIntent().getExtras().get("message");

        if(getActionBar()!=null) {
            getActionBar().setTitle(message);
            final Resources res = this.getResources();

            AQuery aq = new AQuery(this);
            final AjaxCallback<Bitmap> cb = new AjaxCallback<Bitmap>() {
                @Override
                public void callback(String url, Bitmap bm, AjaxStatus status) {
                    BitmapDrawable bitmap = new BitmapDrawable(res, GraphicsUtil.getCircleBitmap(bm));
                    getActionBar().setIcon(bitmap);
                }
            };
            if (avatar!=null) {
                aq.ajax(String.format(TVChatConstants.FACEBOOK_IMAGE_URL, avatar), Bitmap.class, cb);
            }
        }
	}

    public void addComment(View v){
        final EditText t = (EditText)findViewById(R.id.textCreateComment);
        if (t.getText().toString()!=null && !t.getText().toString().equals("")){
            CommentVO comment = new CommentVO();
            String m = t.getText().toString();
            comment.setMessage(m);
            comment.setUserInfo(application.getUserInfo());
            comment.setThreadId(threadId);
            comment.setThreadUserId(userId);
            t.setText("");

            adapter.addComment(comment);
            adapter.notifyDataSetChanged();

            AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    if (json==null || json.optLong("id")==0){
                        Toast.makeText(CommentsActivity.this, "Não foi possível enviar a mensagem", Toast.LENGTH_SHORT).show();
                    } else {
                        t.setText("");
                        hideKeyboard();
                    }
                }
            };

            AQuery aq = new AQuery(this);
            aq.post("http://tv-chat.appspot.com/createComment.do", comment.toJSONObject(), JSONObject.class, cb);

            Tracker tracker = application.getTracker(TVChatApplication.TrackerName.APP_TRACKER);
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Comentario")
                    .setAction("Criar um comentario")
                    .setLabel((String)getIntent().getExtras().get("message"))
                    .build());
        }
    }
	
	private void hideKeyboard() {
	    InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

	    // check if no view has focus:
	    View view = this.getCurrentFocus();
	    if (view != null) {
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}

    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CommentVO comment = (CommentVO)intent.getSerializableExtra("comment");

            if (!adapter.contains(comment)){
                if (!comment.getUserInfo().getUserId().equals(application.getUserInfo().getUserId())){
                    List<String> commentId = new ArrayList<String>();
                    commentId.add(comment.getId());

                    db.addReadedComments(threadId, commentId);
                }
                adapter.addComment(comment);
                adapter.notifyDataSetChanged();
            }
        }
    };
}
