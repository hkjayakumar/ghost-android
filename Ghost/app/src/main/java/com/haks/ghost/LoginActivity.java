package com.haks.ghost;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SessionBuilder;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.state.IdentityKeyStore;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.PreKeyStore;
import org.whispersystems.libsignal.state.SessionStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyStore;
import org.whispersystems.libsignal.state.impl.InMemoryIdentityKeyStore;
import org.whispersystems.libsignal.state.impl.InMemoryPreKeyStore;
import org.whispersystems.libsignal.state.impl.InMemorySessionStore;
import org.whispersystems.libsignal.state.impl.InMemorySignedPreKeyStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
  private EditText mUsernameView;
  private EditText mPasswordView;

  private RequestQueue mRequestQueue;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    mUsernameView = (EditText)findViewById(R.id.username);
    mPasswordView = (EditText)findViewById(R.id.password);

    mRequestQueue = Volley.newRequestQueue(this);

    User me = new User(this);
    if (me.getUserId() > 0) {
      Log.d("LOGIN_ACTIVITY", "USER IS STAYED LOGGED IN");
      goToFriendsScreen();
    }

    Button loginButton = (Button) findViewById(R.id.login_button);
    loginButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        attemptLogin();
      }
    });
  }

  private void attemptLogin() {
    // Reset errors.
    mUsernameView.setError(null);
    mPasswordView.setError(null);

    // Store values at the time of the login attempt.
    String username = mUsernameView.getText().toString();
    String password = mPasswordView.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password, if the user entered one.
    if (TextUtils.isEmpty(password)) {
      mPasswordView.setError(getString(R.string.error_field_required));
      focusView = mPasswordView;
      cancel = true;
    }

    // Check for a valid username.
    if (TextUtils.isEmpty(username)) {
      mUsernameView.setError(getString(R.string.error_field_required));
      focusView = mUsernameView;
      cancel = true;
    }

    if (cancel) {
      focusView.requestFocus();
      return;
    }

    mRequestQueue.add(this.makeLoginRequest());
  }

  private RequestWithHeaders makeLoginRequest() {
    final Activity activity = this;
    RequestWithHeaders loginRequest = new RequestWithHeaders(
        Request.Method.GET,
        Constants.API_BASE + Constants.API_TOKEN,
        null,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            try {
              // Save the user id of the currently logged in user to shared preferences.
              SharedPreferences.Editor editor =
                  getSharedPreferences(Constants.GHOST_SP_FILE_NAME, 0).edit();
              editor.putInt(User.USER_ID_SP_KEY, response.getInt(Constants.API_USER_ID_KEY));
              editor.commit();

              // If the user is logging back in (i.e. not for the first time), do not generate
              // their keys again.
              SharedPreferences sharedPreferences = getSharedPreferences(
                  User.USER_SP_FILE_NAME + response.getInt(Constants.API_USER_ID_KEY), 0);
              if (sharedPreferences.contains(User.TOKEN_SP_KEY)) {
                goToFriendsScreen();
                Log.d("LOGIN_ACTIVITY", "USER HAS LOGGED IN BEFORE");
                return;
              }

              // User is logging in for the first time.
              User me = new User(
                  response.getInt(Constants.API_USER_ID_KEY),
                  mUsernameView.getText().toString(),
                  response.getString(Constants.API_TOKEN_KEY));
              Log.d("LOGIN_ACTIVITY", "USER LOGGING IN FOR THE FIRST TIME");

              // Generate key pairs.
              int registrationId = KeyHelper.generateRegistrationId(false);
              me.setRegistrationId(registrationId);

              me.setDeviceId(1);

              IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
              me.setIdentityKeyStore(new GhostIdentityKeyStore(identityKeyPair, registrationId));

              List<PreKeyRecord> preKeyRecords = KeyHelper.generatePreKeys(1, 100);
              me.setPreKeyStore(new GhostPreKeyStore(preKeyRecords));

              SignedPreKeyRecord signedPreKeyRecord = null;
              try {
                signedPreKeyRecord = KeyHelper.generateSignedPreKey(identityKeyPair, 5);
              } catch (InvalidKeyException e) {
              }
              me.setSignedPreKeyStore(
                  new GhostSignedPreKeyStore(signedPreKeyRecord.getId(), signedPreKeyRecord));

              // Save the generated keys.
              me.save(activity);

              // Send the public keys to the server.
              mRequestQueue.add(makeSendPublicKeysRequest(me));
            } catch (Exception e) {
            }
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            mUsernameView.setError(getString(R.string.error_invalid_login_credentials));
            mPasswordView.setError(getString(R.string.error_invalid_login_credentials));
            mUsernameView.requestFocus();
            Log.d("LOGIN_ACTIVITY", error.toString());
          }
        });
    loginRequest.addAuthHeader(
        mUsernameView.getText().toString() + ":" + mPasswordView.getText().toString());
    return loginRequest;
  }

  private RequestWithHeaders makeSendPublicKeysRequest(User user) {
    JSONArray preKeys = new JSONArray();
    for (PreKeyRecord preKeyRecord : user.getPreKeyStore().loadAll()) {
      preKeys.put(Base64.encodeToString(
          preKeyRecord.getKeyPair().getPublicKey().serialize(),
          Base64.DEFAULT));
    }
    JSONObject body = new JSONObject();
    try {
      body.put(Constants.API_REGISTRATION_ID_KEY, user.getRegistrationId());
      body.put(Constants.API_DEVICE_ID_KEY, user.getDeviceId());
      body.put(Constants.API_IDENTITY_PUBLIC_KEY_KEY,
          Base64.encodeToString(user
              .getIdentityKeyStore()
              .getIdentityKeyPair()
              .getPublicKey()
              .serialize(), Base64.DEFAULT));
      body.put(Constants.API_ONE_TIME_PRE_KEYS_KEY, preKeys);
      body.put(Constants.API_SIGNED_PRE_KEY_KEY,
          Base64.encodeToString(user
              .getSignedPreKeyStore()
              .loadFirstPreKey()
              .getKeyPair()
              .getPublicKey()
              .serialize(), Base64.DEFAULT));
    } catch (Exception e) {
    }
    RequestWithHeaders sendPublicKeysRequest = new RequestWithHeaders(
        Request.Method.PUT,
        Constants.API_BASE + Constants.API_USERS + "/" + user.getUserId() + Constants.API_KEYS,
        body,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            goToFriendsScreen();
          }
        },
        new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.d("LOGIN_ACTIVITY", error.toString());
          }
        });
    sendPublicKeysRequest.addAuthHeader(user.getToken() + ":");
    return sendPublicKeysRequest;
  }

  private void goToFriendsScreen() {
    Intent intent = new Intent(this, FriendsListActivity.class);
    startActivity(intent);
  }

  IdentityKeyPair mIdentityKey = null;
  int mRegistrationId = 0;
  PreKeyRecord mPreKey = null;
  SignedPreKeyRecord mSignedPreKey = null;
  byte[] mMessage = null;

  public void randomMethod() {
    // Create keys on installation.
    IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
    int registrationId = KeyHelper.generateRegistrationId(false);
    List<PreKeyRecord> preKeys = KeyHelper.generatePreKeys(1, 100);
    SignedPreKeyRecord signedPreKey = null;
    try {
      signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, 5);
    } catch (InvalidKeyException e) {
    }

    // Load keys and session information from the device.
    Map<Long, SessionBuilder> userIdToSession = new HashMap<>();
    int deviceId = 1;

    // Establish session with the user when they are added as a friend.
    SessionStore sessionStore = new InMemorySessionStore();
    PreKeyStore preKeyStore = new InMemoryPreKeyStore();
    for (PreKeyRecord preKeyRecord : preKeys) {
      preKeyStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
    }
    SignedPreKeyStore signedPreKeyStore = new InMemorySignedPreKeyStore();
    signedPreKeyStore.storeSignedPreKey(signedPreKey.getId(), signedPreKey);
    IdentityKeyStore identityKeyStore = new InMemoryIdentityKeyStore(identityKeyPair, registrationId);

    SessionBuilder sessionBuilder = new SessionBuilder(sessionStore, preKeyStore, signedPreKeyStore,
        identityKeyStore, new SignalProtocolAddress(mRegistrationId + "", 2));
    try {
      sessionBuilder.process(new PreKeyBundle(
          mRegistrationId,
          2,
          mPreKey.getId(),
          mPreKey.getKeyPair().getPublicKey(),
          mSignedPreKey.getId(),
          mSignedPreKey.getKeyPair().getPublicKey(),
          mSignedPreKey.getSignature(),
          mIdentityKey.getPublicKey()));
    } catch (Exception e) {
    }
    userIdToSession.put(1L, sessionBuilder);

    // Send a message to the user.
    SessionCipher sessionCipher = new SessionCipher(
        sessionStore,
        preKeyStore,
        signedPreKeyStore,
        identityKeyStore,
        new SignalProtocolAddress(mRegistrationId + "", 2));
    CiphertextMessage message = null;
    try {
       message = sessionCipher.encrypt("Ayush is awesome".getBytes("UTF-8"));
    } catch (Exception e) {
    }
    mMessage = message.serialize();
    try {
      Log.d("LOGIN_ACTIVITY", new String(mMessage, "UTF-8"));
    } catch (Exception e) {
      Log.d("LOGIN_ACTIVITY", "ERROR");
    }
  }

  SessionStore mSessionStore = null;
  PreKeyStore mPreKeyStore = null;
  SignedPreKeyStore mSignedPreKeyStore = null;
  IdentityKeyStore mIdentityKeyStore = null;

  SessionCipher mRemoteSessionCipher = null;

  // Remote
  public void randomGenerateKey() {
    // Create keys on installation.
    mIdentityKey = KeyHelper.generateIdentityKeyPair();
    mRegistrationId = KeyHelper.generateRegistrationId(false);
    List<PreKeyRecord> preKeys = KeyHelper.generatePreKeys(1, 100);
    mPreKey = preKeys.get(0);
    try {
      mSignedPreKey = KeyHelper.generateSignedPreKey(mIdentityKey, 5);
    } catch (InvalidKeyException e) {
    }

    // Load keys and session information from the device.
    int deviceId = 2;

    mSessionStore = new InMemorySessionStore();
    mPreKeyStore = new InMemoryPreKeyStore();
    for (PreKeyRecord preKeyRecord : preKeys) {
      mPreKeyStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
    }
    mSignedPreKeyStore = new InMemorySignedPreKeyStore();
    mSignedPreKeyStore.storeSignedPreKey(mSignedPreKey.getId(), mSignedPreKey);
    mIdentityKeyStore = new InMemoryIdentityKeyStore(mIdentityKey, mRegistrationId);
  }

  public void randomReceive() {
    PreKeySignalMessage preKeySignalMessage = null;
    try {
      preKeySignalMessage = new PreKeySignalMessage(mMessage);
    } catch (Exception e) {
      Log.d("LOGIN_ACTIVITY", "ERROR 1");
    }
    mRemoteSessionCipher = new SessionCipher(
        mSessionStore,
        mPreKeyStore,
        mSignedPreKeyStore,
        mIdentityKeyStore,
        new SignalProtocolAddress(preKeySignalMessage.getRegistrationId() + "", 1));
    byte[] message = null;
    try {
      message = mRemoteSessionCipher.decrypt(preKeySignalMessage);
    } catch (Exception e) {
      Log.d("LOGIN_ACTIVITY", e.toString());
    }
    try {
      Log.d("LOGIN_ACTIVITY", new String(message, "UTF-8"));
    } catch (Exception e) {
      Log.d("LOGIN_ACTIVITY", "ERROR 3");
    }
  }
}

