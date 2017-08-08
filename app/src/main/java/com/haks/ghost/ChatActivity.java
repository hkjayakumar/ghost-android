package com.haks.ghost;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity {
  private Friend mFriend;
  private User mMe;

  private Activity mActivity;
  private LinearLayout mLayout;
  private LayoutInflater mLayoutInflater;
  private EditText mMessageText;
  private Button mSendMessageButton;

  private RequestQueue mRequestQueue;

  private TimerTask mPollingTask;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);

    Intent intent = getIntent();
    mFriend = (Friend)intent.getSerializableExtra(Constants.FRIEND_INTENT_KEY);
    setTitle(mFriend.getUsername());
    mMe = new User(this);

    mActivity = this;
    mLayout = (LinearLayout)findViewById(R.id.layout);
    mLayoutInflater = LayoutInflater.from(this.getApplicationContext());

    mMessageText = (EditText)findViewById(R.id.message);
    mSendMessageButton = (Button)findViewById(R.id.send_message);
    mSendMessageButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String message = mMessageText.getText().toString();
        if (TextUtils.isEmpty(message)) {
          return;
        }
        mRequestQueue.add(makeSendMessageRequest(new Message(message, new Date())));
        mMessageText.setText("");
      }
    });

    mRequestQueue = Volley.newRequestQueue(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    final Handler handler = new Handler();
    Timer timer = new Timer();
    mPollingTask = new TimerTask() {
      @Override
      public void run() {
        handler.post(new Runnable() {
          public void run() {
            Log.d("CHAT_ACTIVITY", "TIMEOUT HIT TO SEND RECEIVE MESSAGE REQUEST");
            try {
              mRequestQueue.add(makeGetMessagesRequest());
            } catch (Exception e) {
            }
          }
        });
      }
    };
    timer.schedule(mPollingTask, 0, 3000);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mPollingTask.cancel();
  }

  private void addMyMessage(Message message) {
    this.addMessage(
        message,
        "Me",
        (LinearLayout)mLayoutInflater.inflate(R.layout.my_message_layout, null));
  }

  private void addMyFriendMessage(Message message) {
    this.addMessage(
        message,
        mFriend.getUsername(),
        (LinearLayout)mLayoutInflater.inflate(R.layout.friend_message_layout, null));
  }

  private void addMessage(Message message, String author, LinearLayout messageLayout) {
    TextView messageTextView = (TextView)messageLayout.findViewById(R.id.message);
    messageTextView.setText(message.getMessage());
    TextView authorTextView = (TextView)messageLayout.findViewById(R.id.author);
    authorTextView.setText(author);
    mLayout.addView(messageLayout);
  }

  private RequestWithHeaders makeGetMessagesRequest() {
    RequestWithHeaders getMessagesRequest = new RequestWithHeaders(
        Request.Method.GET,
        Constants.API_BASE + Constants.API_MESSAGES + "/" + mMe.getUserId()
            + "/" + mFriend.getUserId(),
        null,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            Log.d("CHAT_ACTIVITY", "GOT MESSAGES SUCCESSFULLY");
            try {
              JSONArray messages = response.getJSONArray(Constants.API_MESSAGES_KEY);
              for (int i = 0; i < messages.length(); i++) {
                JSONObject message = messages.getJSONObject(i);
                String messageCT = message.getString(Constants.API_MESSAGE_CT_KEY);
                String timestamp = message.getString(Constants.API_TIMESTAMP_KEY);
                Message receivedMessage = new Message(messageCT, timestamp);
                receivedMessage.decrypt(mFriend, mMe, mActivity);
                addMyFriendMessage(receivedMessage);
              }
            } catch (Exception e) {
              Log.d("CHAT_ACTIVITY", "DESERIALIZING ERROR: " + e);
            }
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d("CHAT_ACTIVITY", error.toString());
          }
        });
    getMessagesRequest.addAuthHeader(mMe.getToken() + ":");
    return getMessagesRequest;
  }

  private RequestWithHeaders makeSendMessageRequest(final Message message) {
    JSONObject body = new JSONObject();
    try {
      body.put(Constants.API_SENDER_ID_KEY, mMe.getUserId());
      body.put(Constants.API_RECEIVER_ID_KEY, mFriend.getUserId());
      body.put(Constants.API_MESSAGE_KEY, message.getEncryptedMessage(mMe, mFriend, mActivity));
      body.put(Constants.API_TIMESTAMP_KEY, message.getTimestamp());
    } catch (Exception e) {
    }
    RequestWithHeaders sendMessageRequest = new RequestWithHeaders(
        Request.Method.POST,
        Constants.API_BASE + Constants.API_MESSAGES,
        body,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            Log.d("CHAT_ACTIVITY", "SENT MESSAGE SUCCESSFULLY");
            addMyMessage(message);
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d("CHAT_ACTIVITY", error.toString());
          }
        });
    sendMessageRequest.addAuthHeader(mMe.getToken() + ":");
    return sendMessageRequest;
  }
}
