package wxz.org.cameraproject.ui

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import org.jetbrains.anko.configuration
import org.jetbrains.anko.portrait
import org.jetbrains.anko.toast
import wxz.org.cameraproject.R
import wxz.org.cameraproject.ipcamera.utils.*
import wxz.org.cameraproject.service.BridgeService
import vstc2.nativecaller.NativeCaller
import java.nio.ByteBuffer

/**
 * 实时影像播放界面
 * */
class PlayActivity : AppCompatActivity(), CustomAudioRecorder.AudioRecordResult, BridgeService.PlayInterface {

    private lateinit var surfaceView: GLSurfaceView
    //缓存
    private var audioBuffer: CustomBuffer? = null
    //视频播放器
    private lateinit var audioplayer: AudioPlayer
    //视频录制器
    private lateinit var myVideoRecorder: CustomVideoRecord
    //音频录制器
    private lateinit var customAudioRecorder: CustomAudioRecorder
    private var nameStr: String? = null
    private var didStr: String? = null

    //渲染器
    private lateinit var myRender: MyRender
    // 录每张图片的时间
    private var videoTime: Long = 0
    //是否在录像
    private var isTakeVideo: Boolean = false
    //亮度标志
    private val BRIGHT = 1
    //对比度标志
    private val CONTRAST = 2
    //IR(夜视)标志
    private val IR_STATE = 14
    //分辨率值
    private var mResolution = 0
    //亮度值
    private var mBrightness = 0
    //对比度
    private var mContrast = 0
    //是否初始化设备参数
    private var mInitCameraParams = false
    //镜像标志（上下、左右）
    private var mUpDownMirror = false
    private var mLeftRightMirror = false
    //是否准备手动退出
    private var mManualExit = false
    //是否是H264格式
    private var isH264 = false
    private var isJpeg = false
    private var isTakePicture = false
    private var isPictureSave = false
    private var isTalking = false
    //是否在使用麦克风
    private var isMicrophone = false
    private var mDisplayFinished = true
    //视频数据
    private var mVideoData: ByteArray? = null
    private var mVideoLength = 0
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private lateinit var mBitmap: Bitmap
    //分辨率格式
    private var mStreamCodeType: Int? = 0
    private var mAudioRecordStart = false
    private var isPTZPrompt = false
    private var widthPixels = 0
    private var heightPixels = 0
    //分辨率标识
    private var isMax = false
    private var isHigh = false
    private var isP720 = false
    private var isMiddle = false
    private var isQvga1 = false
    private var isVga1 = false
    private var isQvga = false
    private var isVga = false


