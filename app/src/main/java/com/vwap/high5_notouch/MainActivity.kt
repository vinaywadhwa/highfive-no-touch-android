package com.vwap.high5_notouch

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.vstechlab.easyfonts.EasyFonts
import java.io.File
import java.net.URLConnection
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {
    private lateinit var animationView: LottieAnimationView
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var createdBy: TextView
    private lateinit var root: View
    private lateinit var mGravity: FloatArray
    private var lastColorIndex = -1
    private val backgroundColors: Array<String> =
        arrayOf(
            "#97D8C4",
            "#F4B942",
            "#3490dc"
        )


    private var mAccel: Float = 0f
    private var mAccelCurrent: Float = 0f
    private var mAccelLast: Float = 0f
    private var lastSoundPlayedTimestamp: Long = 0
    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0;

    private val SENSOR_SENSITIVITY = 8

    private var mProximity: Sensor? = null
    private var mSensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSensor()
        animationView = findViewById(R.id.animation_view)
        root = findViewById(R.id.root)
        title = findViewById(R.id.title)
        subtitle = findViewById(R.id.subtitle)
        title.setOnClickListener { share() }
        subtitle.setOnClickListener { share() }
        createdBy = findViewById(R.id.created_by)
        createdBy.setOnClickListener { twitter() }
        title.typeface = EasyFonts.captureIt2(this)
        subtitle.typeface = EasyFonts.captureIt(this)
        createdBy.typeface = EasyFonts.caviarDreams(this)

    }

    private fun twitter() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/vinayw"))
        startActivity(browserIntent)
    }

    private fun initSensor() {
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        mProximity = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mAccelCurrent = SensorManager.GRAVITY_EARTH
        mAccelLast = SensorManager.GRAVITY_EARTH
    }

    private var listener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

        }

        override fun onSensorChanged(p0: SensorEvent) {
            mGravity = p0.values.clone()
            val x: Float = mGravity[0]
            val y: Float = mGravity[1]
            val z: Float = mGravity[2]
            mAccelLast = mAccelCurrent
            mAccelCurrent = sqrt(x * x + y * y + z * z)
            val delta = mAccelCurrent - mAccelLast
            mAccel = mAccel * 0.9f + delta
            if (mAccel > SENSOR_SENSITIVITY && highFiveSoundDue()) {
                playSound()
                updateLastPlayedTimestamp()
                startAnimation()
            }
        }
    }

    private fun changeBackgroundColor() {
        lastColorIndex = (lastColorIndex + 1) % backgroundColors.size
        root.setBackgroundColor(Color.parseColor(backgroundColors[lastColorIndex]))
    }

    private fun highFiveSoundDue() = System.currentTimeMillis() - lastSoundPlayedTimestamp > 1000

    private fun updateLastPlayedTimestamp() {
        lastSoundPlayedTimestamp = System.currentTimeMillis()

    }

    override fun onPause() {
        super.onPause()
        deinitSound()
        unregisterProximitySensor()
        stopAllAnimations()
    }

    private fun stopAllAnimations() {
        animationView.cancelAnimation()
    }

    override fun onResume() {
        super.onResume()
        initSound()
        registerProximitySensor()
        startAnticipationAnimation()
    }

    private fun startAnticipationAnimation() {
        animationView.cancelAnimation()
        animationView.setMinAndMaxProgress(0f, 0.1f)
        animationView.speed = 0.1f
        animationView.repeatCount = LottieDrawable.INFINITE
        animationView.repeatMode = LottieDrawable.REVERSE
        animationView.playAnimation()
    }

    private fun startAnimation() {
        animationView.cancelAnimation()
        animationView.setMinAndMaxProgress(0f, 1f)
        animationView.speed = 2f
        animationView.repeatCount = 0
        animationView.playAnimation()
        animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                startAnticipationAnimation()
                changeBackgroundColor()
                animationView.removeAllAnimatorListeners()
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }

        })

    }

    private fun registerProximitySensor() {
        mSensorManager?.registerListener(listener, mProximity, SensorManager.SENSOR_DELAY_UI)
    }

    private fun unregisterProximitySensor() {
        mSensorManager?.unregisterListener(listener)
    }

    private fun initSound() {
        soundPool = SoundPool(5, AudioManager.STREAM_MUSIC, 0)
        soundId = soundPool.load(this, R.raw.highfive_sound, 1)
    }

    private fun deinitSound() {
        soundPool.stop(soundId)
        soundPool.release()
    }

    private fun playSound() {
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    private fun share() {
        var fileToShare = File("${filesDir}/shared/highfive-notouch.mp4")
        Log.d("SHARE", "Looking for file to share - $fileToShare")
        if (!fileToShare.exists()) {
            Log.d("SHARE", "fileToShare = false, copying assets")
            AssetManager.exportAssets(this)
            fileToShare = File("${filesDir}/shared/highfive-notouch.mp4")
        } else {
            Log.d("SHARE", "fileToShare = exists, not copying assets")
        }
        val imageUri: Uri = FileProvider.getUriForFile(this, packageName, fileToShare)
//        var drawable = R.drawable.feature_graphic
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND

        val url = "https://play.google.com/store/apps/details?id=com.vwap.hi5"
        val text =
            "Virtual high-fives are the new hand shakes!\n\nHigh five ✋ with others by waving \uD83D\uDC4B your phone at them.\n"
        val hashtags = "#StaySafe #HighFiveNoTouch"
        var type = URLConnection.guessContentTypeFromName(imageUri.toString())
        Log.d("SHARE", "type = $type")
        val sharingIntent = Intent(Intent.ACTION_SEND).apply {
            setDataAndType(imageUri, type)
            putExtra(Intent.EXTRA_SUBJECT, text)
            putExtra(Intent.EXTRA_TEXT, "$text\n$hashtags\n$url")
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(sharingIntent, "Share"))
    }

}
