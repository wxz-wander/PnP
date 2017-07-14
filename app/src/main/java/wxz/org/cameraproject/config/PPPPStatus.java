package wxz.org.cameraproject.config;

/**
 * Created by wxz11 on 2017/7/9.
 */

public enum PPPPStatus {
    /**
     * public static final int PPPP_STATUS_CONNECTING = 0;//连接中
     * public static final int PPPP_STATUS_INITIALING = 1;//已连接，正在初始化
     * public static final int PPPP_STATUS_ON_LINE = 2;//在线
     * public static final int PPPP_STATUS_CONNECT_FAILED = 3;// 连接失败
     * public static final int PPPP_STATUS_DISCONNECT = 4;// 连接已关闭
     * public static final int PPPP_STATUS_INVALID_ID = 5;//无效UID
     * public static final int PPPP_STATUS_DEVICE_NOT_ON_LINE = 6;//不在线
     * public static final int PPPP_STATUS_CONNECT_TIMEOUT = 7;//连接超时
     * public static final int PPPP_STATUS_WRONGUSER_RIGHTPWD = 8;//密码错误..
     * public static final int PPPP_STATUS_WRONGPWD_RIGHTUSER = 9;// 密码错误.
     * public static final int PPPP_STATUS_WRONGPWD_WRONGUSER = 10;// 密码错误.
     */
    /**
     * 连接中
     */
    PPPP_STATUS_CONNECTING(0),
    /**
     * 已连接，正在初始化
     */
    PPPP_STATUS_INITIALING(1),
    /**
     * 在线
     */
    PPPP_STATUS_ON_LINE(2),
    /**
     * 连接失败
     */
    PPPP_STATUS_CONNECT_FAILED(3),
    /**
     * 连接已关闭
     */
    PPPP_STATUS_DISCONNECT(4),
    /**
     * 无效UID
     */
    PPPP_STATUS_INVALID_ID(5),
    /**
     * 不在线
     */
    PPPP_STATUS_DEVICE_NOT_ON_LINE(6),
    /**
     * 连接超时
     */
    PPPP_STATUS_CONNECT_TIMEOUT(7),
    /**
     * 密码错误..
     */
    PPPP_STATUS_WRONGUSER_RIGHTPWD(8),
    /**
     * 密码错误.
     */
    PPPP_STATUS_WRONGPWD_RIGHTUSER(9),
    /**
     * 密码错误.
     */
    PPPP_STATUS_WRONGPWD_WRONGUSER(10);


    private int status;

    private PPPPStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

}
