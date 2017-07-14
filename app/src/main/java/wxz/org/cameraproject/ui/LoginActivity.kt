package wxz.org.cameraproject.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View

import wxz.org.cameraproject.R
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast
import wxz.org.cameraproject.ipcamera.utils.ContentCommon
import wxz.org.cameraproject.ipcamera.utils.SystemValue
import wxz.org.cameraproject.service.BridgeService
import wxz.org.cameraproject.service.BridgeService.IpcamClientInterface
import vstc2.nativecaller.NativeCaller
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.os.Bundle
import org.jetbrains.anko.intentFor
import wxz.org.cameraproject.config.PPPPStatus


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), IpcamClientInterface {

    private var option: Int = ContentCommon.INVALID_OPTION
    /**
     * 相机类型
     * */
    private var cameraType: Int = ContentCommon.CAMERA_TYPE_MJPEG
    private var progressDialog: ProgressDialog? = null
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null
    private var tag: Int = 0;
    private lateinit var intentbrod: Intent
    private lateinit var receiver: FinishBroadCastReceiver
    //是否正在连接中
    private var connecting: Boolean = false
    //只能摄像头是否是可用状态
    private var cameraIsOk: Boolean = false

    private val PPPPMsgHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {

            val bd = msg.getData()
            val msgParam = bd.getInt(STR_MSG_PARAM)
            val msgType = msg.what
            Log.i("aaa", "====$msgType--msgParam:$msgParam")
            val did = bd.getString(STR_DID)
            when (msgType) {
                ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS -> {
                    val resid: Int
                    when (msgParam) {
                        ContentCommon.PPPP_STATUS_CONNECTING//0
                        -> {
                            resid = R.string.pppp_status_connecting
//                            showProgress(false)
                            tag = 2
                        }
                        ContentCommon.PPPP_STATUS_CONNECT_FAILED//3
                        -> {
                            resid = R.string.pppp_status_connect_failed
//                            showProgress(false)
                            tag = 0
                        }
                        ContentCommon.PPPP_STATUS_DISCONNECT//4
                        -> {
                            resid = R.string.pppp_status_disconnect
//                            showProgress(false)
                            tag = 0
                        }
                        ContentCommon.PPPP_STATUS_INITIALING//1
                        -> {
                            resid = R.string.pppp_status_initialing
//                            showProgress(false)
                            tag = 2
                        }
                        ContentCommon.PPPP_STATUS_INVALID_ID//5
                        -> {
                            resid = R.string.pppp_status_invalid_id
//                            showProgress(false)
                            tag = 0
                        }
                        ContentCommon.PPPP_STATUS_ON_LINE//2 在线状态
                        -> {
                            resid = R.string.pppp_status_online
//                            showProgress(false)
                            //摄像机在线之后读取摄像机类型
                            val cmd: String = "get_status.cgi?loginuse=admin&loginpas=${SystemValue.devicePass}&user=admin&pwd=${SystemValue.devicePass}"
                            NativeCaller.TransferMessage(did, cmd, 1)
                            tag = 1
                            startActivity(intentFor<PlayActivity>())
                        }
                        ContentCommon.PPPP_STATUS_DEVICE_NOT_ON_LINE//6
                        -> {
                            resid = R.string.device_not_on_line
//                            showProgress(false)
                            tag = 0
                        }
                        ContentCommon.PPPP_STATUS_CONNECT_TIMEOUT//7
                        -> {
                            resid = R.string.pppp_status_connect_timeout
//                            showProgress(false)
                            tag = 0
                        }
                        ContentCommon.PPPP_STATUS_CONNECT_ERRER//8
                        -> {
                            resid = R.string.pppp_status_pwd_error
//                            showProgress(false)
                            tag = 0
                        }
                        else -> resid = R.string.pppp_status_unknown
                    }
                    toast(resources.getString(resid))
                    if (msgParam == ContentCommon.PPPP_STATUS_ON_LINE) {
                        NativeCaller.PPPPGetSystemParams(did, ContentCommon.MSG_TYPE_GET_PARAMS)
                    }
                    if (msgParam == ContentCommon.PPPP_STATUS_INVALID_ID
                            || msgParam == ContentCommon.PPPP_STATUS_CONNECT_FAILED
                            || msgParam == ContentCommon.PPPP_STATUS_DEVICE_NOT_ON_LINE
                            || msgParam == ContentCommon.PPPP_STATUS_CONNECT_TIMEOUT
                            || msgParam == ContentCommon.PPPP_STATUS_CONNECT_ERRER) {
                        NativeCaller.StopPPPP(did)
                    }
                }
                ContentCommon.PPPP_MSG_TYPE_PPPP_MODE -> {//默认状态
                }
            }

        }
    }

    /**-------------摄像头接口回调*/
    override fun BSMsgNotifyData(did: String?, type: Int, param: Int) {

        if (!this.isFinishing) {
            Log.d(TAG, "type:$type param:$param")
            val bd = Bundle()
            val msg = PPPPMsgHandler.obtainMessage()
            msg.what = type
            bd.putInt(STR_MSG_PARAM, param)
            bd.putString(STR_DID, did)
            msg.data = bd
            PPPPMsgHandler.sendMessage(msg)
            if (type === ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS) {
                intentbrod.putExtra("ifdrop", param)
                sendBroadcast(intentbrod)
            }
        }
    }

    override fun BSSnapshotNotify(did: String?, bImage: ByteArray?, len: Int) {

    }

    override fun callBackUserParams(did: String?, user1: String?, pwd1: String?, user2: String?, pwd2: String?, user3: String?, pwd3: String?) {

    }

    override fun CameraStatus(did: String?, status: Int) {

    }

    /**-------------摄像头接口回调*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

//        progressDialog = indeterminateProgressDialog("连接中...", null)
//        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
//            if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                attemptLogin()
//                return@OnEditorActionListener true
//            }
//            false
//        })
        login.setOnClickListener {
            if (connecting) {
                toast("正在连接，请稍后再试！")
            } else {
                attemptLogin()
            }
        }
        /**
         * 注册广播接受者
         * */
        receiver = FinishBroadCastReceiver();
        var filter: IntentFilter = IntentFilter()
        filter.addAction("finish")
        registerReceiver(receiver, filter)
        intentbrod = Intent("drop")
    }

    override fun onDestroy() {
        //解绑广播接受者
        unregisterReceiver(receiver)
        NativeCaller.Free()
        stopService(intentFor<BridgeService>())
        super.onDestroy()
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        uid.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val uidStr = uid.text.toString().trim()
        val passwordStr = password.text.toString().trim()
        val usernameStr = username.text.toString().trim()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
            toast(getString(R.string.error_invalid_password))
        }
        if (TextUtils.isEmpty(usernameStr)) {
            username.error = getString(R.string.error_invalid_username)
            focusView = username
            cancel = true
            toast(getString(R.string.error_invalid_username))
        }

        // Check for a valid uid.
        if (TextUtils.isEmpty(uidStr)) {
            uid.error = getString(R.string.error_field_required)
            focusView = uid
            cancel = true
            toast(getString(R.string.error_field_required))
        } else if (!isUIDValid(uidStr)) {
            uid.error = getString(R.string.error_invalid_uid)
            focusView = uid
            cancel = true
            toast(getString(R.string.error_invalid_uid))
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true)

            progressDialog = indeterminateProgressDialog("连接中...", null)
            progressDialog?.show()
            /**
             * 获取外部类activity的实例
             * */
            BridgeService.setIpcamClientInterface(this@LoginActivity)
            NativeCaller.Init()
            mAuthTask = UserLoginTask(uidStr, passwordStr, usernameStr)
            mAuthTask!!.execute(null as Void?)
        }
    }

    private fun isUIDValid(uid: String): Boolean {
        return uid.matches(Regex("^[a-zA-Z0-9]+$"))
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length >= 6
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val mUID: String, private val mPassword: String, private val mUsername: String) : AsyncTask<Void, Void, Int>() {

        override fun doInBackground(vararg params: Void): Int? {
            // TODO: attempt authentication against a network service.
            if (option == ContentCommon.INVALID_OPTION) {
                option = ContentCommon.ADD_CAMERA
            }
            //设置参数
            SystemValue.deviceId = mUID
            SystemValue.deviceName = mUsername
            SystemValue.devicePass = mPassword
            var result: Int
            if (SystemValue.deviceId.toLowerCase().startsWith("vsta")) {
                result = NativeCaller.StartPPPPExt(SystemValue.deviceId, SystemValue.deviceName,
                        SystemValue.devicePass, 1, "", "EFGFFBBOKAIEGHJAEDHJFEEOHMNGDCNJCDFKAKHLEBJHKEKMCAFCDLLLHAOCJPPMBHMNOMCJKGJEBGGHJHIOMFBDNPKNFEGCEGCBGCALMFOHBCGMFK");
            } else {
                result = NativeCaller.StartPPPP(SystemValue.deviceId, SystemValue.deviceName,
                        SystemValue.devicePass, 1, "");
            }
            SystemClock.sleep(1000)
            Log.i(LoginActivity.TAG, "登录摄像头结果result：" + result)
            return result
            /*DUMMY_CREDENTIALS
                    .map { it.split(":") }
                    .firstOrNull { it[0] == mUID }
                    ?.let {
                        // Account exists, return true if the password matches.
                        it[1] == mPassword
                    }
                    ?: true*/
        }

        override fun onPostExecute(result: Int?) {
            mAuthTask = null
//            showProgress(false)

            if (result == PPPPStatus.PPPP_STATUS_CONNECTING.status) {
                connecting = true
                toast("连接中...")
                //finish()
            } else if (result == PPPPStatus.PPPP_STATUS_INITIALING.status) {
                toast("已连接，正在初始化摄像头...")
                cameraIsOk = true
            } else if (result == PPPPStatus.PPPP_STATUS_ON_LINE.status) {
                toast("摄像头已在线...")
                /**
                 * //摄像机在线之后读取摄像机类型
                String cmd="get_status.cgi?loginuse=admin&loginpas=" + SystemValue.devicePass
                + "&user=admin&pwd=" + SystemValue.devicePass;
                NativeCaller.TransferMessage(did, cmd, 1);
                 * */
                cameraIsOk = true
            } else if (result == PPPPStatus.PPPP_STATUS_CONNECT_FAILED.status) {
                toast("连接失败，请稍后重试...")
            } else if (result == PPPPStatus.PPPP_STATUS_DISCONNECT.status) {
                toast("连接已关闭...")
            } else if (result == PPPPStatus.PPPP_STATUS_INVALID_ID.status) {
                toast("设备UID无效...")
                uid.error = getString(R.string.pppp_status_invalid_id)
                uid.requestFocus()
            } else if (result == PPPPStatus.PPPP_STATUS_DEVICE_NOT_ON_LINE.status) {
                toast("摄像头不在线...")
            } else if (result == PPPPStatus.PPPP_STATUS_CONNECT_TIMEOUT.status) {
                toast("连接超时...")
            } else if (result == PPPPStatus.PPPP_STATUS_WRONGPWD_RIGHTUSER.status) {
                toast("密码错误...")
                password.error = getString(R.string.pppp_status_pwd_error)
                password.requestFocus()
            } else if (result == PPPPStatus.PPPP_STATUS_WRONGPWD_WRONGUSER.status) {
                toast("用户名和密码都不正确...")
                username.error = getString(R.string.error_invalid_username)
                password.error = getString(R.string.pppp_status_pwd_error)
                username.requestFocus()
            } else if (result == PPPPStatus.PPPP_STATUS_WRONGUSER_RIGHTPWD.status) {
                toast("用户名错误...")
                username.error = getString(R.string.error_invalid_username)
                username.requestFocus()
            }
            if (progressDialog != null) {//&& progressDialog.isShowing
                progressDialog?.dismiss()
            }

            //智能摄像头可用之后，跳转到实时影像播放界面
            if (result == PPPPStatus.PPPP_STATUS_ON_LINE.status) {
                startActivity(intentFor<PlayActivity>())
                //finish()
            }

        }

        override fun onCancelled() {
            mAuthTask = null
//            showProgress(false)
        }
    }

    private inner class FinishBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            this@LoginActivity.finish()
            Log.d(TAG, "AddCameraActivity.this.finish()")
        }
    }

    /**
     * 伴随对象，定义常量
     * */
    companion object {

        /**
         * Id to identity READ_EXTERNAL_STORAGE permission request.
         */
        private val REQUEST_READ_STORAGE = 0

        private val TAG = LoginActivity.javaClass.simpleName
        private val STR_DID = "did"
        private val STR_MSG_PARAM = "msgparam"
    }
}
