package com.haks.ghost;

import java.io.Serializable;

public class User implements Serializable {
  private int mUserId;
  private String mUsername;
  private String mName;

  public User(int userId, String username, String name) {
    mUserId = userId;
    mUsername = username;
    mName = name;
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

  public String getName() {
    return mName;
  }

  public void setName(String name) {
    mName = name;
  }
}
