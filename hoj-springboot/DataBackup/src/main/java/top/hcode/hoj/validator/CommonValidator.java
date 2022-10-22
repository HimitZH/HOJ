package top.hcode.hoj.validator;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;

/**
 * @Author Himit_ZH
 * @Date 2022/10/22
 */
@Component
public class CommonValidator {

    public void validateContent(String content, String item) throws StatusFailException {
        if (StrUtil.isBlank(content)) {
            throw new StatusFailException(item + "的内容不能为空！");
        }
        if (content.length() > 65535) {
            throw new StatusFailException(item + "的内容长度超过限制，请重新编辑！");
        }
    }

    public void validateContent(String content, String item, int length) throws StatusFailException {
        if (StrUtil.isBlank(content)) {
            throw new StatusFailException(item + "的内容不能为空！");
        }
        if (content.length() > length) {
            throw new StatusFailException(item + "的内容长度超过限制，请重新编辑！");
        }
    }



    public void validateNotEmpty(Object value, String item) throws StatusFailException {
        if (value == null) {
            throw new StatusFailException(item + "不能为空");
        }
        if (value instanceof String){
            if (StrUtil.isBlank((String)value)){
                throw new StatusFailException(item + "不能为空");
            }
        }
    }
}
