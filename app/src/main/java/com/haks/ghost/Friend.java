package com.haks.ghost;

import java.io.Serializable;

public class Friend implements Serializable {
  private int mUserId;
  private String mUsername;
  private int mRegistrationId;
  private int mDeviceId;

  public Friend(int userId, String username, int registrationId, int deviceId) {
    mUserId = userId;
    mUsername = username;
    mRegistrationId = registrationId;
    mDeviceId = deviceId;
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
}
