package com.haks.ghost;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

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
  private EditText mEmailView;
  private EditText mPasswordView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    // Set up the login form.
    mEmailView = (EditText)findViewById(R.id.email);
    mPasswordView = (EditText)findViewById(R.id.password);

    Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
    mEmailSignInButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        attemptLogin();
      }
    });
  }

  private void attemptLogin() {
    // Reset errors.
    mEmailView.setError(null);
    mPasswordView.setError(null);

    // Store values at the time of the login attempt.
    String email = mEmailView.getText().toString();
    String password = mPasswordView.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password, if the user entered one.
    if (TextUtils.isEmpty(password)) {
      mPasswordView.setError(getString(R.string.error_field_required));
      focusView = mPasswordView;
      cancel = true;
    }

    // Check for a valid email address.
    if (TextUtils.isEmpty(email)) {
      mEmailView.setError(getString(R.string.error_field_required));
      focusView = mEmailView;
      cancel = true;
    }

    if (cancel) {
      focusView.requestFocus();
      return;
    }

    new UserLoginTask(email, password, this).execute((Void) null);
  }

  public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
    private final String mEmail;
    private final String mPassword;
    private final Activity mCurrentActivity;

    UserLoginTask(String email, String password, Activity currentActivity) {
      mEmail = email;
      mPassword = password;
      mCurrentActivity = currentActivity;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      try {
        // Simulate network access.
        randomGenerateKey();
        randomMethod();
        randomReceive();
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        return false;
      }
      return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
      if (success) {
        Intent intent = new Intent(mCurrentActivity, FriendsListActivity.class);
        startActivity(intent);
      } else {
        mPasswordView.setError(getString(R.string.error_invalid_login_credentials));
        mPasswordView.requestFocus();
      }
    }
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
      Log.d("AYUSHTAG", new String(mMessage, "UTF-8"));
    } catch (Exception e) {
      Log.d("AYUSHTAG", "ERROR");
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
      Log.d("HKTAG", "ERROR 1");
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
      Log.d("HKTAG", e.toString());
    }
    try {
      Log.d("HKTAG", new String(message, "UTF-8"));
    } catch (Exception e) {
      Log.d("HKTAG", "ERROR 3");
    }
  }
}

