package nus.cs5248.group1;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.cookie.CookieOrigin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.Intent;
import android.net.ParseException;
import android.os.AsyncTask;
import android.util.Log;

public enum BuildConnection
{
	INSTANCE; // Singleton

	public static String urlFor(String restAction)
	{
		return BASE_URL + restAction;
	}

	public static final String TAG = "DASH Player:";

	public static final String LOGIN = "wp-login.php";
	public static final String CREATE_VIDEO = "index.php";
	public static final String LOGIN_USER = "log";
	public static final String LOGIN_PASS = "pwd";
	public static final String VIDEO_TITLE = "async-upload";
	public static final String BASE_URL = "http://pilatus.d1.comp.nus.edu.sg/~a0092701/home/";
}

class CreateVideoUploadTaskParam 
{
	static CreateVideoUploadTaskParam create(String video)
	{
		CreateVideoUploadTaskParam param = new CreateVideoUploadTaskParam();
		param.video = video;
		return param;
	}

	String video;
}

class CreateVideoUploadTask extends AsyncTask<CreateVideoUploadTaskParam, Integer, Integer>
{
	int result;
	
	@Override
	protected Integer doInBackground(CreateVideoUploadTaskParam... param)
	{
		try
		{
			HttpPost post = new HttpPost(BuildConnection.urlFor(BuildConnection.LOGIN));
			DefaultHttpClient client = new DefaultHttpClient();

			List<NameValuePair> postParams = new ArrayList<NameValuePair>();
			postParams.add(new BasicNameValuePair(BuildConnection.LOGIN_USER, "test"));
			postParams.add(new BasicNameValuePair(BuildConnection.LOGIN_PASS, "test1"));
			post.setEntity(new UrlEncodedFormEntity(postParams));

			CookieStore cookieStore = new BasicCookieStore();
			client.setCookieStore(cookieStore);

			CookieSpecFactory csf = new CookieSpecFactory()
			{
				public CookieSpec newInstance(HttpParams params)
				{
					return new BrowserCompatSpec()
					{
						@Override
						public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException
						{
							// Oh, I am easy.
							// Allow all cookies
							Log.e("custom validate", "SC");
						}
					};
				}
			};
			client.getCookieSpecs().register("easy", csf);
			client.getParams().setParameter(ClientPNames.COOKIE_POLICY, "easy");

			HttpResponse postResponse = client.execute(post);
			HttpEntity responseEntity = postResponse.getEntity();

			if (responseEntity != null)
			{
				this.responseAsText = EntityUtils.toString(responseEntity);
				CreateVideoUploadTask.cookies = client.getCookieStore().getCookies();
				if (cookies != null)
				{
					int size = cookies.size();
					for (int i = 0; i < size; i++)
					{
						client.getCookieStore().addCookie(cookies.get(i));
					}
				}
			}

			HttpPost httppost = new HttpPost(BuildConnection.urlFor(BuildConnection.CREATE_VIDEO));

			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			File file = new File(Storage.getMediaFolder(true), param[0].video);
			entity.addPart("async-upload", new FileBody(file));

			httppost.setEntity(entity);
			HttpResponse response = client.execute(httppost);

			HttpEntity resEntity = response.getEntity();

			if (resEntity != null)
			{
				this.responseAsText = EntityUtils.toString(resEntity);
				result = Result.OK;
			}
		}
		catch (UnsupportedEncodingException e)
		{
			Log.e(BuildConnection.TAG, "Unsupported encoding exception: " + e.getMessage());
			result = Result.FAIL;
		}
		catch (ClientProtocolException e)
		{
			Log.e(BuildConnection.TAG, "Client protocol exception: " + e.getMessage());
			result = Result.FAIL;
		}
		catch (IOException e)
		{
			Log.e(BuildConnection.TAG, "IO exception: " + e.getMessage());
			result = Result.FAIL;
		}
		catch (ParseException e)
		{
			Log.e(BuildConnection.TAG, "JSON parse exception: " + e.getMessage());
			result = Result.FAIL;
		}
		catch (Exception e)
		{
			Log.e(BuildConnection.TAG, "Unexpected exception: " + e.getMessage());
			e.printStackTrace();
			result = Result.FAIL;
		}
		return result;
	}

	protected String responseAsText;
	protected static List<Cookie> cookies;
}