    private val mHandle: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            Log.d(TAG,"---------------------msg: start")
            if (msg != null) {
                if (!isPTZPrompt) {
                    isPTZPrompt = true
                    toast(R.string.ptz_control)
                }
                when (msg.what) {
                    11
                    -> {//h264
                        if (resolutionMap.size == 0) {
                            if (mResolution == 0) {
                                isMax = true
                                isMiddle = false
                                isP720 = false
                                isHigh = false
                                isVga1 = false
                                isQvga1 = false
                                addResolution(ST_MAX, isMax)
                            } else if (mResolution == 1) {
                                isMax = false
                                isMiddle = false
                                isP720 = false
                                isHigh = true
                                isVga1 = false
                                isQvga1 = false
                                addResolution(ST_HIGH, isHigh)
                            } else if (mResolution == 2) {
                                isMax = false
                                isMiddle = true
                                isP720 = false
                                isHigh = false
                                isVga1 = false
                                isQvga1 = false
                                addResolution(ST_MIDDLE, isMiddle)
                            } else if (mResolution == 3) {
                                isMax = false
                                isMiddle = false
                                isP720 = true
                                isHigh = false
                                isVga1 = false
                                isQvga1 = false
                                addResolution(ST_P720, isP720)
                            } else if (mResolution == 4) {
                                isMax = false
                                isMiddle = false
                                isP720 = false
                                isHigh = false
                                isVga1 = true
                                isQvga1 = false
                                addResolution(ST_VGA1, isVga1)
                            } else if (mResolution == 5) {
                                isMax = false
                                isMiddle = false
                                isP720 = false
                                isHigh = false
                                isVga1 = false
                                isQvga1 = true
                                addResolution(ST_QVGA1, isQvga1)
                            }
                        } else {
                            if (resolutionMap.containsKey(didStr)) {
                                getResolution()
                            } else {
                                if (mResolution == 0) {
                                    isMax = true
                                    isMiddle = false
                                    isP720 = false
                                    isHigh = false
                                    isVga1 = false
                                    isQvga1 = false
                                    addResolution(ST_MAX, isMax)
                                } else if (mResolution == 1) {
                                    isMax = false
                                    isMiddle = false
                                    isP720 = false
                                    isHigh = true
                                    isVga1 = false
                                    isQvga1 = false
                                    addResolution(ST_HIGH, isHigh)
                                } else if (mResolution == 2) {
                                    isMax = false
                                    isMiddle = true
                                    isP720 = false
                                    isHigh = false
                                    isVga1 = false
                                    isQvga1 = false
                                    addResolution(ST_MIDDLE, isMiddle)
                                } else if (mResolution == 3) {
                                    isMax = false
                                    isMiddle = false
                                    isP720 = true
                                    isHigh = false
                                    isVga1 = false
                                    isQvga1 = false
                                    addResolution(ST_P720, isP720)
                                } else if (mResolution == 4) {
                                    isMax = false
                                    isMiddle = false
                                    isP720 = false
                                    isHigh = false
                                    isVga1 = true
                                    isQvga1 = false
                                    addResolution(ST_VGA1, isVga1)
                                } else if (mResolution == 5) {
                                    isMax = false
                                    isMiddle = false
                                    isP720 = false
                                    isHigh = false
                                    isVga1 = false
                                    isQvga1 = true
                                    addResolution(ST_QVGA1, isQvga1)
                                }
                            }
                        }

                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            val layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(widthPixels, widthPixels * 3 / 4)
                            layoutParams.gravity = Gravity.CENTER
                            surfaceView.layoutParams = layoutParams
                        } else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            val layoutParams = FrameLayout.LayoutParams(widthPixels, heightPixels)
                            layoutParams.gravity = Gravity.CENTER
                            surfaceView.layoutParams = layoutParams
                        }
                        myRender.writeSample(mVideoData, mVideoWidth, mVideoHeight)
                    }
                    12 -> {//JPEG
                        if (resolutionMap.size == 0) {
                            if (mResolution == 1) {
                                isVga = true
                                isQvga = false
                                addResolution(ST_VGA, isVga)
                            } else if (mResolution == 0) {
                                isVga = false
                                isQvga = true
                                addResolution(ST_QVGA, isQvga)
                            }
                        } else {
                            if (resolutionMap.containsKey(didStr)) {
                                getResolution()
                            } else {
                                if (mResolution == 1) {
                                    isVga = true
                                    isQvga = false
                                    addResolution(ST_VGA, isVga)
                                } else if (mResolution == 0) {
                                    isVga = false
                                    isQvga = true
                                    addResolution(ST_QVGA, isQvga)
                                }
                            }
                        }
                        mBitmap = BitmapFactory.decodeByteArray(mVideoData, 0, mVideoLength)
                        if (mBitmap == null) {
                            mDisplayFinished = true
                            return
                        }
                        if (isTakePicture) {
                            takePicture(mBitmap)
                            isTakePicture = false
                        }
                        mVideoWidth = mBitmap.width
                        mVideoHeight = mBitmap.height
                    }
                    -11 -> {//通知消息--这里就只有断线这个消息
                        Log.d(TAG, "断线了")
                        toast(R.string.pppp_status_disconnect)
                        //TODO 这里要么退出，要么做一个弹窗，此时与设备的连接已断开
                    }
                }
                if (msg.what == 11 || msg.what == 12) {
                    mDisplayFinished = true
                }
            }
        }
    }

    /**---------------------------------------------------------------*/
    //对讲音频录制的回调
    override fun AudioRecordData(data: ByteArray?, len: Int) {
        if (mAudioRecordStart && len > 0) {
            NativeCaller.PPPPTalkAudioData(didStr, data, len)
        }
    }

    //视频播放接口回调
    /**
     * 设备参数回调
     * @param did 设备uid,
     * @param resolution 分辨率
     * @param brightness 亮度
     * @param contrast 对比度
     * @param hue 色彩值
     * @param saturation 饱和度
     * @param flip 镜像开关状态
     * @param mode 模式
     * */
    override fun callBackCameraParamNotify(did: String?, resolution: Int, brightness: Int, contrast: Int, hue: Int, saturation: Int, flip: Int, mode: Int) {
        Log.d(TAG, "设备返回的参数 did：$did , resolution: $resolution , brightness: $brightness , contrast : $contrast , hue: $hue ,saturation: $saturation ,flip: $flip ,mode: $mode")
        //记录亮度。分辨率和对比度
        mResolution = resolution
        mBrightness = brightness
        mContrast = contrast
        mInitCameraParams = true
        //根据摄像头当前状态flip处理页面按钮状态
        //todo 如果有上下、左右镜像处理或者状态显示，就需要在这里做处理
        when (flip) {
            FLIP_NO_MIRROR -> {//无镜像
                mUpDownMirror = false
                mLeftRightMirror = false
                //如果去设置一些页面按钮状态，最好回到主线程
//                runOnUiThread {
//                    iv_left.setImageResource()
//                }
            }
            FLIP_VERTICAL_MIRROR -> {//上下镜像
                mUpDownMirror = true
                mLeftRightMirror = false
            }
            FLIP_HORIZONTAL_MIRROR -> {//左右镜像
                mUpDownMirror = false
                mLeftRightMirror = true
            }
            FLIP_VERTICAL_HORIZONTAL_MIRROR -> {//两方向都被镜像了
                mUpDownMirror = true
                mLeftRightMirror = true
            }
        }

    }

    /***
     * BridgeService callback 视频数据流回调
     *
     * **/
    override fun callBackVideoData(videobuf: ByteArray?, h264Data: Int, len: Int, width: Int, height: Int) {
        Log.d(TAG, "底层返回的数据 videobuf：$videobuf , h264Data: $h264Data ,len: $len ,width: $width ,height: $height")
        if (!mDisplayFinished) {
            return
        }
        mDisplayFinished = false
        mVideoData = videobuf
        mVideoLength = len
        val message = Message.obtain()
        if (h264Data == 1) {//h264数据
            mVideoWidth = width
            mVideoHeight = height
            //todo 这里处理拍照，保存照片等逻辑
            if (isTakePicture) {
                isTakePicture = false
                val rgb = ByteArray(width * height * 2)
                NativeCaller.YUV4202RGB565(videobuf, rgb, width, height)
                val buffer = ByteBuffer.wrap(rgb)
                mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                mBitmap.copyPixelsFromBuffer(buffer)
                takePicture(mBitmap)
            }
            isH264 = true
            message.what = 11;

        } else {//MJPEG
            isJpeg = true
            message.what = 12
        }
        mHandle.sendMessage(message)
        //TODO 拍视频就在这里处理
        if (isTakeVideo) {
            val time: Long = SystemClock.currentThreadTimeMillis()
            val tspan: Int = (time - videoTime).toInt()
            Log.d(TAG, "play time span : $tspan")
            videoTime = time
            if (videoRecorder != null) {
                if (isJpeg) {
                    videoRecorder?.videoRecordData(2, videobuf, width, height, tspan)
                }
            }

        }
    }


    /***
     * BridgeService callback
     * 通知消息的回调
     *
     * **/
    override fun callBackMessageNotify(did: String?, msgType: Int, param: Int) {
        Log.d(TAG, "callBackMessageNotify did: $did ,msgType: $msgType ,param: $param")
        if (mManualExit) {
            return
        }
        if (ContentCommon.PPPP_MSG_TYPE_STREAM == msgType) {
            mStreamCodeType = param
            return
        }
        if (ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS != msgType) {
            return
        }
        if (!did.equals(didStr)) {
            return
        }

        val message = Message.obtain()
        message.what = -11
        mHandle.sendMessage(message)
    }

    /***
     * BridgeService callback
     * 视频数据
     *
     * **/
    override fun callBackAudioData(pcm: ByteArray?, len: Int) {
        Log.d(TAG, "callBackAudioData len: $len")
        if (!audioplayer.isAudioPlaying) {
            return
        }
        val head: CustomBufferHead = CustomBufferHead()
        val data: CustomBufferData = CustomBufferData()
        head.length = len
        head.startcode = AUDIO_BUFFER_START_CODE
        data.head = head
        data.data = pcm
        audioBuffer?.addData(data)

    }

    /***
     * BridgeService callback
     * h264的影像数据
     *
     * **/
    override fun callBackH264Data(h264: ByteArray?, type: Int, size: Int) {
        Log.d(TAG, "callBackH264Data type: $type ,size: $size")
        if (isTakeVideo) {
            val millis = SystemClock.currentThreadTimeMillis()
            val tspan: Int = (millis - videoTime).toInt()
            Log.d(TAG, "play tspan $tspan")
            videoTime = millis
            if (videoRecorder != null) {
                videoRecorder?.videoRecordData(type, h264, size, 0, tspan)
            }
        }
    }

    /**---------------------------------------------------------------*/

    //拍照的逻辑
    private fun takePicture(bitmap: Bitmap) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        surfaceView = findViewById(R.id.my_surface_view) as GLSurfaceView
        init()
    }

    private fun init() {
        nameStr = SystemValue.deviceName
        didStr = SystemValue.deviceId
        audioBuffer = CustomBuffer()
        audioplayer = AudioPlayer(audioBuffer)
        customAudioRecorder = CustomAudioRecorder(this)
        myVideoRecorder = CustomVideoRecord(this, didStr)
        BridgeService.setPlayInterface(this@PlayActivity)
        //p2p在线后，开启视频流，确保只会start一次，切勿重复start
        NativeCaller.StartPPPPLivestream(didStr, 10, 1)
        getCameraParams()
        myRender = MyRender(surfaceView)
        surfaceView.setRenderer(myRender)
        widthPixels = resources.displayMetrics.widthPixels
        heightPixels = resources.displayMetrics.heightPixels
    }

    /**
     * 获取智能摄像头的参数
     * */
    private fun getCameraParams() {
        NativeCaller.PPPPGetSystemParams(didStr, ContentCommon.MSG_TYPE_GET_CAMERA_PARAMS)
    }

    /**
     * 设置摄像头的分辨率
     * */
    protected fun setResolution(resolution: Int) {
        Log.d(TAG, "setResolution resolution:$resolution")
        NativeCaller.PPPPCameraControl(didStr, 16, resolution)
    }

    override fun onDestroy() {
        NativeCaller.StopPPPPLivestream(didStr)
        super.onDestroy()
    }

    private fun addResolution(mess: String, isFast: Boolean) {
        if (resolutionMap.size != 0) {
            if (resolutionMap.containsKey(didStr)) {
                resolutionMap.remove(didStr)
            }
        }

        val map: Map<Any, Any> = mapOf(mess to isFast);
        resolutionMap.put(didStr, map)

    }

    private fun getResolution() {
        if (resolutionMap.containsKey(didStr)) {
            val map = resolutionMap.get(didStr)
            if (map != null) {
                if (map.containsKey(ST_QVGA)) {
                    isQvga = true
                } else if (map.containsKey(ST_QVGA1)) {
                    isQvga1 = true
                } else if (map.containsKey(ST_VGA)) {
                    isVga = true
                } else if (map.containsKey(ST_VGA1)) {
                    isVga1 = true
                } else if (map.containsKey(ST_HIGH)) {
                    isHigh = true
                } else if (map.containsKey(ST_MIDDLE)) {
                    isMiddle = true
                } else if (map.containsKey(ST_P720)) {
                    isP720 = true
                } else if (map.containsKey(ST_MAX)) {
                    isMax = true
                }
            }
        }
    }


    private var videoRecorder: VideoRecorder? = null
    public fun setVideoRecord(videoRecord: VideoRecorder) {
        this.videoRecorder = videoRecord
    }

    public interface VideoRecorder {
        fun videoRecordData(type: Int, videodata: ByteArray?, width: Int, height: Int, time: Int)
    }

    private companion object {
        val AUDIO_BUFFER_START_CODE = 0xff00ff
        val TAG = PlayActivity.javaClass.simpleName
        val ST_QVGA = "qvga"
        val ST_VGA = "vga"
        val ST_QVGA1 = "qvga1"
        val ST_VGA1 = "vga1"
        val ST_P720 = "p720"
        val ST_HIGH = "high"
        val ST_MIDDLE = "middle"
        val ST_MAX = "max"
        /**无镜像处理*/
        val FLIP_NO_MIRROR = 0
        /**上下（垂直）镜像*/
        val FLIP_VERTICAL_MIRROR = 1
        /**左右(水平)镜像*/
        val FLIP_HORIZONTAL_MIRROR = 2
        /**上下左右都被镜像处理*/
        val FLIP_VERTICAL_HORIZONTAL_MIRROR = 3
        //申明可变的map，存储分辨率参数
        var resolutionMap: MutableMap<String?, Map<Any, Any>> = mutableMapOf()
    }
}
