package com.haks.ghost;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class FriendsListActivity extends AppCompatActivity {
  private final static String[] FRIENDS = {
      "1:ayush:Ayush",
      "2:hk:Hanumanth",
      "3:surudh:Surudh",
      "4:kai:Kaivalya"};

  private ListView mFriendsListView;
  private FriendsAdapter mFriendsAdapter;
  private LayoutInflater mLayoutInflater;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_friends_list);
    setTitle("Friends");
    mLayoutInflater = LayoutInflater.from(this.getApplicationContext());

    // Fetch the list of friends.
    mFriendsAdapter = new FriendsAdapter(mLayoutInflater);
    populateFriends();

    // Add the adapter to the list view.
    mFriendsListView = (ListView)findViewById(R.id.list);
    mFriendsListView.setAdapter(mFriendsAdapter);
    mFriendsListView.setOnItemClickListener(new OnFriendClick(this));

    // Set the header.
    View listHeaderView = mLayoutInflater.inflate(R.layout.friends_list_row, null);
    TextView headerText = (TextView)listHeaderView.findViewById(R.id.name);
    headerText.setText("Add Friends");
    mFriendsListView.addHeaderView(listHeaderView);
  }

  private void populateFriends() {
    new FetchFriendsTask(mFriendsAdapter).execute((Void)null);
  }

  public class OnFriendClick implements AdapterView.OnItemClickListener {
    private final Activity mCurrentActivity;

    public OnFriendClick(Activity currentActivity) {
      mCurrentActivity = currentActivity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      if (position == 0) {
        // Trying to add a new friend.
        return;
      }
      Intent intent = new Intent(mCurrentActivity, ChatActivity.class);
      intent.putExtra("FRIEND", (User)mFriendsAdapter.getItem(position));
      startActivity(intent);
    }
  }

  public class FetchFriendsTask extends AsyncTask<Void, Void, Void> {
    private final FriendsAdapter mFriendsAdapter;

    public FetchFriendsTask(FriendsAdapter friendsAdapter) {
      mFriendsAdapter = friendsAdapter;
    }

    @Override
    protected Void doInBackground(Void... params) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        return null;
      }
      for (String friend : FRIENDS) {
        mFriendsAdapter.addFriend(new User(
            Integer.parseInt(friend.split(":")[0]),
            friend.split(":")[1],
            friend.split(":")[2]));
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);
      mFriendsAdapter.notifyDataSetChanged();
    }
  }
}
