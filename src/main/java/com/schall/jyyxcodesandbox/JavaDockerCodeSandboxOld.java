package com.schall.jyyxcodesandbox;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.schall.jyyxcodesandbox.model.ExecuteCodeRequest;
import com.schall.jyyxcodesandbox.model.ExecuteCodeResponse;
import com.schall.jyyxcodesandbox.model.ExecuteMessage;
import com.schall.jyyxcodesandbox.model.JudgeInfo;
import com.schall.jyyxcodesandbox.utils.ProcessUtils;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
@Component
public class JavaDockerCodeSandboxOld implements CodeSandbox {
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    //TODO 超时时间
    private  static final  long TIME_OUT = 5000L;
    private static final String SECURITY_MANAGER_PATH = "E:\\JYYX\\jyyx-code-sandbox\\src\\main\\resources\\security";
    private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";
    private static final Boolean FIRST_INIT = true;

    public static void main(String[] args) {
        JavaDockerCodeSandboxOld javaNativeCodeSandbox = new JavaDockerCodeSandboxOld();
        ExecuteCodeRequest ExecuteCodeRequest = new ExecuteCodeRequest();
        ExecuteCodeRequest.setInputList(Arrays.asList("1 2","1 3"));
        //TODO 正常的代码
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java",StandardCharsets.UTF_8);
        //TODO 测试用户提交恶意代码情况
        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/SleepError.java",StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/MemoryError.java",StandardCharsets.UTF_8);
        ///String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/ReadFileError.java",StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/WriteFileError.java",StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/RunFileError.java",StandardCharsets.UTF_8);
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

        //TODO 获取默认的 Docker Client
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();

        //TODO 拉取镜像
        String image = "openjdk:8-alpine";
        if (FIRST_INIT)
        {

            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像:" + item.getStatus());
                    super.onNext(item);


                }
            };
            try {
                pullImageCmd
                        .exec(pullImageResultCallback)
                        .awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
        }

        System.out.println("下载完成");
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        //TODO 创建容器时，可以指定文件路径映射
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(100 * 1000 * 1000L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        //hostConfig.withSecurityOpts(Arrays.asList("seccomp = 安全管理配置字符串"));

        hostConfig.setBinds(new Bind(userCodeParentPath,new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withReadonlyRootfs(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();

//        //TODO 查看容器状态
//        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
//        List<Container> containerList = listContainersCmd.withShowAll(true).exec();
//        for (Container container : containerList){
//            System.out.println(container);
//        }

        //TODO 启动容器
        dockerClient.startContainerCmd(containerId).exec();
        //TODO 执行命令获取返回结果
        List<ExecuteMessage> executeMessagesList = new ArrayList<>();
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            String[] inputArgsArray = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append( new String[] {"java","-cp","/app","Main"}, inputArgsArray);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            System.out.println("创建执行命令:" + execCreateCmdResponse);


            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] message = {null};
            final String[] errorMessage = {null};
            long time = 0L;
            //TODO 判断是否超时
            final  boolean[] timeout = {true};
            String execId = execCreateCmdResponse.getId();
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback()
            {
                @Override
                public void onComplete(){
                    //TODO 如果执行完成，则表示没超时
                    timeout[0] = false;
                    super.onComplete();
                }
                @Override
                public void onNext(Frame frame)
                {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)){
                        errorMessage[0] = new String(frame.getPayload());
                        System.out.println("输出错误结果:" + errorMessage[0]);
                    }else
                    {
                        message[0] = new String(frame.getPayload());
                        System.out.println("输出结果:" + message[0]);
                    }
                    super.onNext(frame);
                }
            };
            final long[] maxMemory = {0L};
            //TODO 获取占用内存
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用" + statistics.getMemoryStats().getUsage());
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(),maxMemory[0]);
                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }

                @Override
                public void close() throws IOException {

                }
            });
            statsCmd.exec(statisticsResultCallback);

            try {
                stopWatch.start();
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
                statsCmd.close();
            } catch (InterruptedException e) {
                System.out.println("程序执行异常");
                throw new RuntimeException(e);
            }

            executeMessage.setMessage(message[0]);
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessage.setTime(time);
            executeMessage.setMemory(maxMemory[0]);
            executeMessagesList.add(executeMessage);
        }







        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();

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