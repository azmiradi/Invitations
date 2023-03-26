import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.awt.print.PrinterJob
import javax.print.attribute.HashPrintRequestAttributeSet
import java.awt.print.Printable
import java.awt.print.PageFormat
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.awt.print.Printable.NO_SUCH_PAGE
import java.awt.print.Printable.PAGE_EXISTS
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.print.attribute.standard.*

class Printer {
    fun printDocument(qrCodeContent:String) {
        val qrCodeSize = 50
        val documentWidth = 200
        val documentHeight = 300
        val margin = 10

        // Create a new QR Code writer with the appropriate encoding hints
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L, EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val qrCodeWriter = QRCodeWriter()

        // Generate the QR Code matrix
        val bitMatrix = qrCodeWriter.encode(qrCodeContent, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints)

        // Create a new BufferedImage with the appropriate dimensions
        val imageWidth = bitMatrix.width
        val imageHeight = bitMatrix.height
        val image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)

        // Draw the QR Code onto the BufferedImage
        for (x in 0 until imageWidth) {
            for (y in 0 until imageHeight) {
                val color = if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                image.setRGB(x, y, color)
            }
        }

        // Create a new BufferedImage for the document with the appropriate dimensions
        val documentImageWidth = documentWidth - margin * 2
        val documentImageHeight = documentHeight - margin * 2 - qrCodeSize
        val documentImage = BufferedImage(documentImageWidth, documentImageHeight, BufferedImage.TYPE_INT_RGB)

        // Draw the QR Code onto the document BufferedImage with the appropriate margin
        val graphics = documentImage.createGraphics()
        graphics.drawImage(image, margin, documentImageHeight + margin, null)
        graphics.dispose()

        // Convert the document BufferedImage to a ByteArray
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(documentImage, "png", outputStream)
        val documentBytes = outputStream.toByteArray()

        // Get the list of available printers
        val printServices = PrinterJob.lookupPrintServices()

        // Select the first available printer
        val printService = printServices.firstOrNull()

        // If a printer is available, print the document
        if (printService != null) {
            val printRequestAttributeSet = HashPrintRequestAttributeSet().apply {
                // Set the print quality to high
                add(PrintQuality.HIGH)

                // Set the paper size to A4
                add(MediaSizeName.ISO_A4)

                // Set the printable area to include a margin of 10mm on all sides
                add(
                    MediaPrintableArea(
                        margin.toFloat(),
                        margin.toFloat(),
                        (documentWidth - margin * 2).toFloat(),
                        (documentHeight - margin * 2).toFloat(),
                        MediaPrintableArea.MM
                    )
                )

                // Set the orientation to portrait
                add(OrientationRequested.PORTRAIT)

                // Set the number of copies to 1
                add(Copies(1))

                // Set the duplex mode to simplex
                add(Sides.ONE_SIDED)

                // Set the printer name
                add(PrinterName(printService.name, null))
            }

            // Create a new PrinterJob and set the print service
            val printerJob = PrinterJob.getPrinterJob()
            printerJob.printService = printService

            // Set the document as the Printable and print it
            val printable = object : Printable {
                override fun print(graphics: Graphics, pageFormat: PageFormat, pageIndex: Int): Int {
                    if (pageIndex != 0) {
                        return NO_SUCH_PAGE
                    }

                    graphics.drawImage(ImageIO.read(ByteArrayInputStream(documentBytes)), 0, 0, null)

                    return PAGE_EXISTS
                }
            }
            printerJob.setPrintable(printable)

            try {
                printerJob.print(printRequestAttributeSet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            println("No printers available.")
        }
    }
}
