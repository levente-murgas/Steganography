package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.pow

fun decToBin(byte: Byte) : String {
    var quotient = byte.toInt()
    var remainder = ""
    while (quotient != 0) {
        if (quotient % 2 == 0){
            remainder += "0"
        }
        else{
            remainder += "1"
        }
        quotient /= 2
    }
    while(remainder.length != 8){
        remainder += "0"
    }
    return remainder.reversed()
}

fun byteToBinArray(byteArray: ByteArray) : String {
    var bitArray = ""
    for (i in byteArray.indices) {
        bitArray += decToBin(byteArray[i])
    }
    return bitArray
}

fun binToDec(binString: String) : Byte {
    var dec = 0.0
    val two = 2.0
    for (i in 0 until binString.length) {
        if (binString[i] == '1') {
            dec += two.pow(binString.length - i - 1)
        }
    }
    return dec.toInt().toByte()
}

fun equalLengths(messageSize: Int, pwd: String) : String {
    var equalLengthPwd = pwd
    if (messageSize > pwd.length) { //making the password length equal to message length
        val times = messageSize / pwd.length
        val extra = messageSize % pwd.length
        repeat(times - 1){ //egyszer már eleve benne van
            equalLengthPwd += pwd
        }
        for (i in 0 until extra) {
            equalLengthPwd += pwd[i]
        }
    }
    return equalLengthPwd
}

fun xorLongBinary(v1: String, v2: String) : String{
    var ret = ""
    for (i in 0 until v1.length) {
        if (v1[i] == v2[i]) {
            ret += "0"
        }
        else {
            ret += "1"
        }
    }
    return ret
}

fun encrypt(message: String) : String{
    val byteArray = message.encodeToByteArray()
    println("Password:")
    val pwd = readln()
    val equalLengthPwd = equalLengths(message.length,pwd)

    val pwdByteArray = equalLengthPwd.encodeToByteArray()

    return xorLongBinary(byteToBinArray(pwdByteArray), byteToBinArray(byteArray))
}

fun binToByteArray(bitArray: String) : ByteArray {
    var bitWord = ""
    var byteArray = byteArrayOf()
    for (i in 0 until bitArray.length) {
        bitWord += bitArray[i]
        if (bitWord.length == 8) {
            byteArray += binToDec(bitWord)
            bitWord = ""
        }
    }
    return byteArray
}

fun decrypt(encryptedByteArray: ByteArray, pwd: String) : ByteArray {
    val equalLengthPwd = equalLengths(encryptedByteArray.size,pwd)
    val pwdByteArray = equalLengthPwd.encodeToByteArray()

    val decryptedBitArray = xorLongBinary(byteToBinArray(pwdByteArray), byteToBinArray(encryptedByteArray))

    return binToByteArray(decryptedBitArray)
}

fun hideMessage(){
    println("Input image file:")
    val inputName = readln()
    println("Output image file:")
    val outputName = readln()
    val inputFile = File(inputName)
    if(!inputFile.exists()){
        println("Can't read input file!")
        return
    }
    println("Message to hide:")
    val message = readln()
    var bitArray = encrypt(message)
    bitArray += "00000000"
    bitArray += "00000000"
    bitArray += "00000011"


    val myImage: BufferedImage = ImageIO.read(inputFile)

    if (myImage.width * myImage.height < bitArray.length) {
        println("The input image is not large enough to hold this message.")
        return
    }

    // myImage.width is the image width
    // myImage.height is the image height
    loop@for (y in 0 until myImage.height) {               // For every row.
        for (x in 0 until myImage.width) {          // For every column.
            val color = Color(myImage.getRGB(x, y))  // Read color from the (x, y) position

            val r = color.red
            val g = color.green              // Access the Green color value
            val b = color.blue               // Access the Blue color value

            var calcPosition = y * myImage.width + x

            if (calcPosition < bitArray.length) {
                    var newBlue = if (b % 2 == 0) {
                    if (bitArray[calcPosition] == '0') {
                        b //páros és 0 kód, nem kell változtatni
                    } else {
                        b + 1
                    }
                } else {
                    if (bitArray[calcPosition] == '0') {
                        b - 1
                    } else {
                        b //páratlan és 1 kód, nem kell változtatni
                    }
                }
                val colorNew = Color(r, g,newBlue)  //  the least significant bit for each color (Red, Green, and Blue) is set to 1.
                myImage.setRGB(x, y, colorNew.rgb)  // Set the new color at the (x, y) position
            }
            else {
                break@loop
            }
        }
    }
    val outputFileJpg = File(outputName)  // Output the file
    ImageIO.write(myImage, "png", outputFileJpg)  // Create an image using the BufferedImage instance data
    println("Message saved in $outputName image.\n")
}

fun showMessage(){
    println("Input image file:")
    val inputName = readln()
    val inputFile = File(inputName)
    if(!inputFile.exists()){
        println("Can't read input file!")
        return
    }

    println("Password:")
    val pwd = readln()

    val myImage: BufferedImage = ImageIO.read(inputFile)
    var bitWord: String = ""
    var byteArray = byteArrayOf()
    // myImage.width is the image width
    // myImage.height is the image height
    loop@for (y in 0 until myImage.height) {               // For every row.
        for (x in 0 until myImage.width) {          // For every column.
            val color = Color(myImage.getRGB(x, y))  // Read color from the (x, y) position
            val b = color.blue               // Access the Blue color value
            bitWord += if (b % 2 == 0) "0" else "1"
            if (bitWord.length == 8) {
                byteArray += binToDec(bitWord)
                bitWord = ""
                var messageString  = byteArray.toString()
                if (messageString.length >= 3){
                    var last3 : String = "${messageString[messageString.length - 3]}${messageString[messageString.length - 2]}${messageString[messageString.length - 1]}"
                    if (last3 == "003") {
                        break@loop
                    }
                }
            }
        }
    }
    var result = byteArrayOf()
    for (i in 0 until byteArray.size - 3) {
        result += byteArray[i]
    }

    result = decrypt(result,pwd)

    println("Message:")
    println(result.toString(Charsets.UTF_8))

}



fun main() {

    while(true) {
        println("Task (hide, show, exit):")
        var command = readln()
        when(command){
            "hide" -> {
                hideMessage()
            }

            "show" -> {
                showMessage()
            }
            "exit" -> {
                println("Bye!")
                break
            }

            else -> {
                println("Wrong task: $command")
            }
        }
    }
}

