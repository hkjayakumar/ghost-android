package com.haks.ghost;

import java.io.Serializable;

public class Friend implements Serializable {
  private int mUserId;
  private String mUsername;

  public Friend(int userId, String username) {
    mUserId = userId;
    mUsername = username;
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
}
