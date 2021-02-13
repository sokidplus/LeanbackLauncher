package com.amazon.tv.leanbacklauncher.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import com.amazon.tv.leanbacklauncher.BuildConfig
import com.amazon.tv.leanbacklauncher.MainActivity
import com.amazon.tv.leanbacklauncher.R
import java.io.File

class LegacyFileListFragment : GuidedStepSupportFragment() {

    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        return Guidance(
                getString(R.string.select_wallpaper_action_title),  // title
                getWallpaperDesc(requireContext()),  // description
                getString(R.string.settings_dialog_title),  // breadcrumb (parent)
                ResourcesCompat.getDrawable(resources, R.drawable.ic_settings_home, null) // icon
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        val activity = requireActivity()
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "READ_EXTERNAL_STORAGE permission is granted")
        } else {
            Log.v(TAG, "READ_EXTERNAL_STORAGE permission not granted")
            makeRequest(activity)
        }

//        val gpath = Environment.getExternalStorageDirectory().absolutePath
//        val spath = "Pictures"
//        var fullpath = File(gpath + File.separator + spath)
        val dir = File(Environment.getExternalStorageDirectory().absolutePath)
        val images = imageReader(dir)

        if (images.size > 0)
            images.forEach {
                actions.add(GuidedAction.Builder(activity)
                        .id(ACTION_SELECT.toLong())
                        .title(it.name)
                        .description(null)
                        .build()
                )
            }

        actions.add(GuidedAction.Builder(activity)
                .id(ACTION_BACK.toLong())
                .title(R.string.goback)
                .description(null)
                .build()
        )
    }

    fun imageReader(root: File): ArrayList<File> {
        val fileList: ArrayList<File> = ArrayList()
        val listAllFiles = root.listFiles()

        if (listAllFiles != null && listAllFiles.isNotEmpty()) {
            for (currentFile in listAllFiles) {
                if (currentFile.name.endsWith(".jpeg") || currentFile.name.endsWith(".jpg") || currentFile.name.endsWith(".png")) {
                    // File absolute path
                    if (BuildConfig.DEBUG) Log.d("downloadFilePath", currentFile.getAbsolutePath())
                    // File Name
                    if (BuildConfig.DEBUG) Log.d("downloadFileName", currentFile.getName())
                    fileList.add(currentFile.absoluteFile)
                }
            }
            if (BuildConfig.DEBUG) Log.w("fileList", "" + fileList.size)
        }
        return fileList
    }

    protected fun makeRequest(activity: Activity) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                500)
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        val context = requireContext()
        when (action.id.toInt()) {
            ACTION_SELECT -> {
                val name = action.title
                val file = File(Environment.getExternalStorageDirectory(), name.toString())
                if (file.canRead())
                    setWallpaper(context, file.path.toString())
                else
                    Toast.makeText(context, activity!!.getString(R.string.file_no_access), Toast.LENGTH_LONG).show()
                fragmentManager!!.popBackStack()
            }
            ACTION_BACK -> fragmentManager!!.popBackStack()
            else -> {
            }
        }
    }

    private fun setWallpaper(context: Context?, image: String): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        if (!image.isEmpty()) pref.edit().putString("wallpaper_image", image).apply()
        // refresh home
        val activity = requireActivity()
        val Broadcast = Intent(MainActivity::class.java.name) // ACTION
        Broadcast.putExtra("RefreshHome", true)
        activity.sendBroadcast(Broadcast)
        return true
    }

    private fun getWallpaperDesc(context: Context?): String? {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val image = pref.getString("wallpaper_image", "")
        return if (image!!.isNotBlank()) {
            image
        } else {
            getString(R.string.wallpaper_choose)
        }
    }

    companion object {
        private const val TAG = "LegacyFileListFragment"

        /* Action ID definition */
        private const val ACTION_SELECT = 1
        private const val ACTION_BACK = 2
    }
}