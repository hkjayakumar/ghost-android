package com.haks.ghost;

import android.content.SharedPreferences;
import android.util.Base64;

import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GhostSignedPreKeyStore implements SignedPreKeyStore {
  private static final String STORE_SP_KEY = "SIGNED_PRE_KEY_STORE_";

  private Map<Integer, byte[]> mStore = new HashMap<>();
  private int mFirstKey;

  public GhostSignedPreKeyStore() {}

  public GhostSignedPreKeyStore(int signedPreKeyId, SignedPreKeyRecord record) {
    this.storeSignedPreKey(signedPreKeyId, record);
    mFirstKey = signedPreKeyId;
  }

  public GhostSignedPreKeyStore(SharedPreferences sharedPreferences) {
    this.read(sharedPreferences);
  }

  public SignedPreKeyRecord loadFirstSignedPreKey() {
    try {
      return new SignedPreKeyRecord(mStore.get(mFirstKey));
    } catch (Exception e) {
    }
    return null;
  }

  @Override
  public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {
    try {
      if (!mStore.containsKey(signedPreKeyId)) {
        throw new InvalidKeyIdException("No such signedprekeyrecord! " + signedPreKeyId);
      }

      return new SignedPreKeyRecord(mStore.get(signedPreKeyId));
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public List<SignedPreKeyRecord> loadSignedPreKeys() {
    try {
      List<SignedPreKeyRecord> results = new LinkedList<>();

      for (byte[] serialized : mStore.values()) {
        results.add(new SignedPreKeyRecord(serialized));
      }

      return results;
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {
    mStore.put(signedPreKeyId, record.serialize());
  }

  @Override
  public boolean containsSignedPreKey(int signedPreKeyId) {
    return mStore.containsKey(signedPreKeyId);
  }

  @Override
  public void removeSignedPreKey(int signedPreKeyId) {
    mStore.remove(signedPreKeyId);
  }

  public void save(SharedPreferences sharedPreferences) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    for (int key : mStore.keySet()) {
      editor.putString(STORE_SP_KEY + key, Base64.encodeToString(mStore.get(key), Base64.DEFAULT));
    }
    editor.commit();
  }

  public void read(SharedPreferences sharedPreferences) {
    Map<String, ?> allItems = sharedPreferences.getAll();
    for (String key : allItems.keySet()) {
      if (key.startsWith(STORE_SP_KEY)) {
        int id = Integer.parseInt(key.substring(STORE_SP_KEY.length()));
        byte[] signedPreKeyRecord = null;
        try {
          signedPreKeyRecord = Base64.decode((String)allItems.get(key), Base64.DEFAULT);
        } catch (Exception e) {
        }
        mStore.put(id, signedPreKeyRecord);
      }
    }
  }
}