package top.hcode.common.exception;


import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.hcode.common.result.CommonResult;


import javax.mail.MessagingException;
import java.io.IOException;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理shiro的异常
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ShiroException.class)
    public CommonResult handle401(ShiroException e) {
        return CommonResult.errorResponse( e.getCause().toString(),CommonResult.STATUS_ACCESS_DENIED);
    }


    /**
     * 处理Assert的异常 断言的异常！
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public CommonResult handler(IllegalArgumentException e) throws IOException {
        log.error("Assert异常:-------------->{}",e.getMessage());
        return CommonResult.errorResponse(e.getMessage(),CommonResult.STATUS_FAIL);
    }

    /**
     * @Validated 校验错误异常处理
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public CommonResult handler(MethodArgumentNotValidException e) throws IOException {
        log.error("实体校验异常:-------------->",e);
        BindingResult bindingResult = e.getBindingResult();
        ObjectError objectError = bindingResult.getAllErrors().stream().findFirst().get();
        return CommonResult.errorResponse(objectError.getDefaultMessage(),CommonResult.STATUS_FAIL);
    }
    /**
     * 处理运行时出现的异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = RuntimeException.class)
    public CommonResult handler(RuntimeException e) throws IOException {
        log.error("运行时异常:-------------->",e);
        return CommonResult.errorResponse(e.getMessage(),CommonResult.STATUS_ERROR);
    }

    /**
    *  处理邮件发送出现的异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = MessagingException.class)
    public CommonResult handler(MessagingException e) throws Exception {
        log.error("邮箱系统异常:-------------->",e);
        return CommonResult.errorResponse(e.getMessage(),CommonResult.STATUS_ERROR);
    }
}