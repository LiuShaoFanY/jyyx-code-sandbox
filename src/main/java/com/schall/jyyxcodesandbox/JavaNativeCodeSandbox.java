package com.schall.jyyxcodesandbox;

import com.schall.jyyxcodesandbox.model.ExecuteCodeRequest;
import com.schall.jyyxcodesandbox.model.ExecuteCodeResponse;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * TODO Java 原生代码沙箱实现(直接复用模板方法)
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest ExecuteCodeRequest) {
        return super.executeCode(ExecuteCodeRequest);
    }
}
