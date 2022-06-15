package top.MeihuTETL.tetl;

public class DatabaseURL {
    //注意，这个百度云服务器将在2022.8.20停止工作，如果你想要使用请更换服务器
    public static final String HOST = "jdbc:mysql://120.48.38.204:3306/Android?useUnicode=true&characterEncoding=utf8";
    // 用户名
    public static final String USER = "root";
    // 密码
    public static final String PASSWORD = "420325";
    // Java数据库连接JDBC驱动，此处使用的是5.1.30版本的connect，如果换成高版本请更改为com.mysql.cj.jdbc.Driver
    public static final String DRIVER = "com.mysql.jdbc.Driver";
}
