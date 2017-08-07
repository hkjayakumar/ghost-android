package com.haks.ghost;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatActivity extends AppCompatActivity {
  private Friend mFriend;
  private User mMe;

  private LinearLayout mLayout;
  private LayoutInflater mLayoutInflater;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);

    Intent intent = getIntent();
    mFriend = (Friend)intent.getSerializableExtra(Constants.FRIEND_INTENT_KEY);
    mLayout = (LinearLayout)findViewById(R.id.layout);
    mLayoutInflater = LayoutInflater.from(this.getApplicationContext());

    setTitle(mFriend.getUsername());

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
        mFriend.getUsername(),
        (LinearLayout)mLayoutInflater.inflate(R.layout.friend_message_layout, null));
  }

  private void addMessage(String message, String author, LinearLayout messageLayout) {
    TextView messageTextView = (TextView)messageLayout.findViewById(R.id.message);
    messageTextView.setText(message);
    TextView authorTextView = (TextView)messageLayout.findViewById(R.id.author);
    authorTextView.setText(author);
    mLayout.addView(messageLayout);
  }
}
