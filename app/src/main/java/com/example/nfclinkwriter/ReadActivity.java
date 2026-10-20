package com.example.nfclinkwriter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;

public class ReadActivity extends AppCompatActivity {

    private TextView urlTextView;
    private ImageView iconImageView;
    private Button openButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        urlTextView = findViewById(R.id.textViewUrl);
        iconImageView = findViewById(R.id.imageViewRead);
        openButton = findViewById(R.id.btnOpenLink);

        openButton.setOnClickListener(v -> {
            String url = urlTextView.getText().toString();
            if (!url.isEmpty() && url.startsWith("http")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            readFromTag(intent);
        }
    }

    private void readFromTag(Intent intent) {
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(
            NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMessages == null) return;

        NdefMessage message = (NdefMessage) rawMessages[0];
        NdefRecord[] records = message.getRecords();

        for (NdefRecord record : records) {
            if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                Arrays.equals(record.getType(), NdefRecord.RTD_URI)) {
                String url = new String(record.getPayload());
                urlTextView.setText(url);
            }
            if (                record.getTnf() == NdefRecord.TNF_MIME_MEDIA &&
                new String(record.getType()).equals("image/png")) {
                byte[] iconBytes = record.getPayload();
                Bitmap icon = BitmapFactory.decodeByteArray(
                    iconBytes, 0, iconBytes.length);
                iconImageView.setImageBitmap(icon);
            }
        }
        Toast.makeText(this, "Tag read successfully!",
            Toast.LENGTH_SHORT).show();
    }
}
