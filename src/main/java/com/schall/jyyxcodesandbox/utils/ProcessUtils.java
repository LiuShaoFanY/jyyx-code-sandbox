package com.schall.jyyxcodesandbox.utils;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import com.schall.jyyxcodesandbox.model.ExecuteMessage;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO 进程工具类
 */
public class ProcessUtils {
    /**
     * TODO 执行进程，并且获取信息
     *
     * @param opName
     * @param runProcess
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            //TODO 等待程序执行，获得错误码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            //TODO 正常退出
            if (exitValue == 0) {
                System.out.println(opName + "");
                //TODO 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                List<String> outputStrList = new ArrayList<>();
                //TODO 逐行获取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList,"\n"));
                //TODO 整体输出

            } else {
                //TODO 异常退出
                System.out.println(opName + "失败,错误码：" + exitValue);
                //TODO 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                List<String> outputStrList = new ArrayList<>();
                //TODO 逐行获取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList,"\n"));
                //TODO 分批获取进程的错误输出
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                List<String> errorOutputStrList = new ArrayList<>();
                //TODO 逐行获取
                String errorCompileOutputLine;
                while ((errorCompileOutputLine = bufferedReader.readLine()) != null) {
                    errorOutputStrList.add(errorCompileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(errorOutputStrList,"\n"));
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());

        } catch (Exception e) {
            e.printStackTrace();

        }
        return executeMessage;
    }

    /**
     * TODO 执行进程，并且获取信息
     *
     * @param runProcess
     * @param args
     * @param inputArgs
     * @return
     */
//    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String args, String inputArgs) {
//        ExecuteMessage executeMessage = new ExecuteMessage();
//        //TODO 等待程序执行，获得错误码
//        try {
//            //TODO 从控制台输入程序
//            OutputStream outputStream = runProcess.getOutputStream();
//
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
//            String[] s = args.split("");
//            String join = StrUtil.join("\n" , s) + "\n";
//            outputStreamWriter.write(join);
//            //outputStreamWriter.write("1 2");
//            //TODO 相当于按回车，执行输入的发送
//            outputStreamWriter.flush();
//            //TODO 分批获取进程的正常输出
//            InputStream inputStream = runProcess.getInputStream();
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//            StringBuilder compileOutputStringBuilder = new StringBuilder();
//
//            //TODO 逐行获取
//            String compileOutputLine;
//            while ((compileOutputLine = bufferedReader.readLine()) != null) {
//                compileOutputStringBuilder.append(compileOutputLine);
//            }
//            executeMessage.setMessage(compileOutputStringBuilder.toString());
//            //TODO 记得资源的释放，否则会卡死
//            outputStreamWriter.close();
//            outputStream.close();
//            inputStream.close();
//            runProcess.destroy();
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//
//        return executeMessage;
//    }
}

