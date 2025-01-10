package com.schall.jyyxcodesandbox.model;

import lombok.Data;

/**
 * 进程执行信息
 */
@Data
public class ExecuteMessage {

    private Integer exitValue;

    private String message;

    private  String errorMessage;

    private Long time;

    private Long memory;

    @Override
    public String toString() {
        return String.format(
                "ExecuteMessage(exitValue=%d, message=%s, errorMessage=%s, time=%d, memory=%s)",
                exitValue != null ? exitValue : 0,
                message != null ? message : "",
                errorMessage != null ? errorMessage : "null",
                time != null ? time : 0,
                memory != null ? memory : "null"
        );
    }
}
