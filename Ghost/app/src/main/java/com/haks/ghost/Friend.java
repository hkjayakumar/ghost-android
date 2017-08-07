package com.haks.ghost;

import java.io.Serializable;

/**
 * Created by Ayush on 2017-08-06.
 */

public class Friend implements Serializable {
  private String mUserId;
  private String mName;

  public Friend(String userId, String name) {
    mUserId = userId;
    mName = name;
  }

  public String getUserId() {
    return mUserId;
  }

  public void setUserId(String userId) {
    mUserId = userId;
  }

  public String getName() {
    return mName;
  }

  public void setName(String name) {
    mName = name;
  }
}
