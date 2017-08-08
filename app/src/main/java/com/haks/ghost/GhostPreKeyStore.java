package com.haks.ghost;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.PreKeyStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GhostPreKeyStore implements PreKeyStore {
  private static final String STORE_SP_KEY = "PRE_KEY_STORE_";

  private Map<Integer, byte[]> mStore = new HashMap<>();

  public GhostPreKeyStore() {}

  public GhostPreKeyStore(List<PreKeyRecord> preKeyRecords) {
    for (PreKeyRecord preKeyRecord : preKeyRecords) {
      this.storePreKey(preKeyRecord.getId(), preKeyRecord);
      Log.d("GHOST_PRE_KEY_STORE", "PRE KEY: " + Base64.encodeToString(
          preKeyRecord.getKeyPair().getPublicKey().serialize(), Base64.DEFAULT));
    }
  }

  public GhostPreKeyStore(SharedPreferences sharedPreferences) {
    this.read(sharedPreferences);
  }

  public PreKeyRecord loadAnyPreKey() {
    try {
      Map.Entry<Integer, byte[]> entry = mStore.entrySet().iterator().next();
      return new PreKeyRecord(entry.getValue());
    } catch (Exception e) {
    }
    return null;
  }

  public List<PreKeyRecord> loadAll() {
    List<PreKeyRecord> preKeyRecords = new ArrayList<>();
    try {
      for (int preKeyId : mStore.keySet()) {
        preKeyRecords.add(new PreKeyRecord(mStore.get(preKeyId)));
      }
    } catch (Exception e) {
    }
    return preKeyRecords;
  }

  @Override
  public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {
    try {
      if (!mStore.containsKey(preKeyId)) {
        throw new InvalidKeyIdException("No such prekeyrecord!");
      }

      return new PreKeyRecord(mStore.get(preKeyId));
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void storePreKey(int preKeyId, PreKeyRecord record) {
    mStore.put(preKeyId, record.serialize());
  }

  @Override
  public boolean containsPreKey(int preKeyId) {
    return mStore.containsKey(preKeyId);
  }

  @Override
  public void removePreKey(int preKeyId) {
    mStore.remove(preKeyId);
  }

  public void save(SharedPreferences sharedPreferences) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    Map<String, ?> allItems = sharedPreferences.getAll();
    for (String key : allItems.keySet()) {
      if (key.startsWith(STORE_SP_KEY)) {
        editor.remove(key);
      }
    }
    for (int key : mStore.keySet()) {
      editor.putString(STORE_SP_KEY + key, Base64.encodeToString(mStore.get(key), Base64.DEFAULT));
      Log.d("GHOST_PRE_KEY_STORE", "PRE KEY WHEN SAVING "
          + Base64.encodeToString(mStore.get(key), Base64.DEFAULT));
    }
    editor.commit();
  }

  public void read(SharedPreferences sharedPreferences) {
    Map<String, ?> allItems = sharedPreferences.getAll();
    for (String key : allItems.keySet()) {
      if (key.startsWith(STORE_SP_KEY)) {
        int id = Integer.parseInt(key.substring(STORE_SP_KEY.length()));
        byte[] preKeyRecord = null;
        try {
          preKeyRecord = Base64.decode((String)allItems.get(key), Base64.DEFAULT);
        } catch (Exception e) {
        }
        mStore.put(id, preKeyRecord);
      }
    }
  }
}