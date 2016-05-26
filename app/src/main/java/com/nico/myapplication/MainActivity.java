package com.nico.myapplication;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.provider.Settings.Secure;

public class MainActivity extends AppCompatActivity {

	private TextView text;
	private PendingIntent pendingIntent;
	private IntentFilter[] intentFiltersArray;
	private NfcAdapter mNfcAdapter;
	private String[][] techListsArray;

	private String MIME_TYPE = "text/plain";
	private String[] validSerials = {"04ACC0DA993D80"};
	private String androidId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		text = new TextView(this);
		text.setText("So, no NFC then?");
		text.setTextSize(20);
		text.setBackgroundColor(Color.rgb(200, 200, 200));

		RelativeLayout layout = new RelativeLayout(this);
		RelativeLayout.LayoutParams textDetails = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT
		);

		textDetails.addRule(RelativeLayout.CENTER_HORIZONTAL);
		textDetails.addRule(RelativeLayout.CENTER_VERTICAL);

		layout.addView(text, textDetails);
		setContentView(layout);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			intentFilter.addDataType(MIME_TYPE);
		} catch (IntentFilter.MalformedMimeTypeException e) {
			throw new RuntimeException("failed", e);
		}
		intentFiltersArray = new IntentFilter[] {intentFilter, };
		techListsArray = new String[][] { new String[] { MainActivity.class.getName() } };

		androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mNfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);

		Intent intent = getIntent();
		if (intent != null) {
			handleNfc(intent);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleNfc(intent);
	}

	private void handleNfc(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String message = "Invalid serial code.";
			if (isValidSerial(getTagSerial(tag))) {
				message = "Valid serial code.";
			}
			text.setText(message);
		}
	}

	private String getTagSerial(Tag tag) {
		byte[] tagID = tag.getId();
		String idString = "";
		for (byte x : tagID) {
			idString = idString + String.format("%02X",x);
		}
		return idString;
	}

	private boolean isValidSerial(String serial) {
		boolean valid = false;
		for (String x : validSerials) {
			if (x.equals(serial)) {
				valid = true;
			}
		}
		return valid;
	}
}
