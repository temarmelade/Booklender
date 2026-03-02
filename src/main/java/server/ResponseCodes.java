package server;

public enum ResponseCodes {
    OK(200),
    NOT_FOUND(404),
    REDIRECT(303);
    private final int code;
    ResponseCodes(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}
