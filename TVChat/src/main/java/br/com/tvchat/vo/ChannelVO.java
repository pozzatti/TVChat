package br.com.tvchat.vo;

import org.json.JSONObject;

import java.io.Serializable;

public class ChannelVO implements Serializable{
    private String id;
    private String name;
    private int order;

    public ChannelVO(){}

    public ChannelVO(String id, String name, int order){
        this.id = id;
        this.name = name;
        this.order = order;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getOrder(){
        return order;
    }

    public void setOrder(int order){
        this.order = order;
    }

    public static ChannelVO toObject(JSONObject json){
        ChannelVO channel = new ChannelVO();

        channel.setId(json.optString("id", ""));
        channel.setName(json.optString("name", ""));
        channel.setOrder(json.optInt("order", 0));

        return channel;
    }
}
