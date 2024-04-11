package com.xflip.arglassesdemo.base

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.KeyEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewbinding.ViewBinding
import com.xflip.arglassesdemo.entity.MessageEvent
import com.xflip.arglassesdemo.utils.ActivityUtil
import com.xflip.arglassesdemo.utils.ViewUtil
import com.xflip.arglassesdemo.utils.WeakHandler
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class BaseActivity<VM : BaseViewModel, B : ViewBinding>: AppCompatActivity(), Handler.Callback {

    lateinit var viewModel: VM

    lateinit var viewBinding: B

    lateinit var rootView: View

    private var activityLauncher: ActivityResultLauncher<Intent>? = null

    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null

    private var arCallback: ((resultCode: Int, resultIntent: Intent?) -> Unit)? = null

    private var permissionAgree: (() -> Unit)? = null

    private var permissionRefuse: ((refusePermissions: ArrayList<String>) -> Unit)? = null

    lateinit var mainHandler: WeakHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtil.addActivity(this)
        EventBus.getDefault().register(this)
        initViewModelAndOther()
        viewBinding = ViewUtil.inflateWithGeneric(this, layoutInflater)
        rootView = viewBinding.root
        setContentView(rootView)
        initData()
        initEvent()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        ActivityUtil.removeActivity(this)
        mainHandler.removeCallbacksAndMessages(null)
    }

    protected open fun initData() {}

    protected open fun initEvent() {}

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: MessageEvent?) {
        //EventBus Do something
    }

    private fun initViewModelAndOther() {
        viewModel = createViewModel(this, createViewModel())
        activityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result != null) {
                arCallback?.let {
                    it(result.resultCode, result.data)
                }
            }
        }
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            val refusePermission: ArrayList<String> = ArrayList()
            it.keys.forEach { res ->
                if (it[res] == false) {
                    refusePermission.add(res)
                }
            }

            if (refusePermission.isNotEmpty()) {
                permissionRefuse?.let {
                    it(refusePermission)
                }
            } else {
                permissionAgree?.let {
                    it()
                }
            }
        }
        mainHandler = WeakHandler(Looper.getMainLooper(), this)
    }

    fun startActivity(intent: Intent, arCallback: (resultCode: Int, resultIntent: Intent?) -> Unit) {
        this.arCallback = arCallback
        activityLauncher?.launch(intent)
    }

    fun requestPermission(permissions: Array<String>,
                          agree: () -> Unit,
                          refuse: (refusePermissions: ArrayList<String>) -> Unit
    ) {
        this.permissionAgree = agree
        this.permissionRefuse = refuse
        var allAgree = true
        for (permission in permissions) {
            if( ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
                allAgree=false
                break
            }
        }
        if (allAgree) {
            permissionAgree?.let {
                it()
            }
            return
        }
        permissionLauncher?.launch(permissions)
    }

    override fun handleMessage(msg: Message): Boolean {
        return false
    }

    /**
     * create ViewModel
     */
    abstract fun createViewModel(): VM

    /** Whether to block the return key
     * @return
     */
    protected open fun isRefusedBackPress(): Boolean {
        return false
    }

    private fun createViewModel(owner: ViewModelStoreOwner, viewModel: VM): VM {
        return ViewModelProvider(owner)[viewModel.javaClass]
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isRefusedBackPress() && keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

}