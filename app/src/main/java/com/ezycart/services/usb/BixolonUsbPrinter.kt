package com.ezycart.services.usb


import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.content.ContextCompat
import com.bxl.config.editor.BXLConfigLoader
import jpos.JposConst
import jpos.POSPrinter
import jpos.POSPrinterConst
import jpos.JposException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL



/*class BixolonUsbPrinter(private val context: Context) {

    private val TAG = "BixolonPDF"
    private val logicalName = "BK3_USB"

    // SINGLE printer instance
    private val posPrinter by lazy {
        ensureJposXml(context)
        POSPrinter(context.applicationContext)
    }

    // Prevent concurrent printing
    private val printLock = Any()

    *//** Call ONCE (ex: app start) *//*
    fun configure(): Boolean {
        return try {
            val loader = BXLConfigLoader(context)
            try {
                loader.openFile()
            } catch (e: Exception) {
                loader.newFile()
            }

            loader.removeEntry(logicalName)
            loader.addEntry(
                logicalName,
                BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                BXLConfigLoader.PRODUCT_NAME_BK3_3,
                BXLConfigLoader.DEVICE_BUS_USB,
                ""
            )
            loader.saveFile()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Config error", e)
            false
        }
    }

    *//** PUBLIC print API *//*
    fun printPdfAsBitmap(pdfFile: File) {
        if (!pdfFile.exists()) {
            Log.e(TAG, "PDF not found")
            return
        }

        synchronized(printLock) {
            try {
                posPrinter.open(logicalName)
                posPrinter.claim(5000)
                posPrinter.deviceEnabled = true

                val bitmaps = renderPdfToBitmaps(pdfFile)

                for (bitmap in bitmaps) {
                    posPrinter.printBitmap(
                        POSPrinterConst.PTR_S_RECEIPT,
                        bitmap,
                        POSPrinterConst.PTR_BM_CENTER,
                        576
                    )
                }

                // Feed + Full Cut
                posPrinter.printNormal(
                    POSPrinterConst.PTR_S_RECEIPT,
                    "\u001b|3lF\u001b|fP"
                )

            } catch (e: Exception) {
                Log.e(TAG, "Print failed", e)

            } finally {
                cleanupPrinter()
            }
        }
    }

    *//** Always release printer *//*
     fun cleanupPrinter() {
        try {
            if (posPrinter.claimed) {
                posPrinter.deviceEnabled = false
                posPrinter.release()
            }
            posPrinter.close()
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup failed", e)
        }
    }

    *//** PDF → scaled bitmap (BIG TEXT) *//*
    private fun renderPdfToBitmaps(pdfFile: File): List<Bitmap> {
        val result = mutableListOf<Bitmap>()

        val fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)

            val targetWidth = 576
            val scale = 1.9f   // 👈 TEXT SIZE CONTROL HERE

            val targetHeight = (targetWidth * page.height / page.width * scale).toInt()

            val bitmap = Bitmap.createBitmap(
                targetWidth,
                targetHeight,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            val matrix = android.graphics.Matrix().apply {
                postScale(scale, scale)
            }

            page.render(
                bitmap,
                null,
                matrix,
                PdfRenderer.Page.RENDER_MODE_FOR_PRINT
            )

            page.close()

            result.add(convertToMonochrome(bitmap))
            bitmap.recycle()
        }

        renderer.close()
        fd.close()

        return result
    }

    *//** Thermal-friendly conversion *//*
    private fun convertToMonochrome(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.RGB_565)

        for (y in 0 until src.height) {
            for (x in 0 until src.width) {
                val pixel = src.getPixel(x, y)
                val gray =
                    (0.299 * Color.red(pixel) +
                            0.587 * Color.green(pixel) +
                            0.114 * Color.blue(pixel)).toInt()

                out.setPixel(x, y, if (gray < 160) Color.BLACK else Color.WHITE)
            }
        }
        return out
    }

    private fun ensureJposXml(context: Context) {
        val file = File(context.filesDir, "jpos.xml")
        if (file.exists()) return

        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <JposEntries>
            <JposEntry logicalName="BK3_USB">
                <creation factoryClass="com.bxl.services.posprinter.POSPrinterServiceInstanceFactory"/>
                <vendor name="BIXOLON"/>
                <jpos category="POSPrinter" version="1.14"/>
                <product name="BK3-3"/>
                <prop name="deviceBus" value="USB"/>
                <prop name="deviceName" value="BK3-3"/>
            </JposEntry>
        </JposEntries>
    """.trimIndent()

        file.writeText(xml)
    }

}*/



/**
 * ARCHITECTURE FOR ANDROID BOX + USB HUB:
 * 1. Singleton pattern to avoid driver re-init collisions.
 * 2. Extended timeout for USB Hub handshake latency.
 * 3. Aggressive cleanup to prevent 'Resource Busy' on the Hub.
 */
