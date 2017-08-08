package com.haks.ghost;

import android.app.Activity;
import android.content.SharedPreferences;

public class User {
  public final static String USER_SP_FILE_NAME = "User_";

  public final static String USER_ID_SP_KEY = "USER_ID";
  private final static String USERNAME_SP_KEY = "USERNAME";
  public final static String TOKEN_SP_KEY = "TOKEN";
  private final static String REGISTRATION_ID_SP_KEY = "REGISTRATION_ID";
  private final static String DEVICE_ID_SP_KEY = "DEVICE_ID";

  private int mUserId;
  private String mUsername;
  private String mToken;
  private int mRegistrationId;
  private int mDeviceId;
  private GhostIdentityKeyStore mIdentityKeyStore;
  private GhostPreKeyStore mPreKeyStore;
  private GhostSignedPreKeyStore mSignedPreKeyStore;
  private GhostSessionStore mSessionStore;

  public User(int userId, String username, String token) {
    mUserId = userId;
    mUsername = username;
    mToken = token;
    mRegistrationId = 0;
    mDeviceId = 0;
    mIdentityKeyStore = new GhostIdentityKeyStore();
    mPreKeyStore = new GhostPreKeyStore();
    mSignedPreKeyStore = new GhostSignedPreKeyStore();
    mSessionStore = new GhostSessionStore();
  }

  public User(Activity activity) {
    this.read(activity);
  }

  public void save(Activity activity) {
    SharedPreferences.Editor editor =
        activity.getSharedPreferences(Constants.GHOST_SP_FILE_NAME, 0).edit();
    editor.putInt(USER_ID_SP_KEY, mUserId);
    editor.commit();

    SharedPreferences sharedPreferences =
        activity.getSharedPreferences(USER_SP_FILE_NAME + mUserId, 0);
    editor = sharedPreferences.edit();
    editor.putString(USERNAME_SP_KEY, mUsername);
    editor.putString(TOKEN_SP_KEY, mToken);
    editor.putInt(REGISTRATION_ID_SP_KEY, mRegistrationId);
    editor.putInt(DEVICE_ID_SP_KEY, mDeviceId);
    mIdentityKeyStore.save(sharedPreferences);
    mPreKeyStore.save(sharedPreferences);
    mSignedPreKeyStore.save(sharedPreferences);
    mSessionStore.save(sharedPreferences);
    editor.commit();
  }

  public void read(Activity activity) {
    SharedPreferences sharedPreferences =
        activity.getSharedPreferences(Constants.GHOST_SP_FILE_NAME, 0);
    mUserId = sharedPreferences.getInt(USER_ID_SP_KEY, 0);

    sharedPreferences = activity.getSharedPreferences(USER_SP_FILE_NAME + mUserId, 0);
    mUsername = sharedPreferences.getString(USERNAME_SP_KEY, "");
    mToken = sharedPreferences.getString(TOKEN_SP_KEY, "");
    mRegistrationId = sharedPreferences.getInt(REGISTRATION_ID_SP_KEY, 0);
    mDeviceId = sharedPreferences.getInt(DEVICE_ID_SP_KEY, 0);
    mIdentityKeyStore = new GhostIdentityKeyStore(sharedPreferences);
    mPreKeyStore = new GhostPreKeyStore(sharedPreferences);
    mSignedPreKeyStore = new GhostSignedPreKeyStore(sharedPreferences);
    mSessionStore = new GhostSessionStore(sharedPreferences);
  }

  public int getUserId() {
    return mUserId;
  }

  public void setUserId(int userId) {
    mUserId = userId;
  }

  public String getUsername() {
    return mUsername;
  }

  public void setUsername(String username) {
    mUsername = username;
  }

  public String getToken() {
    return mToken;
  }

  public void setToken(String token) {
    mToken = token;
  }

  public int getRegistrationId() {
    return mRegistrationId;
  }

  public void setRegistrationId(int registrationId) {
    mRegistrationId = registrationId;
  }

  public int getDeviceId() {
    return mDeviceId;
  }

  public void setDeviceId(int deviceId) {
    mDeviceId = deviceId;
  }

  public GhostIdentityKeyStore getIdentityKeyStore() {
    return mIdentityKeyStore;
  }

  public void setIdentityKeyStore(GhostIdentityKeyStore identityKeyStore) {
    mIdentityKeyStore = identityKeyStore;
  }

  public GhostPreKeyStore getPreKeyStore() {
    return mPreKeyStore;
  }

  public void setPreKeyStore(GhostPreKeyStore preKeyStore) {
    mPreKeyStore = preKeyStore;
  }

  public GhostSignedPreKeyStore getSignedPreKeyStore() {
    return mSignedPreKeyStore;
  }

  public void setSignedPreKeyStore(GhostSignedPreKeyStore signedPreKeyStore) {
    mSignedPreKeyStore = signedPreKeyStore;
  }

  public GhostSessionStore getSessionStore() {
    return mSessionStore;
  }

  public void setSessionStore(GhostSessionStore sessionStore) {
    mSessionStore = sessionStore;
  }
}
