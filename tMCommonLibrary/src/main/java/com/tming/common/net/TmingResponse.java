package com.tming.common.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.tming.common.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class TmingResponse {

	private static final String LOG_TAG = "TmingResponse";
	private int statusCode;
	private Document responseAsDocument = null;
	private String responseAsString = null;
	private InputStream is;
	private HttpURLConnection con;
	private boolean streamConsumed = false;

	public TmingResponse(HttpURLConnection con) throws IOException {
		this.con = con;
		this.statusCode = con.getResponseCode();
		if (null == (is = con.getErrorStream())) {
			is = con.getInputStream();
		}
		if (null != is && "gzip".equals(con.getContentEncoding())) {
			// the response is gzipped
			is = new GZIPInputStream(is);
		}
	}

    public HttpURLConnection getHttpURLConnection() {
        return con;
    }

    public int getStatusCode() {
		return statusCode;
	}

	public String getResponseHeader(String name) {
		if (con != null)
			return con.getHeaderField(name);
		else
			return null;
	}

	/**
	 * Returns the response stream.<br>
	 * It is suggested to call disconnect() after consuming the stream.
	 * 
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body stream
	 * @see #disconnect()
	 */
	public InputStream asStream() {
		if (streamConsumed) {
			throw new IllegalStateException("Stream has already been consumed.");
		}
		return is;
	}

	/**
	 * Returns the response body as org.json.JSONObject.<br>
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body as org.json.JSONObject
	 * @throws TmingHttpException
	 */
	public JSONObject asJSONObject() throws TmingHttpException {
		try {
			return new JSONObject(asString());
		} catch (JSONException jsone) {
			return null;
			// throw new TmingException(jsone.getMessage() + ":" +
			// this.responseAsString, jsone);
		}
	}

	/**
	 * Returns the response body as org.json.JSONArray.<br>
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body as org.json.JSONArray
	 * @throws TmingHttpException
	 */
	public JSONArray asJSONArray() throws TmingHttpException {
		try {
			return new JSONArray(asString());
		} catch (Exception jsone) {
			throw new TmingHttpException(jsone.getMessage() + ":"
					+ this.responseAsString, jsone);
		}
	}

	/**
	 * Returns the response body as string.<br>
	 * Disconnects the internal HttpURLConnection silently.
	 * 
	 * @return response body
	 * @throws TmingHttpException
	 */
	public String asString() throws TmingHttpException {
		if (null == responseAsString) {
            ByteArrayOutputStream bytesOS = null;
            try {
				InputStream stream = asStream();
				if (null == stream) {
					return null;
				}
                bytesOS = new ByteArrayOutputStream();
				int len;
                byte[] buf = new byte[1024];
				while ((len = stream.read(buf)) != -1) {
					bytesOS.write(buf, 0, len);
				}
				this.responseAsString = new String(bytesOS.toByteArray(), "UTF-8");
				Log.d(LOG_TAG, responseAsString);
				streamConsumed = true;
			} catch (NullPointerException npe) {
				// don't remember in which case npe can be thrown
				throw new TmingHttpException(npe.getMessage(), npe);
			} catch (IOException ioe) {
				throw new TmingHttpException(ioe.getMessage(), ioe);
			} catch (OutOfMemoryError e) {
				throw new TmingHttpException("OutOfMemoryError");
			} finally {
                if (bytesOS != null) {
                    try {
                        bytesOS.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
			    if (con != null) {
			        con.disconnect();
			    }
			}
		}
		return responseAsString;
	}

	public void disconnect() {
		con.disconnect();
	}

	@Override
	public String toString() {
		if (null != responseAsString) {
			return responseAsString;
		}
		return "Response{" + "statusCode=" + statusCode + ", response="
				+ responseAsDocument + ", responseString='" + responseAsString
				+ '\'' + ", is=" + is + ", con=" + con + '}';
	}
}