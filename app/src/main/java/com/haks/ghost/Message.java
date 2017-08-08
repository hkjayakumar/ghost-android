package com.haks.ghost;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;

import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
  private String mMessage;
  private String mTimestamp;

  public Message(String message, String timestamp) {
    mMessage = message;
    mTimestamp = timestamp;
  }

  public Message(String message, Date timestamp) {
    mMessage = message;
    DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    mTimestamp = formatter.format(timestamp);
  }

  public String getMessage() {
    return mMessage;
  }

  public String getEncryptedMessage(User user, Friend friend, Activity activity) {
    SessionCipher sessionCipher = new SessionCipher(
        user.getSessionStore(),
        user.getPreKeyStore(),
        user.getSignedPreKeyStore(),
        user.getIdentityKeyStore(),
        new SignalProtocolAddress(friend.getRegistrationId() + "", friend.getDeviceId()));
    CiphertextMessage message = null;
    try {
      message = sessionCipher.encrypt(mMessage.getBytes("UTF-8"));
      // TODO(ayushbhagat): Figure out why you don't need to save the stores.
      // user.save(activity);
    } catch (Exception e) {
      Log.d("Message", "ERROR WHEN ENCRYPTING: " + e);
    }
    String ct = Base64.encodeToString(message.serialize(), Base64.DEFAULT);
    Log.d("MESSAGE", "CIPHERTEXT IS " + ct);
    // Debug.
    try {
      PreKeySignalMessage preKeySignalMessage =
          new PreKeySignalMessage(Base64.decode(ct, Base64.DEFAULT));
      Log.d("MESSAGE", "DECRYPTED THE ENCRYPTED MESSAGE SUCCESSFULLY");
    } catch (Exception e) {
      Log.d("MESSAGE", "CANNOT DECRYPT THE ENCRYPTED MESSAGE: " + e);
    }
    return ct;
  }

  public void decrypt(Friend friend, User user, Activity activity) {
    SessionCipher sessionCipher = new SessionCipher(
        user.getSessionStore(),
        user.getPreKeyStore(),
        user.getSignedPreKeyStore(),
        user.getIdentityKeyStore(),
        new SignalProtocolAddress(friend.getRegistrationId() + "", friend.getDeviceId()));
    try {
      mMessage = new String(
          sessionCipher.decrypt(new PreKeySignalMessage(Base64.decode(mMessage, Base64.DEFAULT))),
          "UTF-8");
      // TODO(ayushbhagat): Figure out why you need to revert the stores back to the values before
      // decryption.
      user.read(activity);
    } catch (Exception e) {
      Log.d("MESSAGE", "ERROR WHEN DECRYPTING: " + e);
    }
  }

  public void setMessage(String message) {
    mMessage = message;
  }

  public String getTimestamp() {
    return mTimestamp;
  }

  public void setTimestamp(String timestamp) {
    mTimestamp = timestamp;
  }
}
