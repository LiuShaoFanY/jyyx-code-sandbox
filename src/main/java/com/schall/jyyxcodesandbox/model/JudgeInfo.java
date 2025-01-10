package com.schall.jyyxcodesandbox.model;

import lombok.Data;

/**
 * 判题信息
 */
@Data
public class JudgeInfo {
    
    /**
     * 程序执行信息(ms)
     */
    private String massage;

    /**
     * 消耗内存
     */
    private Long memory;

    /**
     * 消耗时间(kb)
     */
    private Long time;
}
