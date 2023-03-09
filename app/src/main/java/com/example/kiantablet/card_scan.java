package com.example.kiantablet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.kiantablet.records.ParsedNdefRecord;
import com.example.kiantablet.records.TextRecord;
import com.example.kiantablet.support.NdefMessageParser;
import com.example.kiantablet.support.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class card_scan extends AppCompatActivity {
    private static final String TAG = Class.class.getSimpleName();
    private Tag tagToBeWritten;
    Button next, showData;
    EditText natId, name;
    LinearLayout mContent;

    private static final DateFormat TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
    private NfcAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_scan);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        next = findViewById(R.id.next);
        showData = findViewById(R.id.Verify);
        natId = findViewById(R.id.NatId);
        name = findViewById(R.id.Name);
        mContent=  (LinearLayout) findViewById(R.id.list);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(card_scan.this,face_scan.class));
            }
        });
        showData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Visitor visitor = null;
                    DbSetup db = new DbSetup();
                    visitor = db.GetCardHolderByCardNum(natId.getText().toString());
                    if (visitor != null) {
                        name.setText(visitor.getName());
                    }
                } catch (Exception e) {
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }
            break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            displayDetectedRecords(getNdefMessages(intent));
        }
    }

    /**
     * Get NDEF message from intent
     */
    private NdefMessage[] getNdefMessages(Intent intent) {
        NdefMessage[] ndefMsgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawNdefMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawNdefMsgs != null) {
                ndefMsgs = new NdefMessage[rawNdefMsgs.length];
                java.lang.System.arraycopy(rawNdefMsgs, 0, ndefMsgs, 0, rawNdefMsgs.length);
            } else {
                // Unknown tag type
                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                byte[] payload = Utils.dumpTagData(tag).getBytes();

                NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_UNKNOWN, new byte[0], id, payload);
                NdefMessage ndefMsg = new NdefMessage(new NdefRecord[]{ndefRecord});
                ndefMsgs = new NdefMessage[]{ndefMsg};
            }
        } else {
            Log.e(TAG, "Unknown intent.");
            finish();
        }
        return ndefMsgs;
    }

    /**
     * Display data that read by application
     */
    public void displayDetectedRecords(NdefMessage[] ndefMsgs) {
        if (ndefMsgs == null || ndefMsgs.length == 0) {
            return;
        }


        // Parse the first message in the list
        // Build views for all of the sub records
        Date now = new Date();

        List<ParsedNdefRecord> records = NdefMessageParser.parse(ndefMsgs[0]);
        final int size = records.size();
        for (int i = 0; i < size; i++) {

            ParsedNdefRecord record = records.get(i);
            mContent.addView(record.getParsedView(this), 1 + i);
            String Name2 = TextRecord.sendText;
            natId.setText(TextRecord.sendText);
            showData.performClick();

        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                Utils.showWirelessSettingsDialog(this);
            } else {
                // Enable foreground dispatch to the given Activity.
                // This will give give priority to the foreground activity when
                // dispatching a discovered Tag to an application.

                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                        getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                mAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Disable foreground dispatch to the given activity.
        //An activity must call this method before its onPause() callback completes.
        mAdapter.disableForegroundDispatch(this);
    }
}
