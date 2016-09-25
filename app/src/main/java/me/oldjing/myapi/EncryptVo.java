package me.oldjing.myapi;

import com.squareup.moshi.Json;

public class EncryptVo {
	public CipherDataVo data;
	public Object error;

	public static class CipherDataVo {
		@Json(name = "cipherkey") public String cipherKey;
		@Json(name = "ciphertoken") public String cipherToken;
		@Json(name = "public_key") public String publicKey;
		public int server_time;
	}
}
