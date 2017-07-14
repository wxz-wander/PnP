package wxz.org.cameraproject.ui

import vstc2.nativecaller.NativeCaller
import android.Manifest
import android.content.pm.PackageManager
import android.os.*
import android.support.design.widget.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import wxz.org.cameraproject.R
import wxz.org.cameraproject.service.BridgeService

class SplashActivity : android.support.v4.app.FragmentActivity() {
    private lateinit var thread: Thread
    private val handle: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            //super.handleMessage(msg)
            if (msg != null && WHAT_NEXT == msg.what) {
                startActivity(intentFor<LoginActivity>())
                finish()
            }

        }
    };

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(wxz.org.cameraproject.R.layout.activity_splash)
        //启动后台服务BridgeService
        startService(intentFor<BridgeService>());
        //申请权限
        if (mayRequestStorage()) {
            toast("存储权限已申请")
        }
        if (mayRequestRecordAudio()) {
            toast("录音权限已申请")
        }
        thread = Thread {
            run {
                try {
                    NativeCaller.PPPPInitialOther("ADCBBFAOPPJAHGJGBBGLFLAGDBJJHNJGGMBFBKHIBBNKOKLDHOBHCBOEHOKJJJKJBPMFLGCPPJMJAPDOIPNL")
                } catch(e: Exception) {
                    e.printStackTrace()
                }
                handle.sendEmptyMessageDelayed(WHAT_NEXT, 2000);
            }

        }

        thread.start()
    }


    private fun mayRequestStorage(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Snackbar.make(uid, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok,
                            { requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), SplashActivity.REQUEST_READ_STORAGE) })
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), SplashActivity.REQUEST_READ_STORAGE)
        }
        return false
    }

    private fun mayRequestRecordAudio():Boolean{
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return true
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            Snackbar.make(uid,"应用需要录音权限",Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok,{
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),SplashActivity.REQUEST_RECORD_AUDIO)
            })
        }else{
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),SplashActivity.REQUEST_RECORD_AUDIO)
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SplashActivity.REQUEST_READ_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toast("存储权限申请成功")
            } else {
                toast("存储权限被拒绝")
            }
        }else if (requestCode == SplashActivity.REQUEST_RECORD_AUDIO) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toast("录音权限申请成功")
            }else{
                toast("录音权限被拒绝")
            }
        }
    }


    companion object {
        /**
         * 消息的标识what
         * */
        private val WHAT_NEXT = 10;
        /**
         * Id to identity READ_EXTERNAL_STORAGE permission request.
         */
        private val REQUEST_READ_STORAGE = 1
        /**
         * Id to identity RECORD_AUDIO permission request.
         */
        private val REQUEST_RECORD_AUDIO = 2
    }
}