/*
object BixolonPrinterManager {
    private const val TAG = "BixolonManager"
    private const val LOGICAL_NAME = "BK3_USB"

    private val printerSemaphore = Semaphore(1)
    private var posPrinter: POSPrinter? = null

    private const val PRINTER_WIDTH = 576 // BK3-3 Standard width


    private fun getPrinter(context: Context): POSPrinter {
        if (posPrinter == null) {
            val loader = BXLConfigLoader(context)
            try { loader.openFile() } catch (e: Exception) { loader.newFile() }
            loader.removeEntry(LOGICAL_NAME)
            loader.addEntry(LOGICAL_NAME, BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                BXLConfigLoader.PRODUCT_NAME_BK3_3, BXLConfigLoader.DEVICE_BUS_USB, "")
            loader.saveFile()

            posPrinter = POSPrinter(context.applicationContext)
        }
        return posPrinter!!
    }

    suspend fun printPdf(context: Context, pdfFile: File): Boolean = withContext(Dispatchers.IO) {
        // 1. Physical Presence Check
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val hasHardware = usbManager.deviceList.values.any { it.vendorId == 1046 }
        if (!hasHardware) {
            Log.e(TAG, "BK3-3 not detected. Check Hub connection.")
            return@withContext false
        }

        // 2. Queue Management
        if (!printerSemaphore.tryAcquire()) return@withContext false

        val printer = getPrinter(context)
        var success = false

        try {
            // 3. The 'Hub-Friendly' Connection Bridge
            var connected = false
            for (i in 1..3) {
                try {
                    if (printer.state != jpos.JposConst.JPOS_S_CLOSED) {
                        printer.close()
                    }
                    printer.open(LOGICAL_NAME)
                    // Increased to 5000ms because Hubs are slow to handshake
                    printer.claim(5000)
                    printer.deviceEnabled = true
                    connected = true
                    break
                } catch (e: Exception) {
                    Log.w(TAG, "Hub latency hit. Retry $i/3...")
                    try { printer.close() } catch (ex: Exception) {}
                    // Critical delay to let the Hub reset its port power
                    Thread.sleep(800)
                }
            }

            if (!connected) throw Exception("USB Hub failed to bridge connection")

            // 4. Print Logic
            //val bitmaps = renderPdf(pdfFile)
            val bitmaps = renderPdfToHighQualityBitmaps(pdfFile)
            for (bitmap in bitmaps) {
                printer.printBitmap(POSPrinterConst.PTR_S_RECEIPT, bitmap, POSPrinterConst.PTR_BM_CENTER, 576)
                bitmap.recycle()
            }

            printer.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\u001b|3lF\u001b|fP")
            success = true

        } catch (e: Exception) {
            Log.e(TAG, "Final Print Error: ${e.message}")
        } finally {
            // 5. Hard Reset (Mandatory for Hubs)
            try {
                if (printer.deviceEnabled) printer.deviceEnabled = false
                if (printer.claimed) printer.release()
                printer.close()
            } catch (e: Exception) {
                Log.e(TAG, "Cleanup failed")
            }
            printerSemaphore.release()
        }
        return@withContext success
    }

    private fun renderPdf(file: File): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)
        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(576, (576 * page.height / page.width), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            bitmaps.add(convertToMonochrome(bitmap))
            bitmap.recycle()
            page.close()
        }
        renderer.close()
        fd.close()
        return bitmaps
    }

    private fun convertToMonochrome(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.RGB_565)
        val canvas = Canvas(out)
        val paint = Paint()
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }


    private fun renderPdfToHighQualityBitmaps(file: File): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)

        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)

            // 1. Calculate height perfectly to avoid empty bottom space
            val aspectRatio = page.height.toFloat() / page.width.toFloat()
            val targetHeight = (PRINTER_WIDTH * aspectRatio).toInt()

            // 2. Use RGB_565 to save memory on Android Box
            val bitmap = Bitmap.createBitmap(PRINTER_WIDTH, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            // 3. High Quality Scaling Matrix
            val matrix = Matrix()
            val scaleFactor = PRINTER_WIDTH.toFloat() / page.width.toFloat()
            matrix.postScale(scaleFactor, scaleFactor)

            page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            // 4. Clean Grayscale conversion
            val grayscaleBitmap = applyThermalOptimization(bitmap, paint)
            bitmaps.add(grayscaleBitmap)

            bitmap.recycle()
            page.close()
        }
        renderer.close()
        fd.close()
        return bitmaps
    }

    private fun applyThermalOptimization(src: Bitmap, paint: Paint): Bitmap {
        val dest = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.RGB_565)
        val canvas = Canvas(dest)

        // ColorMatrix handles the "Blur" by increasing contrast
        val cm = ColorMatrix(floatArrayOf(
            2f, 0f, 0f, 0f, -100f, // Increase Red contrast
            0f, 2f, 0f, 0f, -100f, // Increase Green contrast
            0f, 0f, 2f, 0f, -100f, // Increase Blue contrast
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dest
    }
}
*/


object BixolonPrinterManager {
    private const val TAG = "BixolonManager"
    private const val LOGICAL_NAME = "BK3_USB"
    private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private const val PRINTER_WIDTH = 576 // BK3-3 Standard width

    private val printerSemaphore = Semaphore(1)
    private var posPrinter: POSPrinter? = null

