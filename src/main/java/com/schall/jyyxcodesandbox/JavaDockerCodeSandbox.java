//package com.schall.jyyxcodesandbox;
//import cn.hutool.core.date.StopWatch;
//import cn.hutool.core.io.resource.ResourceUtil;
//import cn.hutool.core.util.ArrayUtil;
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.async.ResultCallback;
//import com.github.dockerjava.api.command.*;
//import com.github.dockerjava.api.model.*;
//import com.github.dockerjava.core.DockerClientBuilder;
//import com.github.dockerjava.core.command.ExecStartResultCallback;
//import com.schall.jyyxcodesandbox.model.ExecuteCodeRequest;
//import com.schall.jyyxcodesandbox.model.ExecuteCodeResponse;
//import com.schall.jyyxcodesandbox.model.ExecuteMessage;
//import org.springframework.stereotype.Component;
//
//import java.io.Closeable;
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
///**
// * TODO Java代码方法模板实现
// */
//@Component
//public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate {
//    //TODO 超时时间
//    private static final long TIME_OUT = 5000L;
//
//    private static final Boolean FIRST_INIT = true;
//
//    public static void main(String[] args) {
//        JavaDockerCodeSandbox javaNativeCodeSandbox = new JavaDockerCodeSandbox();
//        ExecuteCodeRequest ExecuteCodeRequest = new ExecuteCodeRequest();
//        ExecuteCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
//        //TODO 正常的代码
//        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//        //TODO 测试用户提交恶意代码情况
//        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/SleepError.java",StandardCharsets.UTF_8);
//        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/MemoryError.java",StandardCharsets.UTF_8);
//        ///String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/ReadFileError.java",StandardCharsets.UTF_8);
//        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/WriteFileError.java",StandardCharsets.UTF_8);
//        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/unsafeCode/RunFileError.java",StandardCharsets.UTF_8);
//        ExecuteCodeRequest.setCode(code);
//        ExecuteCodeRequest.setLanguage("java");
//        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(ExecuteCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//
//    /**
//     * TODO 3.创建容器，把文件复制到容器内
//     * @param userCodeFile
//     * @param inputList
//     * @return
//     */
//    @Override
//    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
//        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
//        //TODO 获取默认的 Docker Client
//        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
//
//        //TODO 拉取镜像
//        String image = "openjdk:8-alpine";
//        if (FIRST_INIT)
//        {
//
//            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
//            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
//                @Override
//                public void onNext(PullResponseItem item) {
//                    System.out.println("下载镜像:" + item.getStatus());
//                    super.onNext(item);
//
//
//                }
//            };
//            try {
//                pullImageCmd
//                        .exec(pullImageResultCallback)
//                        .awaitCompletion();
//            } catch (InterruptedException e) {
//                System.out.println("拉取镜像异常");
//                throw new RuntimeException(e);
//            }
//        }
//
//        System.out.println("下载完成");
//        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
//        //TODO 创建容器时，可以指定文件路径映射
//        HostConfig hostConfig = new HostConfig();
//        hostConfig.withMemory(100 * 1000 * 1000L);
//        hostConfig.withMemorySwap(0L);
//        hostConfig.withCpuCount(1L);
//        //hostConfig.withSecurityOpts(Arrays.asList("seccomp = 安全管理配置字符串"));
//
//        hostConfig.setBinds(new Bind(userCodeParentPath,new Volume("/app")));
//        CreateContainerResponse createContainerResponse = containerCmd
//                .withHostConfig(hostConfig)
//                .withNetworkDisabled(true)
//                .withReadonlyRootfs(true)
//                .withAttachStdin(true)
//                .withAttachStderr(true)
//                .withAttachStdout(true)
//                .withTty(true)
//                .exec();
//        System.out.println(createContainerResponse);
//        String containerId = createContainerResponse.getId();
//
//        //TODO 启动容器
//        dockerClient.startContainerCmd(containerId).exec();
//        //TODO 执行命令获取返回结果
//        List<ExecuteMessage> executeMessagesList = new ArrayList<>();
//        for (String inputArgs : inputList) {
//            StopWatch stopWatch = new StopWatch();
//            String[] inputArgsArray = inputArgs.split(" ");
//            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
//            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
//                    .withCmd(cmdArray)
//                    .withAttachStderr(true)
//                    .withAttachStdin(true)
//                    .withAttachStdout(true)
//                    .exec();
//            System.out.println("创建执行命令:" + execCreateCmdResponse);
//
//
//            ExecuteMessage executeMessage = new ExecuteMessage();
//            final String[] message = {null};
//            final String[] errorMessage = {null};
//            long time = 0L;
//            //TODO 判断是否超时
//            final boolean[] timeout = {true};
//            String execId = execCreateCmdResponse.getId();
//            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
//                @Override
//                public void onComplete() {
//                    //TODO 如果执行完成，则表示没超时
//                    timeout[0] = false;
//                    super.onComplete();
//                }
//
//                @Override
//                public void onNext(Frame frame) {
//                    StreamType streamType = frame.getStreamType();
//                    if (StreamType.STDERR.equals(streamType)) {
//                        errorMessage[0] = new String(frame.getPayload());
//                        System.out.println("输出错误结果:" + errorMessage[0]);
//                    } else {
//                        message[0] = new String(frame.getPayload());
//                        System.out.println("输出结果:" + message[0]);
//                    }
//                    super.onNext(frame);
//                }
//            };
//            final long[] maxMemory = {0L};
//            //TODO 获取占用内存
//            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
//            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
//                @Override
//                public void onNext(Statistics statistics) {
//                    System.out.println("内存占用" + statistics.getMemoryStats().getUsage());
//                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
//                }
//
//                @Override
//                public void onStart(Closeable closeable) {
//
//                }
//
//                @Override
//                public void onError(Throwable throwable) {
//
//                }
//
//                @Override
//                public void onComplete() {
//
//                }
//
//                @Override
//                public void close() throws IOException {
//
//                }
//            });
//            statsCmd.exec(statisticsResultCallback);
//
//            try {
//                stopWatch.start();
//                dockerClient.execStartCmd(execId)
//                        .exec(execStartResultCallback)
//                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
//                stopWatch.stop();
//                time = stopWatch.getLastTaskTimeMillis();
//                statsCmd.close();
//            } catch (InterruptedException e) {
//                System.out.println("程序执行异常");
//                throw new RuntimeException(e);
//            }
//            executeMessage.setMessage(message[0]);
//            executeMessage.setErrorMessage(errorMessage[0]);
//            executeMessage.setTime(time);
//            executeMessage.setMemory(maxMemory[0]);
//            executeMessagesList.add(executeMessage);
//    }
//     return executeMessagesList;
//}
//
//
//}



