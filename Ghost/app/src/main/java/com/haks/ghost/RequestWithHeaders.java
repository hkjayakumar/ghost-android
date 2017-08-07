package com.haks.ghost;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestWithHeaders extends JsonObjectRequest {
  private Map<String, String> mHeaders;

  public RequestWithHeaders(int method, String url, JSONObject body, Response.Listener<JSONObject> onSuccess, Response.ErrorListener onError) {
    super(method, url, body, onSuccess, onError);
    mHeaders = new HashMap<>();
  }

  @Override
  public Map<String, String> getHeaders() throws AuthFailureError {
    mHeaders.putAll(super.getHeaders());
    return mHeaders;
  }

  public void addHeader(String key, String value) {
    mHeaders.put(key, value);
  }

  // Value can be either username:password or token:.
  public void addAuthHeader(String value) {
    mHeaders.put("Authorization",
        "Basic " + Base64.encodeToString(value.getBytes(), Base64.DEFAULT));
  }
}