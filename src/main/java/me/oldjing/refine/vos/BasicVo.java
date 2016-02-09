package me.oldjing.refine.vos;

public class BasicVo {
    public boolean success;
    public ErrorCodeVo error;

    public BasicVo() {
    }

    public BasicVo(boolean success, ErrorCodeVo error) {
        this.success = success;
        this.error = error;
    }

    public static class ErrorCodeVo {
        public int code;

        public ErrorCodeVo(int code) {
            this.code = code;
        }
    }
}
