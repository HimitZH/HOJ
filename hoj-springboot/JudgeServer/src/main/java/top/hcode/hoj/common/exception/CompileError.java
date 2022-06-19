package top.hcode.hoj.common.exception;

import lombok.Data;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/31 00:16
 * @Description:
 */
@Data
public class CompileError extends Exception {
    private String message;
    private String stdout;
    private String stderr;

    public CompileError(String message, String stdout, String stderr) {
        super(message);
        this.message = message;
        this.stdout = stdout;
        this.stderr = stderr;
    }
}