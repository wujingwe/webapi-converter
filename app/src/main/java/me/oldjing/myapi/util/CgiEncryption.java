package me.oldjing.myapi.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CgiEncryption {

	private final String mCipherToken;
	private final String mCipherText;
	private final int mTimeBias;

	private PublicKey mPubKey = null;

	public CgiEncryption(String publicKey, String cipherToken, String cipherText, int timeBias) {
		final byte bytePKCS[] = Base64.decode(publicKey, Base64.DEFAULT);
		final X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(bytePKCS);

		try {
			mPubKey = KeyFactory.getInstance("RSA").generatePublic(pubKeySpec);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		mCipherToken = cipherToken;
		mCipherText = cipherText;
		mTimeBias = timeBias;
	}

	public Map<String, String> encryptFromParams(Map<String, String> params) {
		String cipherString = encryptFromString(format(params, "UTF-8"));

		if (null == cipherString) {
			return params;
		}

		Map<String, String> result = new HashMap<>();
		result.put(mCipherText, cipherString);
		return result;
	}

	private String format(final Map<String, String> params, final String encoding) {
		final StringBuilder result = new StringBuilder();
		for (final Entry<String, String> entry : params.entrySet()) {
			final String encodedName = encode(entry.getKey(), encoding);
			final String value = entry.getValue();
			final String encodedValue = value != null ? encode(value, encoding) : "";
			if (result.length() > 0)
				result.append("&");
			result.append(encodedName);
			result.append("=");
			result.append(encodedValue);
		}
		return result.toString();
	}

	private String encode(final String content, final String encoding) {
		try {
			return URLEncoder.encode(content, encoding);
		} catch (UnsupportedEncodingException problem) {
			throw new IllegalArgumentException(problem);
		}
	}

	private String encryptFromString(String plainText) {
		byte[] cipherData;
		int timeToken = (int) (System.currentTimeMillis() / 1000) + mTimeBias;

		if (null == mCipherText || null == mCipherToken) {
			return null;
		}
		plainText = mCipherToken + "=" + Integer.toString(timeToken) + "&" + plainText;
		cipherData = encryptFromByte(plainText.getBytes());
		if (null == cipherData) {
			return null;
		}

		return Base64.encodeToString(cipherData, Base64.DEFAULT);
	}

	private byte[] encryptFromByte(byte[] plainByte) {
		byte[] result = null;

		try {
			Cipher cipher = Cipher.getInstance("RSA/CBC/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, mPubKey);
			result = cipher.doFinal(plainByte);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
				IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return result;
	}
}
