import React, { useEffect, useState } from 'react';
import { StyleSheet, Text, TextInput, TouchableOpacity, View, Alert } from 'react-native';
import NfcManager, { NfcTech, Ndef } from 'react-native-nfc-manager';

export default function App() {
  const [url, setUrl] = useState('https://');
  const [status, setStatus] = useState('Initializing NFC...');
  const [supported, setSupported] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        await NfcManager.start();
        const isSupported = await NfcManager.isSupported();
        setSupported(isSupported);
        setStatus(isSupported ? 'NFC ready' : 'NFC not supported');
      } catch (e) {
        setStatus('NFC init failed');
      }
    })();
    return () => {
      NfcManager.cancelTechnologyRequest().catch(() => {});
    };
  }, []);

  const writeTag = async () => {
    if (!url.trim()) {
      Alert.alert('Error', 'Please enter a URL');
      return;
    }
    try {
      setStatus('Approach an NFC tag to write...');
      await NfcManager.requestTechnology(NfcTech.Ndef);
      const bytes = Ndef.encodeMessage([Ndef.uriRecord(url.trim())]);
      await NfcManager.ndefHandler.writeNdefMessage(bytes);
      setStatus('Tag written successfully!');
    } catch (e: any) {
      setStatus('Write failed: ' + (e?.message ?? e));
    } finally {
      NfcManager.cancelTechnologyRequest().catch(() => {});
    }
  };

  const readTag = async () => {
    try {
      setStatus('Approach an NFC tag to read...');
      await NfcManager.requestTechnology(NfcTech.Ndef);
      const tag = await NfcManager.ndefHandler.getNdefMessage();
      const records = tag?.ndefMessage ?? [];
      let foundUrl = '';
      for (const r of records) {
        if (r.tnf === Ndef.TNF_WELL_KNOWN && Array.isArray(r.type) && r.type[0] === 0x55) {
          foundUrl = Ndef.uri.decodePayload(new Uint8Array(r.payload));
        }
      }
      if (foundUrl) {
        setUrl(foundUrl);
        setStatus('Read: ' + foundUrl);
      } else {
        setStatus('No URL record found');
      }
    } catch (e: any) {
      setStatus('Read failed: ' + (e?.message ?? e));
    } finally {
      NfcManager.cancelTechnologyRequest().catch(() => {});
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>NFC Link Writer</Text>
      <Text style={styles.status}>{status}</Text>
      <Text style={styles.label}>URL</Text>
      <TextInput style={styles.input} value={url} onChangeText={setUrl} autoCapitalize="none" />
      <TouchableOpacity style={styles.button} onPress={writeTag} disabled={!supported}>
        <Text style={styles.buttonText}>Write to Tag</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.button} onPress={readTag} disabled={!supported}>
        <Text style={styles.buttonText}>Read Tag</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, justifyContent: 'center', backgroundColor: '#fff' },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 12, textAlign: 'center' },
  status: { marginBottom: 24, textAlign: 'center', color: '#555' },
  label: { marginBottom: 4, fontWeight: '600' },
  input: { borderWidth: 1, borderColor: '#ccc', borderRadius: 8, padding: 12, marginBottom: 16 },
  button: { backgroundColor: '#007AFF', padding: 14, borderRadius: 8, marginBottom: 12 },
  buttonText: { color: '#fff', textAlign: 'center', fontWeight: '600' },
});
