package ca.campbell.gasprice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * 
 * @author P. Campbell
 * @param none
 * @returns none
 * @version beta
 * 
 *          Select US or Canadian (Default to US) radio button Enter the cost of
 *          gas assuming per gallon if US assuming per litre if CAN If US show
 *          Cost per litre in US$ and in CAN$ If CAN show Cost per gallon in US$
 *          and in CAN$
 * 
 *          future: Use location data instead of radio button Use current
 *          conversion data via an api Incorporate material design, make it
 *          slick
 *
 */

public class MainActivity extends Activity {
	public static final String TAG = "gassy";
	public double EXCH_US_TO_CAN = 1.0;
	public double EXCH_CAN_TO_US = 1.0;
	// given
	public static final float USGAL_TO_LITRE = 3.78541F;
	public static final float LITRE_TO_USGAL = 0.264172F;
	public boolean UStoCan = true;
	TextView msg, moneyValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		msg = (TextView) findViewById(R.id.message);
		moneyValue = (TextView) findViewById(R.id.moneyValue);
		setExchange();
	}

	public void calcConversion(View view) {
		EditText gasPrice = (EditText) findViewById(R.id.input_price);
		TextView calcGasPrice = (TextView) findViewById(R.id.calc_price);

		String strGasPrice = gasPrice.getText().toString();
		double fgasPrice = Double.parseDouble(strGasPrice);
		double fcalcGasPrice, calcPrice;
		if (UStoCan) {
			// Given US $ price for a gallon
			// multiply by Canadian exchange
			// divide by num of litres in a gallon
			Log.d(TAG, "US to Canadaian $ only "+ fgasPrice*EXCH_US_TO_CAN);
			calcPrice = Math.round((fgasPrice * EXCH_US_TO_CAN / USGAL_TO_LITRE)*100)/100.0;
			moneyValue.setText(Double.toString(fgasPrice*EXCH_US_TO_CAN));
			calcGasPrice.setText(Double.toString(calcPrice));
			msg.setText(R.string.toCan);
		} else {
			// Given Canadian $ price for a litre
			// multiply by US exchange
			// divide by num of gallons in a litre

			calcPrice = Math.round(fgasPrice * EXCH_CAN_TO_US / LITRE_TO_USGAL);
			calcGasPrice.setText(Double.toString(calcPrice));
			msg.setText(R.string.toUS);
		}
	}

	public void onSelCountry(View view) {
		boolean checked = ((RadioButton) view).isChecked();
		switch (view.getId()) {
		case R.id.radio_can:
			if (checked)
				UStoCan = true;
			break;
		case R.id.radio_us:
			if (checked)
				UStoCan = false;
			break;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setExchange() {
		final String APIURL_USTOCAN = "http://api.fixer.io/latest?base=USD&symbols=CAD";
		final String APIURL_CANTOUS = "http://api.fixer.io/latest?base=CAD&symbols=USD";
		// first check to see if we can get on the network
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// invoke the AsyncTask to do the dirty work.
			new UseGasAPI().execute(APIURL_USTOCAN);
		} else {
			msg.setText("No network connection available.");
		}
	} // setExchange()

	private class UseGasAPI extends AsyncTask<String, Void, String> {
		@Override
		// runs in background (not in UI thread)
		protected String doInBackground(String... urls) {
			// params comes from the execute() call: params[0] is the url.adga
			try {
				return downloadUrl(urls[0]);
			} catch (IOException e) {
				Log.e(TAG, "exception" + Log.getStackTraceString(e));
				return "error";
			}
		} // doInBackground()

		protected void onPostExecute(String exchg) {
				EXCH_US_TO_CAN = getExchange(exchg);
				Log.e(TAG, "Exchagne rate US to Canadian " +EXCH_US_TO_CAN);
		} // onPostExecute()
	} // AsyncTask UseGasAPI()

	private double getExchange(String jsonStr) {
		final double ERROR = -1.0;
		try {
			JSONObject mResponseObj = new JSONObject(jsonStr);
			JSONObject responseObj = mResponseObj.getJSONObject("rates");
			String rate = responseObj.getString("CAD");
			return toDouble(rate);

		} catch (Exception e) {
			Log.e(TAG, "exception JSON "+Log.getStackTraceString(e));
			return ERROR;
		}
	} // getExchange()

	private double toDouble(String strNum) {
		double number;
		try {
			number = Double.parseDouble(strNum);
		} catch (NumberFormatException e) {
			number = -1.0;

		}
		return number;

	} // toDouble()
	/*
	 * Given a URL, establishes an HttpUrlConnection and retrieves the web page
	 * content as a InputStream, which it returns as a string.
	 */

	private String downloadUrl(String myurl) throws IOException {
	// private String downloadUrl(String myurl) {
		InputStream is = null;
		URL url;
		// Only read the first 500 characters of the retrieved
		// web page content.
		int len = 500;
		HttpURLConnection conn = null;
		try {
			// URL throws MalformedURLException is an IOException
			url = new URL(myurl);
			// create and open the connection
			conn = (HttpURLConnection) url.openConnection();

			/*
			 * set maximum time to wait for stream read read fails with
			 * SocketTimeoutException if elapses before connection established
			 * 
			 * in milliseconds
			 * 
			 * default: 0 - timeout disabled
			 */
			conn.setReadTimeout(10000);
			/*
			 * set maximum time to wait while connecting connect fails with
			 * SocketTimeoutException if elapses before connection established
			 * 
			 * in milliseconds
			 * 
			 * default: 0 - forces blocking connect timeout still occurs but
			 * VERY LONG wait ~several minutes
			 */
			conn.setConnectTimeout(15000 /* milliseconds */);
			/*
			 * HTTP Request method defined by protocol
			 * GET/POST/HEAD/POST/PUT/DELETE/TRACE/CONNECT
			 * 
			 * default: GET
			 */
			conn.setRequestMethod("GET");
			// specifies whether this connection allows receiving data
			conn.setDoInput(true);
			// Starts the query
			conn.connect();

			int response = conn.getResponseCode();
			Log.d(TAG, "Server returned: " + response);

			/*
			 * check the status code HTTP_OK = 200 anything else we didn't get
			 * what we want in the data.
			 */
			if (response != HttpURLConnection.HTTP_OK)
				return "Server returned: " + response + " aborting read.";

			// get the stream for the data from the website
			is = conn.getInputStream();
			// read the stream (max len bytes)
			String contentAsString = readIt(is, len);
			Log.d(TAG, contentAsString.substring(0,
						contentAsString.length() < 100 ? contentAsString.length() : 100 ));
			return contentAsString;
			/*
		} catch (IOException e) {
			Log.e(TAG, "exception" + Log.getStackTraceString(e));
			return "error";
			// throw e;
			 * */
		} finally {
			/*
			 * Make sure that the InputStream is closed after the app is
			 * finished using it. Make sure the connection is closed after the
			 * app is finished using it.
			 */

			if (is != null) {
				try {
					is.close();
				} catch (IOException ignore) {
				}
				if (conn != null)
					try {
						conn.disconnect();
					} catch (IllegalStateException ignore) {
					}
			}
		}
	} // downloadUrl()

	//
	/*
	 * Reads stream from HTTP connection and converts it to a String. See
	 * istackoverflow or a good explanation of why I did it this way.
	 * http://stackoverflow
	 * .com/questions/3459127/should-i-buffer-the-inputstream
	 * -or-the-inputstreamreader
	 */
	public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
		char[] buffer = new char[len];
		char[] resize;
		Reader reader = null;
		reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), len);
		int count = reader.read(buffer);
		Log.d(TAG, "Bytes read: " + count + "(-1 means end of reader so max of " + len + " )");
		// TODO
		// create a buffer of size of data
		// maybe take this out, too time consuming does not gain ??? tb
		resize = new char[count];
		for (int i=0 ; i<count ; i++) 
			resize[i] = buffer[i];
		return new String(resize);
	} // readIt()

}
