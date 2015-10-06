package br.com.tvchat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import br.com.tvchat.R;
import br.com.tvchat.TVChatApplication;
import br.com.tvchat.util.TVChatUtil;
import br.com.tvchat.vo.CommentVO;
import br.com.tvchat.vo.ThreadVO;
 
public class ThreadAdapter extends BaseAdapter {
	private List<ThreadVO> lastThreads;
	
	private List<ThreadVO> listArray;
    private Set<ThreadVO> setThreads; 
    
    private TVChatApplication application;
    
    
    public ThreadAdapter(TVChatApplication application) {
    	this.application = application;
    	
        listArray = new ArrayList<ThreadVO>();
        setThreads = new HashSet<ThreadVO>();
        lastThreads = new ArrayList<ThreadVO>();
    }
    
    public boolean contains(ThreadVO thread){
    	return setThreads.contains(thread);
    }
    
    public void addThread(ThreadVO thread){
    	if (thread.getId()==null){
    		lastThreads.add(thread);
    	} else {
    		if (thread.getUserInfo().getUserId().equals(application.getUserInfo().getUserId())){
	    		Iterator<ThreadVO> it = lastThreads.iterator();
	        	while(it.hasNext()){
	        		ThreadVO t = it.next();
	        		if (t.getMessage().equals(thread.getMessage())){
	        			it.remove();
	        		}
	        	}	        	
    		}
        	listArray.add(thread);
        	setThreads.add(thread);
    	}    	
    }

    public void emptyCollection(){
        listArray = new ArrayList<ThreadVO>();
        setThreads = new HashSet<ThreadVO>();
        lastThreads = new ArrayList<ThreadVO>();
    }

    public void incrementCommentCounter(CommentVO comment){
        for (ThreadVO thread: listArray){
            if (thread.getId().equals(comment.getThreadId())){
                thread.setCommentNumber(thread.getCommentNumber()+1);
            }
        }
    }
 
    @Override
    public int getCount() {
    	return listArray.size()+lastThreads.size();
    }
 
    @Override
    public ThreadVO getItem(int i) {
    	if (i<listArray.size()){
    		return listArray.get(i);
    	} else {
    		return lastThreads.get(i-listArray.size());
    	}
    }
 
    @Override
    public long getItemId(int i) {
        return i;
    }
 
    @Override
    public View getView(final int index, View view, final ViewGroup parent) {   	
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.thread_list_view, parent, false);
        }

        ImageView imageProfile = (ImageView)view.findViewById(R.id.imageProfile);
        imageProfile.setImageBitmap(null);
        TVChatUtil.populateProfileImage(new AQuery(view), getItem(index).getUserInfo(), imageProfile);

        TextView txtView1 = (TextView)view.findViewById(R.id.textView1);
        txtView1.setText(getItem(index).getMessage());

        TextView txtView2 = (TextView)view.findViewById(R.id.textView2);
        txtView2.setText(getItem(index).getUserInfo().getFirstName());

        TextView txtComments = (TextView)view.findViewById(R.id.textComments);
        int commentCounter = getItem(index).getCommentNumber();
        String sCommentCounter = ""+commentCounter;
        if (commentCounter>10){
            sCommentCounter = "10+";
        }
        txtComments.setText(sCommentCounter);
        if (getItem(index).getCommentNumber()==0){
        	txtComments.setVisibility(View.INVISIBLE);
        } else {
        	txtComments.setVisibility(View.VISIBLE);
        }
        
        return view;
    }
}