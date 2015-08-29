package ca.campbell.gasprice;

import android.app.Activity;
import android.os.Bundle;
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
 * @version  beta
 * 
 * Select US or Canadian (Default to US) radio button 
 * Enter the cost of gas  
 * 	assuming per gallon if US
 * 	assuming per litre if CAN
 * If US 
 * 	show Cost per litre in US$ and in CAN$
 * If CAN
 * 	show Cost per gallon in US$ and in CAN$
 * 
 * future: 
 * Use location data instead of radio button
 * Use current conversion data via an api
 * Incorporate material design, make it slick 
 *
 */

public class MainActivity extends Activity {
    // eventually get rate
	// use http://api.fixer.io/latest?symbols=USD,CAN
	public static final float EXCH_US_TO_CAN = 1.3269F;
    public static final float EXCH_CAN_TO_US = 0.7536F;
    // given
    public static final float USGAL_TO_LITRE = 3.78541F;
    public static final float LITRE_TO_USGAL = 0.264172F;
    public boolean UStoCan = true;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void calcConversion(View view) {
		EditText gasPrice = (EditText) findViewById(R.id.input_price);
		TextView calcGasPrice = (TextView) findViewById(R.id.calc_price);
		TextView msg = (TextView) findViewById(R.id.message);
		String strGasPrice = gasPrice.getText().toString();
		float fgasPrice = Float.parseFloat(strGasPrice);
		float fcalcGasPrice, calcPrice;
	
		if (UStoCan)  {
		// Given US $ price for a gallon
	    // multiply by Canadian exchange 
		// divide by num of litres in a gallon
		
        calcPrice = Math.round(fgasPrice*EXCH_US_TO_CAN / USGAL_TO_LITRE);
        calcGasPrice.setText(Float.toString(calcPrice));
        msg.setText(R.string.toCan);
		} else {
			// Given Canadian $ price for a litre
		    // multiply by US exchange 
			// divide by num of gallons in a litre
			
	        calcPrice = Math.round(fgasPrice*EXCH_CAN_TO_US / LITRE_TO_USGAL);
	        calcGasPrice.setText(Float.toString(calcPrice));
	        msg.setText(R.string.toUS);
		}
	}
	public void onSelCountry(View view) {
	    boolean checked = ((RadioButton) view).isChecked();
	    switch(view.getId()) {
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
}
