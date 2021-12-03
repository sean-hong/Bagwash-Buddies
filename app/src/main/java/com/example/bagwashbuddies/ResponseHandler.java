package com.example.bagwashbuddies;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResponseHandler extends Request<JSONObject> {
    private final Listener<JSONObject> listener;
    private final Map<String, String> params;

    public ResponseHandler(int method, String url, Map<String, String> params, Listener<JSONObject> respListener, ErrorListener errListener) {
        super(method, url, errListener);
        this.listener = respListener;
        this.params = params;
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    @Override
    protected void deliverResponse(JSONObject resp) {
        listener.onResponse(resp);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse resp) {
        try {
            String jsonString = new String(resp.data, HttpHeaderParser.parseCharset(resp.headers));
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(resp));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Bearer DNGVMSZCJWHNHH7CT4PTMPHIM2GPJ6O6");

        return headerMap;
    }
}
