package top.hcode.hoj.common.exception;


import com.google.protobuf.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.Set;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 401 -UnAuthorized 处理AuthenticationException,token相关异常 即是认证出错 可能无法处理！
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = AuthenticationException.class)
    public CommonResult handleAuthenticationException(AuthenticationException e,
                                                      HttpServletRequest httpRequest,
                                                      HttpServletResponse httpResponse) {
        httpResponse.setHeader("Url-Type", httpRequest.getHeader("Url-Type")); // 为了前端能区别请求来源
        return CommonResult.errorResponse(e.getMessage(), CommonResult.STATUS_ACCESS_DENIED);
    }

    /**
     * 401 -UnAuthorized UnauthenticatedException,token相关异常 即是认证出错 可能无法处理！
     *  没有登录（没有token），访问有@RequiresAuthentication的请求路径会报这个异常
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = UnauthenticatedException.class)
    public CommonResult handleUnauthenticatedException(UnauthenticatedException e,
                                                      HttpServletRequest httpRequest,
                                                      HttpServletResponse httpResponse) {
        httpResponse.setHeader("Url-Type", httpRequest.getHeader("Url-Type")); // 为了前端能区别请求来源
        return CommonResult.errorResponse("请您先登录！", CommonResult.STATUS_ACCESS_DENIED);
    }

    /**
     * 403 -FORBIDDEN AuthorizationException异常 即是授权认证出错 可能无法处理！
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = AuthorizationException.class)
    public CommonResult handleAuthenticationException(AuthorizationException e,
                                                      HttpServletRequest httpRequest,
                                                      HttpServletResponse httpResponse) {
        httpResponse.setHeader("Url-Type", httpRequest.getHeader("Url-Type")); // 为了前端能区别请求来源
        return CommonResult.errorResponse("对不起，您无权限进行此操作！", CommonResult.STATUS_FORBIDDEN);
    }

    /**
     * 403 -FORBIDDEN 处理shiro的异常 无法处理！ 未能走到controller层
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = ShiroException.class)
    public CommonResult handleShiroException(ShiroException e,
                                             HttpServletRequest httpRequest,
                                             HttpServletResponse httpResponse) {
        httpResponse.setHeader("Url-Type", httpRequest.getHeader("Url-Type")); // 为了前端能区别请求来源
        return CommonResult.errorResponse("对不起，您无权限进行此操作，请先登录进行授权认证", CommonResult.STATUS_FORBIDDEN);
    }


    /**
     * 400 - Bad Request 处理Assert的异常 断言的异常！
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public CommonResult handler(IllegalArgumentException e) {
        return CommonResult.errorResponse(e.getMessage(), CommonResult.STATUS_FAIL);
    }

    /**
     * 400 - Bad Request @Validated 校验错误异常处理
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public CommonResult handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) throws IOException {
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
        return CommonResult.errorResponse("缺少必要的请求参数：" + e.getMessage(), CommonResult.STATUS_FAIL);
    }

    /**
     * 400 - Bad Request 参数解析失败
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResult handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        return CommonResult.errorResponse("解析参数格式失败", CommonResult.STATUS_FAIL);
    }


    /**
     * 400 - Bad Request 参数绑定失败
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public CommonResult handleBindException(BindException e) {
        BindingResult result = e.getBindingResult();
        FieldError error = result.getFieldError();
        String field = error.getField();
        String code = error.getDefaultMessage();
        String message = String.format("%s:%s", field, code);
        return CommonResult.errorResponse(message, CommonResult.STATUS_FAIL);
    }

    /**
     * 400 - Bad Request 参数验证失败
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult handleServiceException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        ConstraintViolation<?> violation = violations.iterator().next();
        String message = violation.getMessage();
        return CommonResult.errorResponse("[参数验证失败]parameter:" + message, CommonResult.STATUS_FAIL);
    }

    /**
     * 400 - Bad Request 实体校验失败
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public CommonResult handleValidationException(ValidationException e) {
        return CommonResult.errorResponse("实体校验失败,请求参数不对", CommonResult.STATUS_FAIL);
    }

    /**
     * 405 - Method Not Allowed 不支持当前请求方法
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CommonResult handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        return CommonResult.errorResponse("不支持当前请求方法", 405);
    }

    /**
     * 415 - Unsupported Media Type 不支持当前媒体类型
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public CommonResult handleHttpMediaTypeNotSupportedException(Exception e) {
        return CommonResult.errorResponse("不支持当前媒体类型", 415);
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
        return CommonResult.errorResponse("业务逻辑异常:" + e.getCause(), CommonResult.STATUS_ERROR);
    }

    /**
     * 500 - Internal Server Error 操作数据库出现异常:名称重复，外键关联
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public CommonResult handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("操作数据库出现异常-------------->{}", e.getMessage());
        return CommonResult.errorResponse("操作数据库出现异常：字段重复、有外键关联等", CommonResult.STATUS_ERROR);
    }


    /**
     * 500 - Internal Server Error 操作数据库出现异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SQLException.class)
    public CommonResult handleSQLException(SQLException e) {
        log.error("操作数据库出现异常-------------->{}", e.getMessage());
        return CommonResult.errorResponse("操作失败！错误提示："+e.getMessage(), CommonResult.STATUS_ERROR);
    }

    /**
     * 500 - Internal Server Error 批量操作数据库出现异常:名称重复，外键关联
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(PersistenceException.class)
    public CommonResult handleBatchUpdateException(PersistenceException e) {
        log.error("操作数据库出现异常-------------->{}", e.getMessage());
        return CommonResult.errorResponse("操作失败！请检查数据是否准确！可能原因：数据重复冲突，外键冲突！", CommonResult.STATUS_ERROR);
    }

    /**
     * 500 - Internal Server Error 系统通用异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public CommonResult handleException(Exception e) {
        log.error("系统通用异常-------------->{}", e.getMessage());
        return CommonResult.errorResponse("系统通用异常：" + e.getCause(), CommonResult.STATUS_ERROR);
    }
}
