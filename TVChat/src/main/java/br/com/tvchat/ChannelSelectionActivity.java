package br.com.tvchat;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import br.com.tvchat.adapter.ChannelAdapter;
import br.com.tvchat.vo.ChannelVO;

public class ChannelSelectionActivity extends Activity implements Observer {
    private TVChatApplication application;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (TVChatApplication)getApplication();

		createCustomActionBarTitle();
		
		setContentView(R.layout.activity_channel_selection);

        final ListView listView = (ListView) findViewById(R.id.list_data);
        AQuery aq = new AQuery(this);
        AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(){
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                List<ChannelVO> channels = new ArrayList<ChannelVO>();
                try {
                    JSONArray array = json.getJSONArray("channels");
                    for (int i = 0; i < array.length(); i++) {
                        channels.add(ChannelVO.toObject(array.getJSONObject(i)));
                    }
                } catch (JSONException jsone){
                    jsone.printStackTrace();
                }

                ChannelAdapter channelAdapter = new ChannelAdapter(channels);
                listView.setAdapter(channelAdapter);
            }
        };
        aq.progress(R.id.prgLoading).ajax("http://tv-chat.appspot.com/getChannels.do", JSONObject.class, cb);
	}
	
	private void createCustomActionBarTitle(){
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.custom_action_bar, null);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER );
        
        getActionBar().setCustomView(view, params);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);		
    }

    @Override
    public void update(Observable observable, Object o) {

    }

    @Override
    protected void onResume(){
        super.onResume();
        application.setActiveActivity(getClass(), null);
    }

    @Override
    protected void onPause(){
        super.onPause();
        application.setActiveActivity(null, null);
    }
}
