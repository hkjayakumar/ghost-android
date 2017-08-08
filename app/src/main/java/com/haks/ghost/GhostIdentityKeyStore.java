package com.haks.ghost;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.IdentityKeyStore;

import java.util.HashMap;
import java.util.Map;

public class GhostIdentityKeyStore implements IdentityKeyStore {
  private static final String TRUSTED_KEYS_SP_KEY = "TRUSTED_KEYS_";
  private static final String IDENTITY_KEY_PAIR_SP_KEY = "IDENTITY_KEY_PAIR";
  private static final String LOCAL_REGISTRATION_ID_SP_KEY = "LOCAL_REGISTRATION_ID";

  private Map<SignalProtocolAddress, IdentityKey> mTrustedKeys = new HashMap<>();

  private IdentityKeyPair mIdentityKeyPair;
  private int mLocalRegistrationId;

  public GhostIdentityKeyStore() {}

  public GhostIdentityKeyStore(IdentityKeyPair identityKeyPair, int localRegistrationId) {
    this.mIdentityKeyPair = identityKeyPair;
    this.mLocalRegistrationId = localRegistrationId;
  }

  public GhostIdentityKeyStore(SharedPreferences sharedPreferences) {
    this.read(sharedPreferences);
  }

  @Override
  public IdentityKeyPair getIdentityKeyPair() {
    return mIdentityKeyPair;
  }

  @Override
  public int getLocalRegistrationId() {
    return mLocalRegistrationId;
  }

  @Override
  public boolean saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
    IdentityKey existing = mTrustedKeys.get(address);

    if (!identityKey.equals(existing)) {
      mTrustedKeys.put(address, identityKey);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isTrustedIdentity(
      SignalProtocolAddress address,
      IdentityKey identityKey,
      Direction direction) {
    IdentityKey trusted = mTrustedKeys.get(address);
    return (trusted == null || trusted.equals(identityKey));
  }

  public void save(SharedPreferences sharedPreferences) {
    SharedPreferences.Editor editor = sharedPreferences.edit();

    Map<String, ?> allItems = sharedPreferences.getAll();
    for (String key : allItems.keySet()) {
      if (key.startsWith(TRUSTED_KEYS_SP_KEY)) {
        editor.remove(key);
      }
    }

    for (SignalProtocolAddress address : mTrustedKeys.keySet()) {
      editor.putString(TRUSTED_KEYS_SP_KEY + address.getName() + ":" + address.getDeviceId(),
          Base64.encodeToString(mTrustedKeys.get(address).serialize(), Base64.DEFAULT));
    }

    editor.putString(IDENTITY_KEY_PAIR_SP_KEY,
        Base64.encodeToString(mIdentityKeyPair.serialize(), Base64.DEFAULT));

    editor.putInt(LOCAL_REGISTRATION_ID_SP_KEY, mLocalRegistrationId);

    editor.commit();
  }

  public void read(SharedPreferences sharedPreferences) {
    Map<String, ?> allItems = sharedPreferences.getAll();
    for (String key : allItems.keySet()) {
      if (key.startsWith(TRUSTED_KEYS_SP_KEY)) {
        String addressString = key.substring(TRUSTED_KEYS_SP_KEY.length());
        Log.d("GHOST_IDENTITY_KEY_STOR", addressString);
        SignalProtocolAddress address = new SignalProtocolAddress(
            addressString.split(":")[0],
            Integer.parseInt(addressString.split(":")[1]));
        IdentityKey identityKey = null;
        try {
          identityKey =
              new IdentityKey(Base64.decode((String)allItems.get(key), Base64.DEFAULT), 0);
        } catch (Exception e) {
        }
        mTrustedKeys.put(address, identityKey);
      }
    }

    try {
      mIdentityKeyPair = new IdentityKeyPair(
          Base64.decode(sharedPreferences.getString(IDENTITY_KEY_PAIR_SP_KEY, ""), Base64.DEFAULT));
    } catch (Exception e) {
    }

    mLocalRegistrationId = sharedPreferences.getInt(LOCAL_REGISTRATION_ID_SP_KEY, 0);
  }
}
