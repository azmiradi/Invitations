import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.print.PageFormat
import java.awt.print.Printable
import java.awt.print.PrinterException
import java.awt.print.PrinterJob
import java.io.IOException
import java.util.*
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.PrintRequestAttributeSet
import javax.print.attribute.standard.Copies
import javax.print.attribute.standard.MediaSizeName
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun printQRCode() = suspendCoroutine<Unit> { continuation ->
    // Set up QR code
    val qrCodeContent = "hi"
    val qrCodeSize = 100 // 2 cm
    val hintMap = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
    hintMap[EncodeHintType.CHARACTER_SET] = "UTF-8"
    val qrCodeWriter = QRCodeWriter()
    val bitMatrix = qrCodeWriter.encode(qrCodeContent, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap)
    val qrCodeImage = BufferedImage(qrCodeSize, qrCodeSize, BufferedImage.TYPE_INT_RGB)
    qrCodeImage.createGraphics().apply {
        color = Color.WHITE
        fillRect(0, 0, qrCodeSize, qrCodeSize)
        color = Color.BLACK
        for (x in 0 until qrCodeSize) {
            for (y in 0 until qrCodeSize) {
                if (bitMatrix[x, y]) {
                    drawRect(x, y, 1, 1)
                }
            }
        }
        dispose()
    }

    // Set up printer
    val pageFormat = PrinterJob.getPrinterJob().defaultPage(PageFormat())
    val paper = pageFormat.paper
    paper.setSize(18.8 * 72.0, 13.5 * 72.0) // Set paper size to 18.8 cm x 13.5 cm
    paper.setImageableArea(72.0, 72.0, 18.8 * 72.0 - 2 * 72.0, 13.5 * 72.0 - 2 * 72.0) // Set 1 cm margins
    pageFormat.paper = paper
    pageFormat.orientation = PageFormat.PORTRAIT

    val printRequestAttributeSet = HashPrintRequestAttributeSet().apply {
        add(MediaSizeName.ISO_A4)
        add(Copies(1))
    }

    // Set up printing task
    val printable = Printable { graphics, _, pageIndex ->
        if (pageIndex != 0) {
            Printable.NO_SUCH_PAGE
        } else {
            graphics as Graphics2D
            graphics.translate(72, 72) // 1 cm margin
            graphics.font = Font("Arial", Font.PLAIN, 20)
            graphics.drawString(qrCodeContent, 0, 0)
            graphics.drawImage(qrCodeImage, 0, 60, null) // Position QR code at bottom
            Printable.PAGE_EXISTS
        }
    }

    // Print document in background thread using coroutines
    val printJob = object : Thread() {
        override fun run() {
            try {
                val printerJob = PrinterJob.getPrinterJob()
                printerJob.setPrintable(printable, pageFormat)
                printerJob.print(printRequestAttributeSet)
                continuation.resume(Unit)
            } catch (e: PrinterException) {
                continuation.resumeWithException(e)
            }
        }
    }
    printJob.start()
}
