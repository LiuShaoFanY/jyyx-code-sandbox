//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * 向取服务器写文件（植入威胁程序）
// */
//public class Main {
//    public static void main(String[] args) throws InterruptedException, IOException
//    {
//        String userDir = System.getProperty("user.dir");
//        String filePath = userDir + File.separator + "src/main/resources/演示木马程序.bat";
//        String errorProgram = "java -version 2>&1";
//        Files.write(Paths.get(filePath), Arrays.asList(errorProgram));
//        System.out.println("演示木马写入成功！！！");
//
//    }
//}
