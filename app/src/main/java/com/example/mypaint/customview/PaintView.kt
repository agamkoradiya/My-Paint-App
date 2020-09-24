package com.example.mypaint.customview

import android.R.attr.path
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.mypaint.model.FingerPath
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.math.abs
import kotlin.random.Random


class PaintView : View {

    private lateinit var fos: OutputStream
    private lateinit var imageUri: Uri

    companion object {
        const val BRUSH_SIZE = 5
        const val DEFAULT_COLOR: Int = Color.BLACK
        const val DEFAULT_BG_COLOR: Int = Color.WHITE
        private const val TOUCH_TOLERANCE = 0f
    }

    private var mX: Float = 0f
    private var mY: Float = 0f
    private var mPath: Path? = null
    private var mPaint: Paint? = Paint()
    private val paths: MutableList<FingerPath> = mutableListOf()
    private var currentColor = 0
    private var backgroundColors: Int = DEFAULT_BG_COLOR
    private var strokeWidth = 0
    private var blur = false
    private var mBlur: MaskFilter? = null
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private val mBitmapPaint: Paint = Paint(Paint.DITHER_FLAG)


    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mPaint!!.isAntiAlias = true
        mPaint!!.isDither = true
        mPaint!!.color = DEFAULT_COLOR
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeJoin = Paint.Join.ROUND
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mPaint!!.xfermode = null
        mPaint!!.alpha = 0xff
    }

    fun init(metrics: DisplayMetrics) {
        val height: Int = metrics.heightPixels
        val width: Int = metrics.widthPixels

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)

        currentColor = DEFAULT_COLOR
        strokeWidth = BRUSH_SIZE
    }

    fun setColor(colorCode: String) {
        currentColor = Color.parseColor(colorCode)
    }

    fun setBrushSize(brushSize: Int) {
        strokeWidth = brushSize
    }

    fun setEraser(brushSize: Int) {
        blur = false
        strokeWidth = brushSize
        currentColor = DEFAULT_BG_COLOR
    }

    fun normal() {
        blur = false
        currentColor = DEFAULT_COLOR
        strokeWidth = BRUSH_SIZE
    }

    fun doBlur(trueFalse: Boolean) {
        blur = trueFalse
    }

    fun blurEffect(effect: BlurMaskFilter.Blur) {
        mBlur = BlurMaskFilter(strokeWidth.toFloat(), effect)
    }

    fun undo() {
        val size = paths.size
        if (size >= 1) {
            paths.removeAt(size - 1)
            invalidate()
        }
    }

    fun clear() {
        backgroundColors = DEFAULT_BG_COLOR
        paths.clear()
        normal()
        invalidate()
    }

    fun download(context: Context) {
        val randomNum = Random.nextInt().toString()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d("PaintView", "download: - >Q")
            val resolver: ContentResolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "myPaint$randomNum.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "jpg")
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            )
            imageUri =
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)!!
            fos = resolver.openOutputStream(imageUri)!!
            Log.d("TAG", "createPdf: $fos")
            try {
                mBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, fos)
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
            }
        } else {
            Log.d("PaintView", "download: - <Q")

            val path = Environment.getExternalStorageDirectory().toString()
            val dir = File("$path/Download/")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File("$path/Download/", "myPaint$randomNum.jpg")

            if (!file.exists()) {
                file.createNewFile()
            }
            try {
                mBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(file))
                Log.d("PaintView", "saved")
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.save()
        mCanvas?.drawColor(backgroundColors)

        for (fp in paths) {
            mPaint?.color = fp.color
            mPaint?.strokeWidth = fp.strokeWidth.toFloat()
            mPaint?.maskFilter = null
            if (fp.blur) {
                mPaint?.maskFilter = fp.blurEffect
            }
            mCanvas?.drawPath(fp.path, mPaint!!)
        }
        canvas?.drawBitmap(mBitmap!!, 0f, 0f, mBitmapPaint)
        canvas?.restore()
    }

    private fun touchStart(x: Float, y: Float) {
        mPath = Path()
        val fp = FingerPath(currentColor, blur, mBlur, strokeWidth, mPath!!)
        paths.add(fp)
        mPath!!.reset()
        mPath!!.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = abs(x - mX)
        val dy = abs(y - mY)

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touchUp() {
        mPath!!.lineTo(mX, mY)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }
}