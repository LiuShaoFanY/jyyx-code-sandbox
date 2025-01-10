package com.schall.jyyxcodesandbox;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.schall.jyyxcodesandbox.model.ExecuteCodeRequest;
import com.schall.jyyxcodesandbox.model.ExecuteCodeResponse;
import com.schall.jyyxcodesandbox.model.ExecuteMessage;
import com.schall.jyyxcodesandbox.model.JudgeInfo;
import com.schall.jyyxcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    //TODO 超时时间
    private  static final  long TIME_OUT = 5000L;
    /**
     * TODO 抽象流程模板方法
     * @param ExecuteCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest ExecuteCodeRequest) {
//        System.setSecurityManager(new DenySecurityManager());
        List<String> inputList = ExecuteCodeRequest.getInputList();
        String code = ExecuteCodeRequest.getCode();
        String language = ExecuteCodeRequest.getLanguage();

        //1.把用户的代码保存为文件
        File userCodeFile = saveCodeToFile(code);

        //2.执行代码，得到输出结果
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
        System.out.println(compileFileExecuteMessage);

        //3.执行代码获得输出结果
        List<ExecuteMessage> executeMessageList = runFile(userCodeFile,inputList);

        //4.收集整理输出结果
        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessageList);

//        //5.错误处理，提升程序健壮性
//        deleteFile(userCodeFile);
//
//        boolean b = deleteFile(userCodeFile);
//        if (!b){
//            log.error("deleteFile error ,userCodeFilePath = {}",userCodeFile.getAbsolutePath());
//        }
        return outputResponse;
    }
    /**
     * TODO 1.把用户的代码保存为文件
     * @param code 用户代码
     * @return
     */
    public File saveCodeToFile(String code){
        //TODO 1.把用户的代码保存为文件
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
        return userCodeFile;
    }
    /**
     * TODO 2.编译代码
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage  compileFile(File userCodeFile){
        String compileCmd = String.format("javac -encoding utf-8 %s",userCodeFile.getAbsoluteFile());
        try {
            Process comileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(comileProcess,"编译");
            if (executeMessage.getExitValue() != 0)
            {
                throw new RuntimeException("编译错误");
            }
            System.out.println(executeMessage);
            return executeMessage;
        }catch (Exception e){
//            return getErrorResponse(e);
            throw new RuntimeException(e);
        }
    }
    /**
     * TODO 3.执行文件，获得执行结果列表
     * @param userCodeFile
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList)
        {
            //String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s",userCodeParentPath,inputArgs);
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath,inputArgs);
            //TODO 去掉安全处理 %s -Djava.security.manager=%s
            try {

                Process runProcess = Runtime.getRuntime().exec(runCmd);
                //TODO 超时控制
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时了，中断");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess,"运行");
                //ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess,"运行",inputArgs);
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            }catch (Exception e)
            {
                throw new RuntimeException("执行错误",e);
            }
        }
        return executeMessageList;
    }

    /**
     *TODO 4.获取执行结果
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList){
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

        return executeCodeResponse;
    }

//    /**
//     * TODO 5.删除文件
//     * @param userCodeFile
//     * @return
//     */
//    public boolean deleteFile(File userCodeFile){
//        if(userCodeFile.getParentFile()!=null)
//        {
//            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
//            boolean del =  FileUtil.del(userCodeParentPath);
//            System.out.println("删除"+(del ? "成功" : "失败"));
//            return del;
//        }
//        return true;
//
//    }




    /**
     * TODO 6.获取错误响应
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

