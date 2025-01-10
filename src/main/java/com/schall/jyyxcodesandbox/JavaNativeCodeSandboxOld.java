package com.schall.jyyxcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.schall.jyyxcodesandbox.model.ExecuteCodeRequest;
import com.schall.jyyxcodesandbox.model.ExecuteCodeResponse;
import com.schall.jyyxcodesandbox.model.ExecuteMessage;
import com.schall.jyyxcodesandbox.model.JudgeInfo;
import com.schall.jyyxcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JavaNativeCodeSandboxOld implements CodeSandbox {
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    //TODO 超时时间
    private  static final  long TIME_OUT = 5000L;
    private static final String SECURITY_MANAGER_PATH = "E:\\JYYX\\jyyx-code-sandbox\\src\\main\\resources\\security";
    private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";
    private static final List<String> backList = Arrays.asList("Files","exec");
    //TODO 校验代码中是否包含黑名单的命令
    private static final WordTree WORD_TREE;
    static {
        //初始化字典树
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(backList);
    }

    public static void main(String[] args) {
        JavaNativeCodeSandboxOld javaNativeCodeSandbox = new JavaNativeCodeSandboxOld();
        ExecuteCodeRequest ExecuteCodeRequest = new ExecuteCodeRequest();
        ExecuteCodeRequest.setInputList(Arrays.asList("1 2","1 3"));
        //TODO 正常的代码
        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java",StandardCharsets.UTF_8);
        //TODO 测试用户提交恶意代码情况
        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/SleepError.java",StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/MemoryError.java",StandardCharsets.UTF_8);
        ///String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/ReadFileError.java",StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/WriteFileError.java",StandardCharsets.UTF_8);
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/RunFileError.java",StandardCharsets.UTF_8);
        ExecuteCodeRequest.setCode(code);
        ExecuteCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(ExecuteCodeRequest);
        System.out.println(executeCodeResponse);
    }
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest ExecuteCodeRequest) {
//        System.setSecurityManager(new DenySecurityManager());
        List<String> inputList = ExecuteCodeRequest.getInputList();
        String code = ExecuteCodeRequest.getCode();
        String language = ExecuteCodeRequest.getLanguage();

        //校验代码中是否包含黑名单中的命令
//        FoundWord foundWord = WORD_TREE.matchWord(code);
//        if (foundWord != null)
//        {
//            System.out.println("包含禁止词" + foundWord.getFoundWord());
//            return  null;
//        }

        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        //TODO 判断全局目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)){
            FileUtil.mkdir(globalCodePathName);
        }
        //把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code,userCodePath,StandardCharsets.UTF_8);
        //1.把用户的代码保存为文件
        //2.执行代码，得到输出结果
        //3.收集整理输出结果
        //4.文件清理
        //5.错误处理，提升程序健壮性


        //TODO 2.编译代码，得到class文件
        String compileCmd = String.format("javac -encoding utf-8 %s",userCodeFile.getAbsoluteFile());
        try {
            Process comileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(comileProcess,"编译");
            System.out.println(executeMessage);
        }catch (Exception e){
            return getErrorResponse(e);
        }
        //2.执行代码，得到输出结果
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList)
        {
            //String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s",userCodeParentPath,inputArgs);
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s",
                    userCodeParentPath,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS_NAME,inputArgs);
            try {

                Process runProcess = Runtime.getRuntime().exec(runCmd);
                //超时控制
//                new Thread(() -> {
//                    try {
//                        Thread.sleep(TIME_OUT);
//                        System.out.println("超时了，中断");
//                        runProcess.destroy();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess,"运行");
                //ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess,"运行",inputArgs);
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            }catch (Exception e)
            {
              return getErrorResponse(e);
            }
        }

        //3.收集整理输出结果
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        //TODO 取最大值，便于判断是否超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessageList)
        {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(executeMessage.getErrorMessage()))
            {
                executeCodeResponse.setMessage(errorMessage);
                //TODO 执行中存在错误
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null)
            {
                maxTime = Math.max(maxTime,time);
            }
        }
        if (outputList.size() == executeMessageList.size())
        {
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();

        judgeInfo.setMemory(maxTime);
        //借助第三方库来获取内存占用，非常麻烦，暂时不实现
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        //4.文件清理
        if(userCodeFile.getParentFile()!=null)
        {
           boolean del =  FileUtil.del(userCodeParentPath);
           System.out.println("删除"+(del ? "成功" : "失败"));
        }



        return executeCodeResponse;
    }

    /**
     * 获取错误响应
     * @param e
     * @return
     */
    private  ExecuteCodeResponse getErrorResponse(Throwable e)
    {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        //TODO  表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return  executeCodeResponse;
        
    }
}