//
//package com.schall.jyyxcodesandbox;
//
//import cn.hutool.core.date.StopWatch;
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.core.io.resource.ResourceUtil;
//import cn.hutool.core.util.ArrayUtil;
//import cn.hutool.core.util.StrUtil;
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.async.ResultCallback;
//import com.github.dockerjava.api.command.*;
//import com.github.dockerjava.api.model.*;
//import com.github.dockerjava.core.DefaultDockerClientConfig;
//import com.github.dockerjava.core.DockerClientConfig;
//import com.github.dockerjava.core.DockerClientImpl;
//import com.github.dockerjava.core.command.ExecStartResultCallback; // 导入 ExecStartResultCallback
//import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
//import com.github.dockerjava.transport.DockerHttpClient;
//import com.schall.jyyxcodesandbox.model.ExecuteCodeRequest;
//import com.schall.jyyxcodesandbox.model.ExecuteCodeResponse;
//import com.schall.jyyxcodesandbox.model.ExecuteMessage;
//import com.schall.jyyxcodesandbox.model.JudgeInfo;
//import org.springframework.stereotype.Component;
//
//import java.io.Closeable;
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//@Component
//public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate {
//
//    private static final long TIME_OUT = 5000L; // 超时时间
//    private static final Boolean FIRST_INIT = true; // 是否首次初始化
//
//    public static void main(String[] args) {
//        JavaDockerCodeSandbox javaDockerCodeSandbox = new JavaDockerCodeSandbox();
//        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
//        executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
//        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setLanguage("java");
//        ExecuteCodeResponse executeCodeResponse = javaDockerCodeSandbox.executeCode(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//
//    @Override
//    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
//        List<String> inputList = executeCodeRequest.getInputList();
//        String code = executeCodeRequest.getCode();
//
//        System.out.println("1. 保存用户代码到文件...");
//        File userCodeFile = saveCodeToFile(code);
//        System.out.println("用户代码已保存到: " + userCodeFile.getAbsolutePath());
//
//        // 2. 获取 Docker 客户端
//        DockerClient dockerClient = getDockerClient();
//        System.out.println("Docker 客户端已初始化。");
//
//        // 3. 拉取镜像并创建容器
//        String image = "openjdk:8-alpine";
//        if (FIRST_INIT) {
//            System.out.println("首次运行，拉取 Docker 镜像: " + image);
//            pullImage(dockerClient, image);
//        }
//
//        System.out.println("创建 Docker 容器...");
//        CreateContainerResponse containerResponse = createContainer(dockerClient, image, userCodeFile);
//        String containerId = containerResponse.getId();
//        System.out.println("容器创建成功，ID: " + containerId);
//
//        System.out.println("启动容器...");
//        dockerClient.startContainerCmd(containerId).exec();
//        System.out.println("容器已启动。");
//
//        // 4. 编译代码
//        System.out.println("开始编译用户代码...");
//        ExecuteMessage compileMessage = compileFile(userCodeFile, dockerClient, containerId);
//        if (compileMessage.getErrorMessage() != null) {
//            System.err.println("编译失败: " + compileMessage.getErrorMessage());
//            return getErrorResponse(new RuntimeException(compileMessage.getErrorMessage()));
//        }
//        System.out.println("编译成功。");
//
//        // 5. 执行代码
//        System.out.println("开始执行用户代码...");
//        List<ExecuteMessage> executeMessages = runFile(userCodeFile, inputList, dockerClient, containerId);
//        System.out.println("代码执行完成。");
//
//        // 6. 收集整理输出结果
//        System.out.println("整理执行结果...");
//        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessages);
//
//        // 7. 清理容器
//        System.out.println("清理容器...");
//        dockerClient.removeContainerCmd(containerId).withForce(true).exec();
//        System.out.println("容器已清理。");
//
//        return outputResponse;
//    }
//
//    /**
//     * 获取 Docker 客户端
//     */
//    private DockerClient getDockerClient() {
//        // 配置 Docker 客户端
//        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//                .withDockerHost("tcp://localhost:2375") // 使用 TCP 协议连接 Docker
//                .build();
//
//        // 使用 ApacheDockerHttpClient 作为 HTTP 客户端
//        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
//                .dockerHost(config.getDockerHost())
//                .sslConfig(config.getSSLConfig())
//                .maxConnections(100)
//                .connectionTimeout(Duration.ofSeconds(30))
//                .responseTimeout(Duration.ofSeconds(45))
//                .build();
//
//        return DockerClientImpl.getInstance(config, httpClient);
//    }
//
//    /**
//     * 拉取 Docker 镜像
//     */
//    private void pullImage(DockerClient dockerClient, String image) {
//        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
//        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
//            @Override
//            public void onNext(PullResponseItem item) {
//                System.out.println("下载镜像状态: " + item.getStatus());
//                super.onNext(item);
//            }
//        };
//        try {
//            pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
//            System.out.println("镜像拉取完成: " + image);
//        } catch (InterruptedException e) {
//            throw new RuntimeException("拉取镜像异常", e);
//        }
//    }
//
//    /**
//     * 创建 Docker 容器
//     */
//    private CreateContainerResponse createContainer(DockerClient dockerClient, String image, File userCodeFile) {
//        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
//        HostConfig hostConfig = new HostConfig()
//                .withMemory(100 * 1000 * 1000L) // 限制内存
//                .withMemorySwap(0L)
//                .withCpuCount(1L)
//                .withBinds(new Bind(userCodeParentPath, new Volume("/app"))); // 绑定目录
//
//        CreateContainerResponse response = dockerClient.createContainerCmd(image)
//                .withHostConfig(hostConfig)
//                .withNetworkDisabled(true) // 禁用网络
//                .withReadonlyRootfs(true) // 只读文件系统
//                .withAttachStdin(true)
//                .withAttachStderr(true)
//                .withAttachStdout(true)
//                .withTty(true)
//                .exec();
//        System.out.println("容器配置完成。");
//        return response;
//    }
//
//    /**
//     * 编译代码
//     */
//    private ExecuteMessage compileFile(File userCodeFile, DockerClient dockerClient, String containerId) {
//        String compileCmd = String.format("javac -encoding utf-8 /app/%s", userCodeFile.getName());
//        System.out.println("编译命令: " + compileCmd);
//
//        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
//                .withCmd("sh", "-c", compileCmd)
//                .withAttachStderr(true)
//                .withAttachStdout(true)
//                .exec();
//
//        ExecuteMessage executeMessage = new ExecuteMessage();
//        final String[] message = {null};
//        final String[] errorMessage = {null};
//
//        ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
//            @Override
//            public void onNext(Frame frame) {
//                StreamType streamType = frame.getStreamType();
//                if (StreamType.STDERR.equals(streamType)) {
//                    errorMessage[0] = new String(frame.getPayload());
//                    System.err.println("编译错误: " + errorMessage[0]);
//                } else {
//                    message[0] = new String(frame.getPayload());
//                    System.out.println("编译输出: " + message[0]);
//                }
//                super.onNext(frame);
//            }
//        };
//
//        try {
//            dockerClient.execStartCmd(execCreateCmdResponse.getId())
//                    .exec(execStartResultCallback)
//                    .awaitCompletion();
//        } catch (InterruptedException e) {
//            throw new RuntimeException("编译过程被中断", e);
//        }
//
//        executeMessage.setMessage(message[0]);
//        executeMessage.setErrorMessage(errorMessage[0]);
//        return executeMessage;
//    }
//
//    /**
//     * 执行代码
//     */
//    private List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList, DockerClient dockerClient, String containerId) {
//        List<ExecuteMessage> executeMessagesList = new ArrayList<>();
//        for (String inputArgs : inputList) {
//            StopWatch stopWatch = new StopWatch();
//            String[] inputArgsArray = inputArgs.split(" ");
//            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
//            System.out.println("执行命令: " + String.join(" ", cmdArray));
//
//            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
//                    .withCmd(cmdArray)
//                    .withAttachStderr(true)
//                    .withAttachStdout(true)
//                    .exec();
//
//            ExecuteMessage executeMessage = new ExecuteMessage();
//            final String[] message = {null};
//            final String[] errorMessage = {null};
//
//            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
//                @Override
//                public void onNext(Frame frame) {
//                    StreamType streamType = frame.getStreamType();
//                    if (StreamType.STDERR.equals(streamType)) {
//                        errorMessage[0] = new String(frame.getPayload());
//                        System.err.println("执行错误: " + errorMessage[0]);
//                    } else {
//                        message[0] = new String(frame.getPayload());
//                        System.out.println("执行输出: " + message[0]);
//                    }
//                    super.onNext(frame);
//                }
//            };
//
//            try {
//                stopWatch.start();
//                dockerClient.execStartCmd(execCreateCmdResponse.getId())
//                        .exec(execStartResultCallback)
//                        .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
//                stopWatch.stop();
//                executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
//            } catch (InterruptedException e) {
//                throw new RuntimeException("执行过程被中断", e);
//            }
//
//            executeMessage.setMessage(message[0]);
//            executeMessage.setErrorMessage(errorMessage[0]);
//            executeMessagesList.add(executeMessage);
//        }
//        return executeMessagesList;
//    }
//
//    /**
//     * 保存用户代码到文件
//     */
//    public File saveCodeToFile(String code) {
//        String userDir = System.getProperty("user.dir");
//        String globalCodePathName = userDir + File.separator + "tmpCode";
//        if (!FileUtil.exist(globalCodePathName)) {
//            FileUtil.mkdir(globalCodePathName);
//        }
//        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
//        String userCodePath = userCodeParentPath + File.separator + "Main.java";
//        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
//    }
//
//    /**
//     * 获取错误响应
//     */
//    private ExecuteCodeResponse getErrorResponse(Throwable e) {
//        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
//        executeCodeResponse.setOutputList(new ArrayList<>());
//        executeCodeResponse.setMessage(e.getMessage());
//        executeCodeResponse.setStatus(2); // 表示代码沙箱错误
//        executeCodeResponse.setJudgeInfo(new JudgeInfo());
//        return executeCodeResponse;
//    }
//}

