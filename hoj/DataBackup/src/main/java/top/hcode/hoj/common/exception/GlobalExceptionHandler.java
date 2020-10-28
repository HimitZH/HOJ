package top.hcode.hoj.common.exception;


import com.google.protobuf.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.hcode.hoj.common.result.CommonResult;
import javax.mail.MessagingException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.io.IOException;
import java.util.Set;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 401 -UnAuthorized 处理shiro的异常
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ShiroException.class)
    public CommonResult handle401(ShiroException e) {
        log.error("shiro异常-------------->{}", e.getMessage());
        return CommonResult.errorResponse("对不起，您无权限进行此操作，请先登录进行授权认证", CommonResult.STATUS_ACCESS_DENIED);
    }

    /**
     * 400 - Bad Request 处理Assert的异常 断言的异常！
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public CommonResult handler(IllegalArgumentException e) throws IOException {
        log.error("Assert异常-------------->{}", e.getMessage());
        return CommonResult.errorResponse(e.getMessage(), CommonResult.STATUS_FAIL);
    }

    /**
     * 400 - Bad Request @Validated 校验错误异常处理
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public CommonResult handler(MethodArgumentNotValidException e) throws IOException {
        log.error("实体校验异常-------------->{}", e);
        BindingResult bindingResult = e.getBindingResult();
        ObjectError objectError = bindingResult.getAllErrors().stream().findFirst().get();
        return CommonResult.errorResponse(objectError.getDefaultMessage(), CommonResult.STATUS_FAIL);
    }


    /**
     * 400 - Bad Request 处理缺少请求参数
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public CommonResult handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.error("缺少请求参数-------------->{}", e.getMessage());
        return CommonResult.errorResponse("缺少必要的请求参数："+e.getMessage(),CommonResult.STATUS_FAIL);
    }

    /**
     * 400 - Bad Request 参数解析失败
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResult handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.error("参数解析失败-------------->{}", e.getMessage());
        return CommonResult.errorResponse("解析参数格式失败",CommonResult.STATUS_FAIL);
    }


    /**
     * 400 - Bad Request 参数绑定失败
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public CommonResult handleBindException(BindException e) {
        log.error("参数绑定失败-------------->{}", e);
        BindingResult result = e.getBindingResult();
        FieldError error = result.getFieldError();
        String field = error.getField();
        String code = error.getDefaultMessage();
        String message = String.format("%s:%s", field, code);
        return CommonResult.errorResponse("参数绑定失败",CommonResult.STATUS_FAIL);
    }

    /**
     * 400 - Bad Request 参数验证失败
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult handleServiceException(ConstraintViolationException e) {
        log.error("参数验证失败-------------->{}", e.getMessage());
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        ConstraintViolation<?> violation = violations.iterator().next();
        String message = violation.getMessage();
        return CommonResult.errorResponse("[参数验证失败]parameter:" + message,CommonResult.STATUS_FAIL);
    }

    /**
     * 400 - Bad Request 实体校验失败
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public CommonResult handleValidationException(ValidationException e) {
        log.error("实体校验失败-------------->{}", e.getMessage());
        return CommonResult.errorResponse("实体校验失败,请求参数不对",CommonResult.STATUS_FAIL);
    }

    /**
     * 405 - Method Not Allowed 不支持当前请求方法
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CommonResult handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.error("不支持当前请求方法-------------->{}", e.getMessage());
        return CommonResult.errorResponse("不支持当前请求方法",405);
    }

    /**
     * 415 - Unsupported Media Type 不支持当前媒体类型
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public CommonResult handleHttpMediaTypeNotSupportedException(Exception e) {
        log.error("不支持当前媒体类型-------------->{}", e.getMessage());
        return CommonResult.errorResponse("不支持当前媒体类型",415);
    }


    /**
     * 500 - Internal Server Error 处理邮件发送出现的异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = MessagingException.class)
    public CommonResult handler(MessagingException e) throws Exception {
        log.error("邮箱系统异常-------------->{}", e.getMessage());
        return CommonResult.errorResponse(e.getMessage(), CommonResult.STATUS_ERROR);
    }

    /**
     * 500 - Internal Server Error 业务逻辑异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ServiceException.class)
    public CommonResult handleServiceException(ServiceException e) {
        log.error("业务逻辑异常-------------->{}", e.getMessage());
        return CommonResult.errorResponse("业务逻辑异常：" + e.getMessage(),CommonResult.STATUS_ERROR);
    }

    /**
     * 500 - Internal Server Error 系统通用异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public CommonResult handleException(Exception e) {
        log.error("系统通用异常-------------->{}", e.getMessage());
        return CommonResult.errorResponse("系统通用异常：" + e.getMessage(),CommonResult.STATUS_ERROR);
    }

    /**
     * 500 - Internal Server Error 操作数据库出现异常:名称重复，外键关联
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public CommonResult handleException(DataIntegrityViolationException e) {
        log.error("操作数据库出现异常-------------->{}", e.getMessage());
        return CommonResult.errorResponse("操作数据库出现异常：字段重复、有外键关联等",CommonResult.STATUS_ERROR);
    }
}