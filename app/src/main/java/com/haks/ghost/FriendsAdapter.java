package com.haks.ghost;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class FriendsAdapter extends BaseAdapter {
  private List<Friend> mFriendList;
  private LayoutInflater mLayoutInflater;

  FriendsAdapter(LayoutInflater layoutInflater) {
    mFriendList = new ArrayList<>();
    mLayoutInflater = layoutInflater;
  }

  public void addFriend(Friend friend) {
    mFriendList.add(friend);
  }

  @Override
  public int getCount() {
    return mFriendList.size();
  }

  @Override
  public long getItemId(int position) {
    return position - 1;
  }

  @Override
  public Object getItem(int position) {
    return mFriendList.get(position - 1);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    Friend friend = mFriendList.get(position);
    View currentView = convertView == null
        ? mLayoutInflater.inflate(R.layout.friends_list_row, null)
        : convertView;
    TextView nameTextView = (TextView)currentView.findViewById(R.id.name);
    nameTextView.setText(friend.getUsername());
    return currentView;
  }
}
