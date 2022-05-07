package top.hcode.hoj.util;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;

import javax.script.*;
import java.util.List;

@Slf4j(topic = "hoj")
public class CodeForcesUtils {
    private static String RCPC;

    public static String getRCPC() {
        return RCPC;
    }

    public static void updateRCPC(List<String> list) {

        ScriptEngine se = new ScriptEngineManager().getEngineByName("javascript");
        Bindings bindings = se.createBindings();
        bindings.put("string", 4);
        se.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        String file = ResourceUtil.readUtf8Str("CodeForcesAES.js");
        try {
            se.eval(file);
            // 是否可调用
            if (se instanceof Invocable) {
                Invocable in = (Invocable) se;
                RCPC = (String) in.invokeFunction("getRCPC", list.get(0), list.get(1), list.get(2));
            }
        } catch (ScriptException e) {
            log.error("CodeForcesUtils.updateRCPC throw ScriptException", e);
        } catch (NoSuchMethodException e) {
            log.error("CodeForcesUtils.updateRCPC throw NoSuchMethodException", e);
        }
    }
}
