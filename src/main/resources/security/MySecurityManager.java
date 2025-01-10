

import java.security.Permission;

/**
 * TODO 默认安全管理器
 */
public class MySecurityManager extends SecurityManager{

    @Override
    public void checkPermission(Permission perm) {
//        super.checkPermission(perm);
    }

    /**
     *  TODO 检测程序是否可执行
     * @param cmd   the specified system command.
     */
    @Override
    public void checkExec(String cmd) {
//        super.checkExec(cmd);
//        throw new SecurityException("checkExec 权限不足：" + cmd);
    }

    /**
     * TODO 检测程序是否允许读文件
     * @param file      the system-dependent filename.
     *
     */
    @Override
    public void checkRead(String file) {
       System.out.println(file);
       if (file.contains("E:\\JYYX\\jyyx-code-sandbox"))
       {
           return;
       }
//      throw new SecurityException("checkRead 权限异常：" + file);
    }

    /**
     * TODO 检测程序是否允许写文件
     * @param file   the system-dependent filename.
     */
    @Override
    public void checkWrite(String file) {
//        throw new SecurityException("checkWrite 权限异常：" + file);
    }

    /**
     * TODO 检测程序是否允许删除文件
     * @param file   the system-dependent filename.
     */
    @Override
    public void checkDelete(String file) {
//        throw new SecurityException("checkDelete 权限异常：" + file);
    }

    /**
     * TODO 检测程序是否允许连接网络
     * @param host   the host name port to connect to.
     * @param port   the protocol port to connect to.
     */
    @Override
    public void checkConnect(String host, int port) {
//        throw new SecurityException("checkConnect 权限异常：" + host + ":" + port);
    }

}
