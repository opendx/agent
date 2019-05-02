package com.fgnb;

/**
 * Created by jiangyitao.
 */
public class JavaCodeCompiler {

    public static Class compile(String fullClassName, String code) throws Exception {
        return DynamicEngine.getInstance().javaCodeToClass(fullClassName, code);
    }
}
