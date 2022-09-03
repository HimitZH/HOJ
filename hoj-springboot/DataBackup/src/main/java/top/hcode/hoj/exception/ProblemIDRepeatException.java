package top.hcode.hoj.exception;

/**
 * @Author Himit_ZH
 * @Date 2022/9/3
 */
public class ProblemIDRepeatException extends RuntimeException{
    public ProblemIDRepeatException() {
        super();
    }

    public ProblemIDRepeatException(String message) {
        super(message);
    }

    public ProblemIDRepeatException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProblemIDRepeatException(Throwable cause) {
        super(cause);
    }

    protected ProblemIDRepeatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
