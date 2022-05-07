package top.hcode.hoj.util;

import cn.hutool.core.io.resource.ResourceUtil;

import javax.script.*;
import java.util.List;

public class CodeForcesUtils {
    private static String RCPC;


    public static String getRCPC() {
        return RCPC;
    }

    public static void updateRCPC(List<String> list) throws ScriptException, NoSuchMethodException {

        ScriptEngine se = new ScriptEngineManager().getEngineByName("javascript");
        Bindings bindings = se.createBindings();
        bindings.put("string", 4);
        se.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        String file = ResourceUtil.readUtf8Str("CodeForcesAES.js");
        se.eval(file);
        // 是否可调用
        if (se instanceof Invocable) {
            Invocable in = (Invocable) se;
            RCPC = (String) in.invokeFunction("getRCPC", list.get( 0), list.get(1), list.get(2));

        }
    }
}
