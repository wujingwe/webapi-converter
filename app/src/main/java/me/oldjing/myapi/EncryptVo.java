package me.oldjing.myapi;

import com.google.gson.annotations.SerializedName;

public class EncryptVo {
	public CipherDataVo data;

	public class CipherDataVo {
		@SerializedName("cipherkey") public String cipherKey;
		@SerializedName("ciphertoken") public String cipherToken;
		@SerializedName("public_key") public String publicKey;
		public int server_time;
	}
}