//
//package com.schall.jyyxcodesandbox;
//
//import cn.hutool.core.date.StopWatch;
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.core.util.ArrayUtil;
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.async.ResultCallback;
//import com.github.dockerjava.api.command.*;
//import com.github.dockerjava.api.model.*;
//import com.github.dockerjava.core.DefaultDockerClientConfig;
//import com.github.dockerjava.core.DockerClientConfig;
//import com.github.dockerjava.core.DockerClientImpl;
//import com.github.dockerjava.core.command.ExecStartResultCallback;
//import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
//import com.github.dockerjava.transport.DockerHttpClient;
//import com.schall.jyyxcodesandbox.model.ExecuteCodeRequest;
//import com.schall.jyyxcodesandbox.model.ExecuteCodeResponse;
//import com.schall.jyyxcodesandbox.model.ExecuteMessage;
//import com.schall.jyyxcodesandbox.model.JudgeInfo;
//import org.springframework.stereotype.Component;
//
//import java.io.Closeable;
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//@Component
//public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate {
//
//    private static final long TIME_OUT = 5000L; // 超时时间
//    private static final Boolean FIRST_INIT = true; // 是否首次初始化
//
//    @Override
//    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
//        List<String> inputList = executeCodeRequest.getInputList();
//        String code = executeCodeRequest.getCode();
//
//        System.out.println("1. 保存用户代码到文件...");
//        File userCodeFile = saveCodeToFile(code);
//        System.out.println("用户代码已保存到: " + userCodeFile.getAbsolutePath());
//
//        // 2. 获取 Docker 客户端
//        DockerClient dockerClient = getDockerClient();
//        System.out.println("Docker 客户端已初始化。");
//
//        // 3. 拉取镜像并创建容器
//        String image = "openjdk:8-alpine";
//        if (FIRST_INIT) {
//            System.out.println("首次运行，拉取 Docker 镜像: " + image);
//            pullImage(dockerClient, image);
//        }
//
//        System.out.println("创建 Docker 容器...");
//        CreateContainerResponse containerResponse = createContainer(dockerClient, image, userCodeFile);
//        String containerId = containerResponse.getId();
//        System.out.println("容器创建成功，ID: " + containerId);
//
//        System.out.println("启动容器...");
//        dockerClient.startContainerCmd(containerId).exec();
//        System.out.println("容器已启动。");
//
//        // 4. 编译代码
//        System.out.println("开始编译用户代码...");
//        ExecuteMessage compileMessage = compileFile(userCodeFile, dockerClient, containerId);
//        if (compileMessage.getErrorMessage() != null) {
//            System.err.println("编译失败: " + compileMessage.getErrorMessage());
//            return getErrorResponse(new RuntimeException(compileMessage.getErrorMessage()));
//        }
//        System.out.println("编译成功。");
//
//        // 5. 执行代码
//        System.out.println("开始执行用户代码...");
//        List<ExecuteMessage> executeMessages = runFile(userCodeFile, inputList, dockerClient, containerId);
//        System.out.println("代码执行完成。");
//
//        // 6. 收集整理输出结果
//        System.out.println("整理执行结果...");
//        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessages);
//
//        // 7. 清理容器
//        System.out.println("清理容器...");
//        dockerClient.removeContainerCmd(containerId).withForce(true).exec();
//        System.out.println("容器已清理。");
//
//        return outputResponse;
//    }
//
//    private DockerClient getDockerClient() {
//        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//                .withDockerHost("tcp://localhost:2375")
//                .build();
//
//        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
//                .dockerHost(config.getDockerHost())
//                .sslConfig(config.getSSLConfig())
//                .maxConnections(100)
//                .connectionTimeout(Duration.ofSeconds(30))
//                .responseTimeout(Duration.ofSeconds(45))
//                .build();
//
//        return DockerClientImpl.getInstance(config, httpClient);
//    }
//
//    private void pullImage(DockerClient dockerClient, String image) {
//        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
//        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
//            @Override
//            public void onNext(PullResponseItem item) {
//                System.out.println("下载镜像状态: " + item.getStatus());
//                super.onNext(item);
//            }
//        };
//        try {
//            pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
//            System.out.println("镜像拉取完成: " + image);
//        } catch (InterruptedException e) {
//            throw new RuntimeException("拉取镜像异常", e);
//        }
//    }
//
//    private CreateContainerResponse createContainer(DockerClient dockerClient, String image, File userCodeFile) {
//        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
//        HostConfig hostConfig = new HostConfig()
//                .withMemory(100 * 1000 * 1000L) // 限制内存
//                .withMemorySwap(0L)
//                .withCpuCount(1L)
//                .withBinds(new Bind(userCodeParentPath, new Volume("/app"))); // 绑定目录
//
//        CreateContainerResponse response = dockerClient.createContainerCmd(image)
//                .withHostConfig(hostConfig)
//                .withNetworkDisabled(true) // 禁用网络
//                .withReadonlyRootfs(true) // 只读文件系统
//                .withAttachStdin(true)
//                .withAttachStderr(true)
//                .withAttachStdout(true)
//                .withTty(true)
//                .exec();
//        System.out.println("容器配置完成。");
//        return response;
//    }
//
//    private ExecuteMessage compileFile(File userCodeFile, DockerClient dockerClient, String containerId) {
//        String compileCmd = String.format("javac -encoding utf-8 /app/%s", userCodeFile.getName());
//        System.out.println("编译命令: " + compileCmd);
//
//        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
//                .withCmd("sh", "-c", compileCmd)
//                .withAttachStderr(true)
//                .withAttachStdout(true)
//                .exec();
//
//        ExecuteMessage executeMessage = new ExecuteMessage();
//        final String[] message = {null};
//        final String[] errorMessage = {null};
//
//        ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
//            @Override
//            public void onNext(Frame frame) {
//                StreamType streamType = frame.getStreamType();
//                if (StreamType.STDERR.equals(streamType)) {
//                    errorMessage[0] = new String(frame.getPayload());
//                    System.err.println("编译错误: " + errorMessage[0]);
//                } else {
//                    message[0] = new String(frame.getPayload());
//                    System.out.println("编译输出: " + message[0]);
//                }
//                super.onNext(frame);
//            }
//        };
//
//        try {
//            dockerClient.execStartCmd(execCreateCmdResponse.getId())
//                    .exec(execStartResultCallback)
//                    .awaitCompletion();
//        } catch (InterruptedException e) {
//            throw new RuntimeException("编译过程被中断", e);
//        }
//
//        executeMessage.setMessage(message[0]);
//        executeMessage.setErrorMessage(errorMessage[0]);
//        return executeMessage;
//    }
//
//    private List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList, DockerClient dockerClient, String containerId) {
//        List<ExecuteMessage> executeMessagesList = new ArrayList<>();
//        for (String inputArgs : inputList) {
//            System.out.println("Input Args: " + inputArgs); // 打印输入参数
//            String[] inputArgsArray = inputArgs.split("\\s+"); // 使用正则表达式分割空格和换行符
//            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
//            System.out.println("执行命令: " + String.join(" ", cmdArray));
//
//            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
//                    .withCmd(cmdArray)
//                    .withAttachStderr(true)
//                    .withAttachStdout(true)
//                    .exec();
//
//            ExecuteMessage executeMessage = new ExecuteMessage();
//            final String[] message = {null};
//            final String[] errorMessage = {null};
//
//            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
//                @Override
//                public void onNext(Frame frame) {
//                    StreamType streamType = frame.getStreamType();
//                    if (StreamType.STDERR.equals(streamType)) {
//                        errorMessage[0] = new String(frame.getPayload());
//                        System.err.println("执行错误: " + errorMessage[0]);
//                    } else {
//                        message[0] = new String(frame.getPayload());
//                        System.out.println("执行输出: " + message[0]);
//                    }
//                    super.onNext(frame);
//                }
//            };
//
//            try {
//                StopWatch stopWatch = new StopWatch();
//                stopWatch.start();
//                dockerClient.execStartCmd(execCreateCmdResponse.getId())
//                        .exec(execStartResultCallback)
//                        .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
//                stopWatch.stop();
//                executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
//            } catch (InterruptedException e) {
//                throw new RuntimeException("执行过程被中断", e);
//            }
//
//            executeMessage.setMessage(message[0]);
//            executeMessage.setErrorMessage(errorMessage[0]);
//            executeMessagesList.add(executeMessage);
//        }
//        return executeMessagesList;
//    }
//
//    private ExecuteCodeResponse getErrorResponse(Throwable e) {
//        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
//        executeCodeResponse.setOutputList(new ArrayList<>());
//        executeCodeResponse.setMessage(e.getMessage());
//        executeCodeResponse.setStatus(2); // 表示代码沙箱错误
//        executeCodeResponse.setJudgeInfo(new JudgeInfo());
//        return executeCodeResponse;
//    }
//}

