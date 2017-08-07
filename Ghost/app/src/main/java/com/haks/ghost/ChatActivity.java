package com.haks.ghost;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatActivity extends AppCompatActivity {
  private LinearLayout mLayout;
  private LayoutInflater mLayoutInflater;
  private int mPreviousMessageId;

  private Friend mFriend;
  private Friend mMe;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);

    Intent intent = getIntent();
    mFriend = (Friend)intent.getSerializableExtra("FRIEND");
    mMe = new Friend("ayush", "Ayush");
    mLayout = (LinearLayout)findViewById(R.id.layout);
    mLayoutInflater = LayoutInflater.from(this.getApplicationContext());
    mPreviousMessageId = 1;

    setTitle(mFriend.getName());

    addMyMessage("I am ayush!");
    addMyMessage("SECOND TEST!");
    addMyFriendMessage("I am your friend! This is so coool");
    addMyFriendMessage("I am your friend! This is so coool");
    addMyFriendMessage("I am your friend! This is so coool");
    addMyFriendMessage("I am your friend! This is so coool");
    addMyFriendMessage("I am your friend! This is so coool");
    addMyFriendMessage("I am your friend! This is so coool");
    addMyFriendMessage("I am your friend! This is so coool");
    addMyFriendMessage("I am your friend! This is so coool");
  }

  private void addMyMessage(String message) {
    this.addMessage(
        message,
        "Me",
        (LinearLayout)mLayoutInflater.inflate(R.layout.my_message_layout, null));
  }

  private void addMyFriendMessage(String message) {
    this.addMessage(
        message,
        mFriend.getName(),
        (LinearLayout)mLayoutInflater.inflate(R.layout.friend_message_layout, null));
  }

  private void addMessage(String message, String author, LinearLayout messageLayout) {
    /*RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.BELOW, mPreviousMessageId++);
    messageLayout.setId(mPreviousMessageId);
    messageLayout.setLayoutParams(layoutParams);*/

    TextView messageTextView = (TextView)messageLayout.findViewById(R.id.message);
    messageTextView.setText(message);

    TextView authorTextView = (TextView)messageLayout.findViewById(R.id.author);
    authorTextView.setText(author);

    mLayout.addView(messageLayout);
  }
}
