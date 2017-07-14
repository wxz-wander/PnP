package wxz.org.cameraproject.utils

import android.content.Context
import android.widget.Toast
import javax.xml.datatype.Duration

/**
 * Created by wxz11 on 2017/7/7.
 */
class CommonUtils {
    /**
     * 封装的toast
     * */
    fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration)
    }
}