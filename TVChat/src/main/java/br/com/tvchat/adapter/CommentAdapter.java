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
import br.com.tvchat.util.DateUtil;
import br.com.tvchat.util.TVChatUtil;
import br.com.tvchat.vo.CommentVO;

public class CommentAdapter extends BaseAdapter {
    private List<CommentVO> lastMessages;
	
	private List<CommentVO> listArray;
    private Set<CommentVO> setComments;
    
    private TVChatApplication application;
 
    public CommentAdapter(TVChatApplication application) {
    	this.application = application;
    	
        listArray = new ArrayList<CommentVO>();
        setComments = new HashSet<CommentVO>();
        lastMessages = new ArrayList<CommentVO>();
    }
    
    public boolean contains(CommentVO comment){
    	return setComments.contains(comment);
    }
    
    public void addComment(CommentVO comment){
    	if (comment.getId()==null){
    		lastMessages.add(comment);
    	} else {
    		if (comment.getUserInfo().getUserId().equals(application.getUserInfo().getUserId())){
	    		Iterator<CommentVO> it = lastMessages.iterator();
	        	while(it.hasNext()){
	        		CommentVO c = it.next();
	        		if (c.getMessage().equals(comment.getMessage())){
	        			it.remove();
	        		}
	        	}
    		}
    		    		
    		listArray.add(comment);
        	setComments.add(comment);
    	}    	
    }
    
    @Override
    public int getCount() {
        return listArray.size()+lastMessages.size();
    }
 
    @Override
    public CommentVO getItem(int i) {
    	if (i<listArray.size()){
    		return listArray.get(i);
    	} else {
    		return lastMessages.get(i-listArray.size());
    	}    		
    }
 
    @Override
    public long getItemId(int i) {
        return i;
    }
 
    @Override
    public View getView(final int index, View view, final ViewGroup parent) {
    	if (getItem(index).getUserInfo().getUserId().equals(application.getUserInfo().getUserId())){
    		return doMe(index, view, parent);
    	} else {
    		return doOthers(index, view, parent);
    	}
    }
    
    private View doOthers(final int index, View view, final ViewGroup parent){
    	if (view == null || view.findViewById(R.id.imageProfile)==null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());            
            view = inflater.inflate(R.layout.comments_list_view_others, parent, false);
        }

        ImageView imageProfile = (ImageView)view.findViewById(R.id.imageProfile);
        imageProfile.setImageBitmap(null);
        TVChatUtil.populateProfileImage(new AQuery(view), getItem(index).getUserInfo(), imageProfile);

        TextView txtName = (TextView)view.findViewById(R.id.textName);
        txtName.setText(getItem(index).getUserInfo().getFirstName());
        
        TextView txtComments = (TextView)view.findViewById(R.id.textComments);
        txtComments.setText(getItem(index).getMessage());
        
        if (getItem(index).getId()!=null){
	        TextView txtTime = (TextView)view.findViewById(R.id.textTime);
	        txtTime.setText(DateUtil.getTimeComment(getItem(index).getTime()));
        }
        
        return view;    	   	
    }
    
    private View doMe(final int index, View view, final ViewGroup parent){
    	if (view == null || view.findViewById(R.id.imageProfile)!=null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());            
            view = inflater.inflate(R.layout.comments_list_view_me, parent, false);
        }

        TextView txtComments = (TextView)view.findViewById(R.id.textComments);
        txtComments.setText(getItem(index).getMessage());

        TextView txtTime = (TextView)view.findViewById(R.id.textTime);
        if (getItem(index).getId()!=null){
	        txtTime.setText(DateUtil.getTimeComment(getItem(index).getTime()));
        } else {
            txtTime.setText("");
        }
        
        return view;
    }
}