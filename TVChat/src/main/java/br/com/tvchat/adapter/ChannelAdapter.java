package br.com.tvchat.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.androidquery.AQuery;

import java.util.List;

import br.com.tvchat.R;
import br.com.tvchat.ThreadSelectionActivity;
import br.com.tvchat.vo.ChannelVO;

public class ChannelAdapter extends BaseAdapter {
    private List<ChannelVO> channels;
 
    public ChannelAdapter(List<ChannelVO> channels) {
        this.channels = channels;
    }
 
    @Override
    public int getCount() {
        return channels.size();    // total number of elements in the list
    }
 
    @Override
    public Object getItem(int i) {
        return channels.get(i);    // single item in the list
    }
 
    @Override
    public long getItemId(int i) {
        return i;                   // index number
    }
 
    @Override
    public View getView(final int index, View view, final ViewGroup parent) {
 
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.channel_list_view, parent, false);
        }

        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(parent.getContext(), ThreadSelectionActivity.class);
                intent.putExtra("channel", channels.get(index).getName().toUpperCase());
                parent.getContext().startActivity(intent);
            }
        };

        AQuery aq = new AQuery(view);
        Log.i("CHANNEL", "http://tv-chat.appspot.com/img/" + channels.get(index).getName() + ".png");
        aq.id(R.id.btn_channel).image("http://tv-chat.appspot.com/img/" + channels.get(index).getName().toLowerCase() + ".png", false, true).clickable(true).clicked(click);
        Log.i("CHANNEL", "http://tv-chat.appspot.com/img/" + channels.get(index).getName() + ".png");

        return view;
    }
}