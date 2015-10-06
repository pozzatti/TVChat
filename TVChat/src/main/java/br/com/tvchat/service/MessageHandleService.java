package br.com.tvchat.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.tvchat.CommentsActivity;
import br.com.tvchat.R;
import br.com.tvchat.TVChatApplication;
import br.com.tvchat.contants.TVChatConstants;
import br.com.tvchat.receiver.GcmBroadcastReceiver;
import br.com.tvchat.util.GraphicsUtil;
import br.com.tvchat.vo.CommentVO;
import br.com.tvchat.vo.ThreadVO;

public class MessageHandleService extends IntentService{
    private TVChatApplication application;

    public MessageHandleService(){
        super("MessageHandleService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        application = (TVChatApplication)getApplication();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (application.getUserInfo()!=null) {
            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String messageType = gcm.getMessageType(intent);

            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String type = extras.getString("type");

                if ("Thread".equals(type)) {
                    sendThreadBroadcast(extras);
                } else if ("Comment".equals(type)) {
                    sendCommentBroadcast(extras);
                }
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendThreadBroadcast(Bundle extras){
        try {
            ThreadVO thread = ThreadVO.toObject(new JSONObject(extras.getString("data")));

            Intent i = new Intent(TVChatConstants.THREAD_INTENT);
            i.putExtra("thread", thread);
            this.sendBroadcast(i);
        } catch (JSONException jsone){
            Log.e("JSON", jsone.getMessage(), jsone);
        }
    }

    private void sendNotification(final CommentVO comment, final String threadMessage, String threadFacebookId){
        if (!application.isActivityActive(CommentsActivity.class, comment.getThreadId())) {

            final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Intent i = new Intent(this, CommentsActivity.class);
            i.putExtra("threadId", comment.getThreadId());
            i.putExtra("message", threadMessage);
            i.putExtra("userId", threadFacebookId);
            PendingIntent p = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);


            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentTitle(threadMessage);
            builder.setContentText(comment.getUserInfo().getFirstName());
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
            builder.setContentIntent(p);

            final AjaxCallback<Bitmap> cb = new AjaxCallback<Bitmap>() {
                @Override
                public void callback(String url, Bitmap bm, AjaxStatus status) {
                    builder.setLargeIcon(GraphicsUtil.getCircleBitmap(bm));
                    NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                    style.addLine(comment.getMessage());
                    style.addLine(comment.getUserInfo().getFirstName());
                    builder.setStyle(style);

                    NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
                    secondPageStyle.setBigContentTitle(threadMessage).bigText(comment.getMessage());
                    Notification secondPageNotification = new NotificationCompat.Builder(MessageHandleService.this).setStyle(secondPageStyle).build();

                    Notification n = builder.extend(new NotificationCompat.WearableExtender().addPage(secondPageNotification)).build();

                    n.vibrate = new long[]{150, 300, 150, 600};
                    n.flags = Notification.FLAG_AUTO_CANCEL;
                    nm.notify(R.drawable.ic_launcher, n);

                    try {
                        Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone toque = RingtoneManager.getRingtone(MessageHandleService.this, som);
                        toque.play();
                    } catch (Exception ignored) {
                    }
                }
            };
            final AQuery aq = new AQuery(this);
            aq.ajax(String.format(TVChatConstants.FACEBOOK_IMAGE_URL, comment.getUserInfo().getUserId()), Bitmap.class, 0, cb);
        }
    }

    private void sendCommentBroadcast(Bundle extras){
        try {
            JSONObject jComment = new JSONObject(extras.getString("data"));
            CommentVO comment = CommentVO.toObject(jComment);

            Intent i = new Intent(TVChatConstants.COMMENT_INTENT+"."+comment.getThreadId());
            i.putExtra("comment", comment);
            this.sendBroadcast(i);

            if (!comment.getUserInfo().getUserId().equals(application.getUserInfo().getUserId())){
                Intent i2 = new Intent(TVChatConstants.THREAD_INTENT);
                i2.putExtra("comment", comment);
                this.sendBroadcast(i2);


                JSONArray facebookKeys = jComment.getJSONArray("facebookKeys");
                for (int j = 0; j < facebookKeys.length(); j++){
                    String facebookKey = facebookKeys.getString(j);
                    if (facebookKey.equals(application.getUserInfo().getUserId())){
                        sendNotification(comment, jComment.getString("threadMessage"), jComment.getString("threadFacebookId"));
                        break;
                    }
                }
            }


        } catch (JSONException jsone){
            Log.e("JSON", jsone.getMessage(), jsone);
        }
    }
}