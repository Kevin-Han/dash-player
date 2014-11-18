package nus.cs5248.group1.model;

import java.io.File;
import java.io.FilenameFilter;
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
import org.apache.http.client.methods.HttpGet;
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
import org.apache.http.cookie.CookieOrigin;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import android.annotation.SuppressLint;
import android.net.ParseException;
import android.util.Log;

public class Server
{
	public static String urlFor(String restAction)
	{
		return BASE_URL + restAction;
	}

	public static final String TAG = "DASH Player:";

	public static final String LOGIN = "wp-login.php";
	public static final String CREATE_VIDEO = "index.php";
	public static final String LIST_VIDEO = "wp-content/list.php";
	public static final String LIST_MPD = "wp-content/listMPD.php";
	public static final String LOGIN_USER = "log";
	public static final String LOGIN_PASS = "pwd";
	public static final String VIDEO_TITLE = "async-upload";
	public static final String VIDEO_LIST = "wp-admin/upload.php";
	public static final String BASE_URL = "http://pilatus.d1.comp.nus.edu.sg/~a0092701/home/";
	public static final String SEGMENT_BASE = "wp-content/segmentVideo/";
	
	public static DefaultHttpClient client;
	protected static String responseAsText;
	protected static List<Cookie> cookies;
	
	public DefaultHttpClient BuidlConnection()
	{
		try
		{
			HttpPost post = new HttpPost(Server.urlFor(Server.LOGIN));
			client = new DefaultHttpClient();

			List<NameValuePair> postParams = new ArrayList<NameValuePair>();
			postParams.add(new BasicNameValuePair(Server.LOGIN_USER, "test"));
			postParams.add(new BasicNameValuePair(Server.LOGIN_PASS, "test1"));
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
				Server.responseAsText = EntityUtils.toString(responseEntity);
				if (cookies != null)
				{
					int size = cookies.size();
					for (int i = 0; i < size; i++)
					{
						client.getCookieStore().addCookie(cookies.get(i));
					}
				}
			}
		}
		catch (UnsupportedEncodingException e)
		{
			Log.e(Server.TAG, "Unsupported encoding exception: " + e.getMessage());
		}
		catch (ClientProtocolException e)
		{
			Log.e(Server.TAG, "Client protocol exception: " + e.getMessage());
		}
		catch (IOException e)
		{
			Log.e(Server.TAG, "IO exception: " + e.getMessage());
		}
		catch (ParseException e)
		{
			Log.e(Server.TAG, "JSON parse exception: " + e.getMessage());
		}
		catch (Exception e)
		{
			Log.e(Server.TAG, "Unexpected exception: " + e.getMessage());
			e.printStackTrace();
		}
		return client;	
	}
	
	public static String getMP4FileListFromServer() {	
		FilenameFilter filter = new FilenameFilter() {
			@SuppressLint("DefaultLocale")
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".mp4");
			}
		};

		try
		{
           
			HttpGet get = new HttpGet(Server.urlFor(Server.VIDEO_LIST));
			cookies = client.getCookieStore().getCookies();
			
			HttpResponse response = client.execute(get);

			HttpEntity resEntity = response.getEntity();
			//InputStream resContent = resEntity.getContent();

			if (resEntity != null)
			{
				responseAsText = EntityUtils.toString(resEntity);	
				Document doc = Jsoup.parse(responseAsText);
				Elements ele = doc.select("div.filename");
				ele.toString();
			}
		}
		catch (UnsupportedEncodingException e)
		{
			Log.e(Server.TAG, "Unsupported encoding exception: " + e.getMessage());
		}
		catch (ClientProtocolException e)
		{
			Log.e(Server.TAG, "Client protocol exception: " + e.getMessage());
		}
		catch (IOException e)
		{
			Log.e(Server.TAG, "IO exception: " + e.getMessage());
		}
		catch (ParseException e)
		{
			Log.e(Server.TAG, "JSON parse exception: " + e.getMessage());
		}
		catch (Exception e)
		{
			Log.e(Server.TAG, "Unexpected exception: " + e.getMessage());
			e.printStackTrace();
		}

		return responseAsText;
	}
}
