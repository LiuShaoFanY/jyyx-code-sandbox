package com.schall.jyyxcodesandbox;

import com.schall.jyyxcodesandbox.model.ExecuteCodeRequest;
import com.schall.jyyxcodesandbox.model.ExecuteCodeResponse;

/**
 * TODO 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * TODO 执行代码
     * @param ExecuteCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest ExecuteCodeRequest);

}
