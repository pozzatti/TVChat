package br.com.tvchat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import br.com.tvchat.adapter.ThreadAdapter;
import br.com.tvchat.contants.TVChatConstants;
import br.com.tvchat.db.TVChatOpenHelper;
import br.com.tvchat.util.TVChatUtil;
import br.com.tvchat.vo.CommentVO;
import br.com.tvchat.vo.ThreadVO;
import br.com.tvchat.vo.UserInfoVO;

public class ThreadSelectionActivity extends Activity implements Observer{
    private String channel;
    private ThreadAdapter adapter;
    private ListView listView;

    private TVChatApplication application;

    private TVChatOpenHelper db;

    private AjaxCallback<JSONObject> threadsCb = new AjaxCallback<JSONObject>() {
        @Override
        public void callback(String url, JSONObject json, AjaxStatus status) {
            if(json != null){
                try {
                    JSONArray array = json.getJSONArray("threads");
                    for (int i = 0; i < array.length(); i++) {
                        ThreadVO thread = ThreadVO.toObject(array.getJSONObject(i));
                        List<String> notUserComments = thread.getNotUserComments();
                        notUserComments.removeAll(db.getCommentIds(thread.getId()));
                        thread.setCommentNumber(notUserComments.size());
                        adapter.addThread(thread);
                    }

                    ProgressBar prgLoading = (ProgressBar)findViewById(R.id.prgLoading);
                    prgLoading.setVisibility(View.GONE);
                } catch (JSONException jsone){
                    jsone.printStackTrace();
                }

                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        Object o = listView.getItemAtPosition(position);
                        ThreadVO thread=(ThreadVO)o;

                        Intent intent = new Intent(ThreadSelectionActivity.this, CommentsActivity.class);
                        intent.putExtra("threadId", thread.getId());
                        intent.putExtra("userId", thread.getUserInfo().getUserId());
                        intent.putExtra("message", thread.getMessage());
                        ThreadSelectionActivity.this.startActivity(intent);
                    }
                });
            }
        }
    };

    @Override
    public void update(Observable observable, Object o) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_selection);

        application = (TVChatApplication)getApplication();

        db = new TVChatOpenHelper(this);

        if (getIntent().getExtras()!=null){
            channel = (String)getIntent().getExtras().get("channel");
        }

        registerReceiver(mHandleMessageReceiver, new IntentFilter(TVChatConstants.THREAD_INTENT));

        setupActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter!=null){
            adapter.emptyCollection();
            adapter.notifyDataSetChanged();
        }

        ProgressBar prgLoading = (ProgressBar)findViewById(R.id.prgLoading);
        prgLoading.setVisibility(View.VISIBLE);

        listView = (ListView) findViewById(R.id.list_thread);
        adapter = new ThreadAdapter(((TVChatApplication)getApplication()));
        AQuery aq = new AQuery(this);
        aq.ajax(String.format("http://tv-chat.appspot.com/getThreads.do?registrationId=%s&channel=%s", TVChatUtil.getGCMRegistrationId(this), channel), JSONObject.class, threadsCb);

        application.setActiveActivity(getClass(), null);

        Tracker t = application.getTracker(TVChatApplication.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory("Canal")
                .setAction("Canal escolhido")
                .setLabel(channel)
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (savedInstanceState!=null) {
            savedInstanceState.putString("channel", channel);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        channel = savedInstanceState.getString("channel");
    }
    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        if (getActionBar()!=null) {
            getActionBar().setDisplayShowHomeEnabled(false);

            getActionBar().setTitle(channel);
        }
    }

    public void addThread(View v){
        TVChatApplication app = (TVChatApplication)getApplication();
        UserInfoVO user = app.getUserInfo();

        final EditText t = (EditText)findViewById(R.id.textCreateConversation);
        if (t.getText().toString()!=null && !t.getText().toString().equals("")){
            ThreadVO thread = new ThreadVO();
            String m = t.getText().toString();
            thread.setMessage(m);
            thread.setChannel(channel);
            thread.setUserInfo(user);
            t.setText("");

            adapter.addThread(thread);
            adapter.notifyDataSetChanged();

            AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                    if (json==null || json.optLong("id")==0){
                        Toast.makeText(ThreadSelectionActivity.this, "Não foi possível enviar a mensagem", Toast.LENGTH_SHORT).show();
                    } else {
                        t.setText("");
                        hideKeyboard();
                    }
                }
            };

            AQuery aq = new AQuery(this);
            aq.post("http://tv-chat.appspot.com/createThread.do", thread.toJSONObject(), JSONObject.class, cb);

            Tracker tracker = application.getTracker(TVChatApplication.TrackerName.APP_TRACKER);
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Conversa")
                    .setAction("Criar uma conversa")
                    .setLabel(channel)
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
            ThreadVO thread = (ThreadVO)intent.getSerializableExtra("thread");

            if(thread!=null && !adapter.contains(thread)){
                adapter.addThread(thread);
                adapter.notifyDataSetChanged();
            } else {
                CommentVO comment = (CommentVO) intent.getSerializableExtra("comment");
                if (comment != null) {
                    adapter.incrementCommentCounter(comment);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };
}