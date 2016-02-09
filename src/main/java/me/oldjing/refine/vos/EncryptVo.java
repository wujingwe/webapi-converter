package me.oldjing.refine.vos;

public class EncryptVo extends BasicVo {
    public final CipherVo data;

    public EncryptVo(CipherVo data) {
        this.data = data;
    }

    public static class CipherVo {
        public final String cipherkey;
        public final String ciphertoken;
        public final String public_key;
        public final int server_time;

        public CipherVo(String cipherkey, String ciphertoken, String public_key, int server_time) {
            this.cipherkey = cipherkey;
            this.ciphertoken = ciphertoken;
            this.public_key = public_key;
            this.server_time = server_time;
        }
    }
}
