package top.hcode.hoj.exception;

/**
 * @Author Himit_ZH
 * @Date 2022/5/9
 */
public class AccessException extends Exception{
    public AccessException() {
        super();
    }

    public AccessException(String message) {
        super(message);
    }

    public AccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessException(Throwable cause) {
        super(cause);
    }

    protected AccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
