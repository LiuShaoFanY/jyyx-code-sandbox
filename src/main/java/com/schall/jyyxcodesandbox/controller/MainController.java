package com.schall.jyyxcodesandbox.controller;
import com.schall.jyyxcodesandbox.JavaDockerCodeSandbox;
import com.schall.jyyxcodesandbox.JavaNativeCodeSandbox;
import com.schall.jyyxcodesandbox.model.ExecuteCodeRequest;
import com.schall.jyyxcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
public class MainController {
    //TODO 完成鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_SECRET = "secretKey";

    @Resource
    private JavaDockerCodeSandbox javaDockerCodeSandbox;
    @GetMapping("/health")
    public  String healthCheck(){
        return "ok";
    }

    /**
     * TODO 执行代码
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response){
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if(!AUTH_REQUEST_SECRET.equals(authHeader)){
            response.setStatus(403);
            return null;
        }
    if(executeCodeRequest == null){
        throw new RuntimeException("请求结果为空");
    }
    return javaDockerCodeSandbox.executeCode(executeCodeRequest);
}
}
