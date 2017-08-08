package com.haks.ghost;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

public class FriendsListActivity extends AppCompatActivity {
  private User mMe;

  private Activity mActivity;
  private ListView mFriendsListView;
  private FriendsAdapter mFriendsAdapter;
  private LayoutInflater mLayoutInflater;

  private RequestQueue mRequestQueue;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_friends_list);
    setTitle(Constants.FRIENDS_SCREEN_TITLE);

    mActivity = this;
    mLayoutInflater = LayoutInflater.from(this.getApplicationContext());

    // Setup Volley.
    mRequestQueue = Volley.newRequestQueue(this);

    // Get user information.
    mMe = new User(this);

    // Fetch the list of friends.
    mFriendsAdapter = new FriendsAdapter(mLayoutInflater);
    populateFriends();

    // Add the adapter to the list view.
    mFriendsListView = (ListView) findViewById(R.id.list);
    mFriendsListView.setAdapter(mFriendsAdapter);
    mFriendsListView.setOnItemClickListener(new OnFriendClick(this));

    // Set the header.
    View listHeaderView = mLayoutInflater.inflate(R.layout.friends_list_row, null);
    TextView headerText = (TextView) listHeaderView.findViewById(R.id.name);
    headerText.setText(Constants.ADD_FRIEND_DIALOG_TITLE);
    mFriendsListView.addHeaderView(listHeaderView);
  }

  private void populateFriends() {
    mRequestQueue.add(this.makeGetFriendsRequest());
  }

  private class OnFriendClick implements AdapterView.OnItemClickListener {
    private final Activity mCurrentActivity;

    public OnFriendClick(Activity currentActivity) {
      mCurrentActivity = currentActivity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      if (position == 0) {
        // Trying to add a new friend.
        final AlertDialog addFriendDialog = new AlertDialog.Builder(FriendsListActivity.this)
            .setTitle(Constants.ADD_FRIEND_DIALOG_TITLE)
            .setView(R.layout.add_friend_dialog)
            .create();
        addFriendDialog.show();  // This must be called before adding the onClickListeners.
        Button addFriendButton = (Button) addFriendDialog.findViewById(R.id.add);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            EditText friendNameEditText = (EditText) addFriendDialog.findViewById(R.id.friend_name);
            String friendName = friendNameEditText.getText().toString();
            mRequestQueue.add(makeAddFriendRequest(friendName));
            addFriendDialog.dismiss();
          }
        });
        Button cancelFriendButton = (Button) addFriendDialog.findViewById(R.id.cancel);
        cancelFriendButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            addFriendDialog.dismiss();
          }
        });
        return;
      }
      Intent intent = new Intent(mCurrentActivity, ChatActivity.class);
      intent.putExtra(Constants.FRIEND_INTENT_KEY, (Friend) mFriendsAdapter.getItem(position));
      startActivity(intent);
    }
  }

  private RequestWithHeaders makeGetFriendsRequest() {
    RequestWithHeaders getFriendsRequest = new RequestWithHeaders(
        Request.Method.GET,
        Constants.API_BASE + Constants.API_USERS + "/" + mMe.getUserId() + Constants.API_FRIENDS,
        null,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            Log.d("FRIENDS_LIST_ACTIVITY", "GOT FRIENDS SUCCESSFULLY");
            try {
              JSONArray friends = response.getJSONArray(Constants.API_FRIENDS_KEY);
              for (int i = 0; i < friends.length(); i++) {
                JSONObject friend = friends.getJSONObject(i);
                int friendUserId = friend.getInt(Constants.API_USER_ID_KEY);
                String friendUsername = friend.getString(Constants.API_USERNAME_KEY);
                int friendRegistrationId = friend.getInt(Constants.API_REGISTRATION_ID_KEY);
                int friendDeviceId = friend.getInt(Constants.API_DEVICE_ID_KEY);
                mFriendsAdapter.addFriend(
                    new Friend(friendUserId, friendUsername, friendRegistrationId, friendDeviceId));
              }
              mFriendsAdapter.notifyDataSetChanged();
            } catch (Exception e) {
            }
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d("FRIENDS_LIST_ACTIVITY", error.toString());
          }
        });
    getFriendsRequest.addAuthHeader(mMe.getToken() + ":");
    return getFriendsRequest;
  }

  private RequestWithHeaders makeAddFriendRequest(String friendUsername) {
    JSONObject body = new JSONObject();
    try {
      body.put(Constants.API_USERNAME_KEY, friendUsername);
    } catch (Exception e) {
    }
    RequestWithHeaders addFriendRequest = new RequestWithHeaders(
        Request.Method.POST,
        Constants.API_BASE + Constants.API_USERS + "/" + mMe.getUserId() + Constants.API_FRIENDS,
        body,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            Log.d("FRIENDS_LIST_ACTIVITY", "ADDED FRIEND SUCCESSFULLY");
            try {
              // Deserialize the response.
              JSONObject friend = response.getJSONObject(Constants.API_USER_KEY);
              int friendRegistrationId = friend.getInt(Constants.API_REGISTRATION_ID_KEY);
              int friendDeviceId = friend.getInt(Constants.API_DEVICE_ID_KEY);
              IdentityKey friendIdentityPublicKey = new IdentityKey(
                  Base64.decode(
                      friend.getString(Constants.API_IDENTITY_PUBLIC_KEY_KEY), Base64.DEFAULT),
                  0);
              String[] preKeyInfo =
                  friend.getString(Constants.API_ONE_TIME_PRE_KEY_KEY).split(":");
              int friendPreKeyId = Integer.parseInt(preKeyInfo[0]);
              ECPublicKey friendOneTimePublicPreKey =
                  Curve.decodePoint(Base64.decode(preKeyInfo[1], Base64.DEFAULT), 0);
              Log.d("FRIENDS_LIST_ACTIVITY", "PRE KEY BEFORE: " + preKeyInfo[1]);
              Log.d("FRIENDS_LIST_ACTIVITY", "PRE KEY AFTER: "
                  + Base64.encodeToString(friendOneTimePublicPreKey.serialize(), Base64.DEFAULT));
              String[] signedPreKeyInfo =
                  friend.getString(Constants.API_SIGNED_PRE_KEY_KEY).split(":");
              int friendSignedPreKeyId = Integer.parseInt(signedPreKeyInfo[0]);
              byte[] friendSignedPreKeySignature =
                  Base64.decode(signedPreKeyInfo[1], Base64.DEFAULT);
              ECPublicKey friendPublicSignedPreKey =
                  Curve.decodePoint(Base64.decode(signedPreKeyInfo[2], Base64.DEFAULT), 0);

              // Establish a session with the friend.
              SessionBuilder sessionBuilder = new SessionBuilder(
                  mMe.getSessionStore(),
                  mMe.getPreKeyStore(),
                  mMe.getSignedPreKeyStore(),
                  mMe.getIdentityKeyStore(),
                  new SignalProtocolAddress(friendRegistrationId + "", friendDeviceId));
              sessionBuilder.process(new PreKeyBundle(
                  friendRegistrationId,
                  friendDeviceId,
                  friendPreKeyId,
                  friendOneTimePublicPreKey,
                  friendSignedPreKeyId,
                  friendPublicSignedPreKey,
                  friendSignedPreKeySignature,
                  friendIdentityPublicKey
              ));
              SessionCipher sessionCipher = new SessionCipher(
                  mMe.getSessionStore(),
                  mMe.getPreKeyStore(),
                  mMe.getSignedPreKeyStore(),
                  mMe.getIdentityKeyStore(),
                  new SignalProtocolAddress(friendRegistrationId + "", friendDeviceId));
              mMe.save(mActivity);

              // Populate the friend list.
              populateFriends();

              // Debug.
              CiphertextMessage ct = sessionCipher.encrypt("test".getBytes("UTF-8"));
              Log.d("FRIENDS_LIST_ACTIVITY", Base64.encodeToString(ct.serialize(), Base64.DEFAULT));
              ct = sessionCipher.encrypt("test".getBytes("UTF-8"));
              Log.d("FRIENDS_LIST_ACTIVITY", Base64.encodeToString(ct.serialize(), Base64.DEFAULT));
              PreKeySignalMessage signalMessage = new PreKeySignalMessage(ct.serialize());
            } catch (Exception e) {
              Log.d("FRIENDS_LIST_ACTIVITY", "ERROR: " + e);
            }
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d("FRIENDS_LIST_ACTIVITY", error.toString());
          }
        });
    addFriendRequest.addAuthHeader(mMe.getToken() + ":");
    return addFriendRequest;
  }
}