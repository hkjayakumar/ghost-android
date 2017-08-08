package com.haks.ghost;

import android.content.SharedPreferences;
import android.util.Base64;

import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SessionStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GhostSessionStore implements SessionStore {
  private static final String SESSIONS_SP_KEY = "SESSION_";

  private Map<SignalProtocolAddress, byte[]> mSessions = new HashMap<>();

  public GhostSessionStore() {}

  public GhostSessionStore(SharedPreferences sharedPreferences) {
    this.read(sharedPreferences);
  }

  @Override
  public synchronized SessionRecord loadSession(SignalProtocolAddress remoteAddress) {
    try {
      if (containsSession(remoteAddress)) {
        return new SessionRecord(mSessions.get(remoteAddress));
      } else {
        return new SessionRecord();
      }
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public synchronized List<Integer> getSubDeviceSessions(String name) {
    List<Integer> deviceIds = new LinkedList<>();

    for (SignalProtocolAddress key : mSessions.keySet()) {
      if (key.getName().equals(name) &&
          key.getDeviceId() != 1)
      {
        deviceIds.add(key.getDeviceId());
      }
    }

    return deviceIds;
  }

  @Override
  public synchronized void storeSession(SignalProtocolAddress address, SessionRecord record) {
    mSessions.put(address, record.serialize());
  }

  @Override
  public synchronized boolean containsSession(SignalProtocolAddress address) {
    return mSessions.containsKey(address);
  }

  @Override
  public synchronized void deleteSession(SignalProtocolAddress address) {
    mSessions.remove(address);
  }

  @Override
  public synchronized void deleteAllSessions(String name) {
    for (SignalProtocolAddress key : mSessions.keySet()) {
      if (key.getName().equals(name)) {
        mSessions.remove(key);
      }
    }
  }

  public void save(SharedPreferences sharedPreferences) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    Map<String, ?> allItems = sharedPreferences.getAll();
    for (String key : allItems.keySet()) {
      if (key.startsWith(SESSIONS_SP_KEY)) {
        editor.remove(key);
      }
    }
    for (SignalProtocolAddress address : mSessions.keySet()) {
      editor.putString(SESSIONS_SP_KEY + address.getName() + ":" + address.getDeviceId(),
          Base64.encodeToString(mSessions.get(address), Base64.DEFAULT));
    }
    editor.commit();
  }

  public void read(SharedPreferences sharedPreferences) {
    Map<String, ?> allItems = sharedPreferences.getAll();
    for (String key : allItems.keySet()) {
      if (key.startsWith(SESSIONS_SP_KEY)) {
        String addressString = key.substring(SESSIONS_SP_KEY.length());
        SignalProtocolAddress address = new SignalProtocolAddress(
            addressString.split(":")[0],
            Integer.parseInt(addressString.split(":")[1]));
        byte[] session = null;
        try {
          session = Base64.decode((String)allItems.get(key), Base64.DEFAULT);
        } catch (Exception e) {
        }
        mSessions.put(address, session);
      }
    }
  }
}