package com.schall.jyyxcodesandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.schall.jyyxcodesandbox.model.ExecuteCodeRequest;
import com.schall.jyyxcodesandbox.model.ExecuteCodeResponse;
import com.schall.jyyxcodesandbox.model.ExecuteMessage;
import com.schall.jyyxcodesandbox.model.JudgeInfo;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate {

    private static final long TIME_OUT = 5000L; // 超时时间
    private static final Boolean FIRST_INIT = true; // 是否首次初始化

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();

        System.out.println("1. 保存用户代码到文件...");
        File userCodeFile = saveCodeToFile(code);
        System.out.println("用户代码已保存到: " + userCodeFile.getAbsolutePath());

        // 2. 获取 Docker 客户端
        DockerClient dockerClient = getDockerClient();
        System.out.println("Docker 客户端已初始化。");

        // 3. 拉取镜像并创建容器
        String image = "openjdk:8-alpine";
        if (FIRST_INIT) {
            System.out.println("首次运行，拉取 Docker 镜像: " + image);
            pullImage(dockerClient, image);
        }

        System.out.println("创建 Docker 容器...");
        CreateContainerResponse containerResponse = createContainer(dockerClient, image, userCodeFile);
        String containerId = containerResponse.getId();
        System.out.println("容器创建成功，ID: " + containerId);

        System.out.println("启动容器...");
        dockerClient.startContainerCmd(containerId).exec();
        System.out.println("容器已启动。");

        // 4. 编译代码
        System.out.println("开始编译用户代码...");
        ExecuteMessage compileMessage = compileFile(userCodeFile, dockerClient, containerId);
        System.out.println("编译结果: " + compileMessage);
        if (compileMessage.getErrorMessage() != null) {
            System.err.println("编译失败: " + compileMessage.getErrorMessage());
            return getErrorResponse(new RuntimeException(compileMessage.getErrorMessage()));
        }
        System.out.println("编译成功。");

        // 5. 执行代码
        System.out.println("开始执行用户代码...");
        List<ExecuteMessage> executeMessages = runFile(userCodeFile, inputList, dockerClient, containerId);
        System.out.println("代码执行完成。");

        // 6. 收集整理输出结果
        System.out.println("整理执行结果...");
        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessages);

        // 7. 清理容器
        System.out.println("清理容器...");
        dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        System.out.println("容器已清理。");

        return outputResponse;
    }

    private DockerClient getDockerClient() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:2375")
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        return DockerClientImpl.getInstance(config, httpClient);
    }

    private void pullImage(DockerClient dockerClient, String image) {
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("下载镜像状态: " + item.getStatus());
                super.onNext(item);
            }
        };
        try {
            pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
            System.out.println("镜像拉取完成: " + image);
        } catch (InterruptedException e) {
            throw new RuntimeException("拉取镜像异常", e);
        }
    }

    private CreateContainerResponse createContainer(DockerClient dockerClient, String image, File userCodeFile) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        HostConfig hostConfig = new HostConfig()
                .withMemory(100 * 1000 * 1000L) // 限制内存
                .withMemorySwap(0L)
                .withCpuCount(1L)
                .withBinds(new Bind(userCodeParentPath, new Volume("/app"))); // 绑定目录

        CreateContainerResponse response = dockerClient.createContainerCmd(image)
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true) // 禁用网络
                .withReadonlyRootfs(true) // 只读文件系统
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        System.out.println("容器配置完成。");
        return response;
    }

    private ExecuteMessage compileFile(File userCodeFile, DockerClient dockerClient, String containerId) {
        String compileCmd = String.format("javac -encoding utf-8 /app/%s", userCodeFile.getName());
        System.out.println("编译命令: " + compileCmd);

        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("sh", "-c", compileCmd)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .exec();

        ExecuteMessage executeMessage = new ExecuteMessage();
        final String[] message = {null};
        final String[] errorMessage = {null};

        ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
            @Override
            public void onNext(Frame frame) {
                StreamType streamType = frame.getStreamType();
                if (StreamType.STDERR.equals(streamType)) {
                    errorMessage[0] = new String(frame.getPayload());
                    System.err.println("编译错误: " + errorMessage[0]);
                } else {
                    message[0] = new String(frame.getPayload());
                    System.out.println("编译输出: " + message[0]);
                }
                super.onNext(frame);
            }
        };

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(execStartResultCallback)
                    .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException("编译过程被中断", e);
        }

        executeMessage.setMessage(message[0]);
        executeMessage.setErrorMessage(errorMessage[0]);

        // 打印编译结果的所有参数
        System.out.println("编译结果: " + executeMessage);
        return executeMessage;
    }

    private List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList, DockerClient dockerClient, String containerId) {
        List<ExecuteMessage> executeMessagesList = new ArrayList<>();
        for (String inputArgs : inputList) {
            System.out.println("Input Args: " + inputArgs); // 打印输入参数
            String[] inputArgsArray = inputArgs.split("\\s+"); // 使用正则表达式分割空格和换行符
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
            System.out.println("执行命令: " + String.join(" ", cmdArray));

            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .exec();

            ExecuteMessage executeMessage = new ExecuteMessage();
            final StringBuilder messageBuilder = new StringBuilder(); // 使用 StringBuilder 收集标准输出
            final StringBuilder errorMessageBuilder = new StringBuilder(); // 使用 StringBuilder 收集标准错误输出
            final int[] exitValue = {0};

            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    String payload = new String(frame.getPayload());
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessageBuilder.append(payload); // 收集标准错误输出
                        System.err.println("执行错误: " + payload);
                    } else {
                        messageBuilder.append(payload); // 收集标准输出
                        System.out.println("执行输出: " + payload);
                    }
                    super.onNext(frame);
                }

                @Override
                public void onComplete() {
                    exitValue[0] = 0; // 假设执行成功
                    super.onComplete();
                }
            };

            try {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                dockerClient.execStartCmd(execCreateCmdResponse.getId())
                        .exec(execStartResultCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
                stopWatch.stop();
                executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
            } catch (InterruptedException e) {
                System.out.println("超时了，中断");
                throw new RuntimeException("执行过程被中断", e);
            }

            // 将收集到的标准输出和标准错误输出设置到 ExecuteMessage 中
            executeMessage.setExitValue(exitValue[0]);
            executeMessage.setMessage(messageBuilder.toString().trim()); // 去除多余的空格和换行符
            executeMessage.setErrorMessage(errorMessageBuilder.toString().trim()); // 去除多余的空格和换行符

            // 打印执行结果的所有参数
            System.out.println("执行结果: " + executeMessage);
            executeMessagesList.add(executeMessage);
        }
        return executeMessagesList;
    }

    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setStatus(2); // 表示代码沙箱错误
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}