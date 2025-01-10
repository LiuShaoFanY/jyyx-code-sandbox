package com.schall.jyyxcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

    private List<String> outputList;

    /**
     * TODO 接口信息
     */
    private String message;

    /**
     * TODO 执行状态
     */
    private Integer status;

    /**
     * TODO 执行状态
     */
    private JudgeInfo judgeInfo;



}