    private fun getPrinter(context: Context): POSPrinter {
        if (posPrinter == null) {
            val loader = BXLConfigLoader(context)
            try { loader.openFile() } catch (e: Exception) { loader.newFile() }
            loader.removeEntry(LOGICAL_NAME)
            loader.addEntry(LOGICAL_NAME, BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                BXLConfigLoader.PRODUCT_NAME_BK3_3, BXLConfigLoader.DEVICE_BUS_USB, "")
            loader.saveFile()

            posPrinter = POSPrinter(context.applicationContext)
        }
        return posPrinter!!
    }

    /** * MAIN PRINT FUNCTION
     */
    suspend fun printPdf(context: Context, pdfFile: File): Boolean = withContext(Dispatchers.IO) {
        // 1. Physical Presence Check (Vendor 1046 = Bixolon)
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val printerDevice = usbManager.deviceList.values.find { it.vendorId == 1046 }

        if (printerDevice == null) {
            Log.e(TAG, "BK3-3 not detected. Check Hub connection.")
            return@withContext false
        }

        // 2. Queue Management
        if (!printerSemaphore.tryAcquire()) return@withContext false

        val printer = getPrinter(context)
        var success = false

        try {
            // 3. HANDSHAKE: Ensure Permission (Deep Fix for Hubs)
            if (!requestUsbPermission(context, usbManager, printerDevice)) {
                Log.e(TAG, "USB Permission Denied")
                return@withContext false
            }

            // 4. HUB-FRIENDLY CONNECTION BRIDGE
            var connected = false
            for (i in 1..3) {
                try {
                    if (printer.state != jpos.JposConst.JPOS_S_CLOSED) {
                        printer.close()
                    }
                    printer.open(LOGICAL_NAME)
                    printer.claim(5000) // Increased for Hub latency
                    printer.deviceEnabled = true
                    connected = true
                    break
                } catch (e: Exception) {
                    Log.w(TAG, "Hub latency hit. Retry $i/3...")
                    try { printer.close() } catch (ex: Exception) {}
                    Thread.sleep(800) // Recovery period
                }
            }

            if (!connected) throw Exception("USB Hub failed to bridge connection")

            // 5. HIGH QUALITY PRINTING
            val bitmaps = renderPdfToHighQualityBitmaps(pdfFile)
            for (bitmap in bitmaps) {
                printer.printBitmap(POSPrinterConst.PTR_S_RECEIPT, bitmap, POSPrinterConst.PTR_BM_CENTER, 576)
                bitmap.recycle()
            }

            // Feed and Cut (fP = Full cut immediately)
            printer.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\u001b|fP")
            success = true

        } catch (e: Exception) {
            Log.e(TAG, "Final Print Error: ${e.message}")
        } finally {
            // 6. HARD CLEANUP (Prevents Zombie Handles)
            try {
                if (printer.deviceEnabled) printer.deviceEnabled = false
                if (printer.claimed) printer.release()
                printer.close()
            } catch (e: Exception) {
                Log.e(TAG, "Cleanup failed")
            }
            printerSemaphore.release()
        }
        return@withContext success
    }

    /**
     * ASYNC PERMISSION REQUEST
     */
    private suspend fun requestUsbPermission(context: Context, manager: UsbManager, device: UsbDevice): Boolean {
        if (manager.hasPermission(device)) return true

        return suspendCancellableCoroutine { continuation ->
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    context.unregisterReceiver(this)
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

                    // Fix: Added the empty block for onCancellation to match the signature
                    continuation.resume(granted) {
                        // Optional: Code to run if the coroutine is cancelled while waiting
                    }
                }
            }

            val filter = IntentFilter(ACTION_USB_PERMISSION)
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )

            val intent = PendingIntent.getBroadcast(
                context, 0, Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_IMMUTABLE
            )

            try {
                manager.requestPermission(device, intent)
            } catch (e: Exception) {
                context.unregisterReceiver(receiver)
                continuation.resume(false) {}
            }
        }
    }

    private fun renderPdfToHighQualityBitmaps(file: File): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)

        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val aspectRatio = page.height.toFloat() / page.width.toFloat()
            val targetHeight = (PRINTER_WIDTH * aspectRatio).toInt()

            val bitmap = Bitmap.createBitmap(PRINTER_WIDTH, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            val matrix = Matrix()
            val scaleFactor = PRINTER_WIDTH.toFloat() / page.width.toFloat()
            matrix.postScale(scaleFactor, scaleFactor)

            page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            val grayscaleBitmap = applyThermalOptimization(bitmap, paint)
            bitmaps.add(grayscaleBitmap)

            bitmap.recycle()
            page.close()
        }
        renderer.close()
        fd.close()
        return bitmaps
    }

    private fun applyThermalOptimization(src: Bitmap, paint: Paint): Bitmap {
        val dest = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.RGB_565)
        val canvas = Canvas(dest)

        // Force Sharpness by boosting contrast
        val cm = ColorMatrix(floatArrayOf(
            2f, 0f, 0f, 0f, -110f,
            0f, 2f, 0f, 0f, -110f,
            0f, 0f, 2f, 0f, -110f,
            0f, 0f, 0f, 1f, 0f
        ))

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dest
    }
}


