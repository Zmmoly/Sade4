package com.awab.ai

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var requestButton: Button
    private lateinit var accessibilityButton: Button
    private lateinit var scrollView: ScrollView

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val BACKGROUND_LOCATION_REQUEST_CODE = 1002
        
        // الأذونات الأساسية
        private val ALL_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.ADD_VOICEMAIL,
            Manifest.permission.USE_SIP,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_WAP_PUSH,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )

        // Android 12+ (API 31)
        private val ANDROID_12_PERMISSIONS = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        // Android 13+ (API 33)
        private val ANDROID_13_PERMISSIONS = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.BODY_SENSORS_BACKGROUND
        )

        // Android 14+ (API 34)
        private val ANDROID_14_PERMISSIONS = arrayOf(
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.statusTextView)
        requestButton = findViewById(R.id.requestButton)
        accessibilityButton = findViewById(R.id.accessibilityButton)
        scrollView = findViewById(R.id.scrollView)

        requestButton.setOnClickListener { requestAllPermissions() }
        accessibilityButton.setOnClickListener { openAccessibilitySettings() }

        updatePermissionStatus()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun openAccessibilitySettings() {
        if (isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "خدمة إمكانية الوصول مفعلة بالفعل! ✓", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "يرجى تفعيل خدمة Permissions App", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val serviceName = "$packageName/${MyAccessibilityService::class.java.name}"
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(serviceName) == true
    }

    private fun requestAllPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // الأذونات الأساسية
        ALL_PERMISSIONS.forEach { permission ->
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        // Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ANDROID_12_PERMISSIONS.forEach { permission ->
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission)
                }
            }
        }

        // Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ANDROID_13_PERMISSIONS.forEach { permission ->
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission)
                }
            }
        }

        // Android 14+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ANDROID_14_PERMISSIONS.forEach { permission ->
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            // إذا كانت كل الأذونات ممنوحة، تحقق من Background Location
            checkBackgroundLocationPermission()
        }
    }

    private fun checkBackgroundLocationPermission() {
        // Android 10+ يتطلب طلب ACCESS_BACKGROUND_LOCATION بشكل منفصل
        // Android 15 يشدد هذا المتطلب أكثر
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hasFineLocation = ContextCompat.checkSelfPermission(
                this, 
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            
            val hasBackgroundLocation = ContextCompat.checkSelfPermission(
                this, 
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFineLocation && !hasBackgroundLocation) {
                // Android 15: يجب توضيح السبب قبل طلب Background Location
                Toast.makeText(
                    this, 
                    "الآن سيتم طلب إذن الموقع في الخلفية بشكل منفصل", 
                    Toast.LENGTH_LONG
                ).show()
                
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_REQUEST_CODE
                )
            } else {
                Toast.makeText(this, "جميع الأذونات ممنوحة بالفعل!", Toast.LENGTH_SHORT).show()
                updatePermissionStatus()
            }
        } else {
            Toast.makeText(this, "جميع الأذونات ممنوحة بالفعل!", Toast.LENGTH_SHORT).show()
            updatePermissionStatus()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                val granted = grantResults.count { it == PackageManager.PERMISSION_GRANTED }
                val denied = grantResults.count { it == PackageManager.PERMISSION_DENIED }

                Toast.makeText(this, "تم منح $granted إذن، رُفض $denied إذن", Toast.LENGTH_LONG).show()
                
                // بعد منح الأذونات الأساسية، تحقق من Background Location
                checkBackgroundLocationPermission()
            }
            BACKGROUND_LOCATION_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty() && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                
                if (granted) {
                    Toast.makeText(this, "تم منح إذن الموقع في الخلفية! ✓", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "تم رفض إذن الموقع في الخلفية", Toast.LENGTH_SHORT).show()
                }
                updatePermissionStatus()
            }
        }
    }

    private fun updatePermissionStatus() {
        val status = buildString {
            append("حالة الأذونات:\n\n")

            var totalGranted = 0
            var totalDenied = 0

            // الأذونات الأساسية
            append("=== الأذونات الأساسية ===\n")
            ALL_PERMISSIONS.forEach { permission ->
                val isGranted = ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_GRANTED
                val permissionName = permission.substringAfterLast('.')
                append("$permissionName: ${if (isGranted) "✓ ممنوح" else "✗ مرفوض"}\n")
                if (isGranted) totalGranted++ else totalDenied++
            }

            // Background Location (Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                append("\n=== إذن الموقع في الخلفية (Android 10+) ===\n")
                val isGranted = ContextCompat.checkSelfPermission(
                    this@MainActivity, 
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                append("ACCESS_BACKGROUND_LOCATION: ${if (isGranted) "✓ ممنوح" else "✗ مرفوض"}\n")
                if (isGranted) totalGranted++ else totalDenied++
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    append("ملاحظة: Android 15 يتطلب توضيح سبب استخدام الموقع في الخلفية\n")
                }
            }

            // Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                append("\n=== أذونات Android 12+ ===\n")
                ANDROID_12_PERMISSIONS.forEach { permission ->
                    val isGranted = ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_GRANTED
                    val permissionName = permission.substringAfterLast('.')
                    append("$permissionName: ${if (isGranted) "✓ ممنوح" else "✗ مرفوض"}\n")
                    if (isGranted) totalGranted++ else totalDenied++
                }
            }

            // Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                append("\n=== أذونات Android 13+ ===\n")
                ANDROID_13_PERMISSIONS.forEach { permission ->
                    val isGranted = ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_GRANTED
                    val permissionName = permission.substringAfterLast('.')
                    append("$permissionName: ${if (isGranted) "✓ ممنوح" else "✗ مرفوض"}\n")
                    if (isGranted) totalGranted++ else totalDenied++
                }
            }

            // Android 14+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                append("\n=== أذونات Android 14+ ===\n")
                ANDROID_14_PERMISSIONS.forEach { permission ->
                    val isGranted = ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_GRANTED
                    val permissionName = permission.substringAfterLast('.')
                    append("$permissionName: ${if (isGranted) "✓ ممنوح" else "✗ مرفوض"}\n")
                    if (isGranted) totalGranted++ else totalDenied++
                }
            }

            // Android 15 Specific Notes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                append("\n=== ملاحظات Android 15 ===\n")
                append("✓ متطلبات Background Location أكثر صرامة\n")
                append("✓ تحسينات الخصوصية للوسائط\n")
                append("✓ Private Space مفعل\n")
            }

            append("\n=== الإجمالي ===\n")
            append("إجمالي الممنوحة: $totalGranted\n")
            append("إجمالي المرفوضة: $totalDenied\n")
            append("نسخة Android: ${Build.VERSION.SDK_INT}")
            
            // عرض اسم الإصدار
            val versionName = when {
                Build.VERSION.SDK_INT >= 35 -> " (Android 15 - Vanilla Ice Cream)"
                Build.VERSION.SDK_INT >= 34 -> " (Android 14 - Upside Down Cake)"
                Build.VERSION.SDK_INT >= 33 -> " (Android 13 - Tiramisu)"
                Build.VERSION.SDK_INT >= 31 -> " (Android 12)"
                else -> ""
            }
            append(versionName)
            append("\n")

            append("\n=== خدمة إمكانية الوصول ===\n")
            append("الحالة: ${if (isAccessibilityServiceEnabled()) "✓ مفعلة" else "✗ غير مفعلة"}\n")
        }

        statusTextView.text = status
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_UP) }
    }
}
