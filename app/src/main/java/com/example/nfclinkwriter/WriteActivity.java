package com.example.nfclinkwriter;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.IntentFilter;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WriteActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilters;
    private EditText urlEditText;
    private ImageView iconImageView;
    private Bitmap selectedIcon;
    private String urlToWrite = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        urlEditText = findViewById(R.id.editTextUrl);
        iconImageView = findViewById(R.id.imageViewIcon);
        Button selectIconButton = findViewById(R.id.btnSelectIcon);
        Button writeButton = findViewById(R.id.btnWriteTag);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(
            this, 0, new Intent(this, getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE);
        intentFilters = new IntentFilter[]{
            new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};

        selectIconButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        writeButton.setOnClickListener(v -> {
            urlToWrite = urlEditText.getText().toString().trim();
            if (urlToWrite.isEmpty()) {
                Toast.makeText(WriteActivity.this,
                    "Please enter a URL", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedIcon = MediaStore.Images.Media.getBitmap(
                    getContentResolver(), imageUri);
                iconImageView.setImageBitmap(selectedIcon);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null && pendingIntent != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeToTag(tag);
        }
    }

    private void writeToTag(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                Toast.makeText(this, "Tag not NDEF formatted",
                    Toast.LENGTH_SHORT).show();
                return;
            }

            NdefRecord urlRecord = NdefRecord.createUri(urlToWrite);
            NdefRecord iconRecord = null;
            if (selectedIcon != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedIcon.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] iconBytes = baos.toByteArray();
                iconRecord = NdefRecord.createMime("image/png", iconBytes);
            }

            NdefRecord[] records = iconRecord != null ?
                new NdefRecord[]{urlRecord, iconRecord} :
                new NdefRecord[]{urlRecord};

            NdefMessage message = new NdefMessage(records);
            ndef.connect();
            ndef.writeNdefMessage(message);
            ndef.close();

            Toast.makeText(this, "Tag written successfully!",
                Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Write failed: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
        }
    }
}
