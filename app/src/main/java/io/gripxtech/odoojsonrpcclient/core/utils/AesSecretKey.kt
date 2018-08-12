package io.gripxtech.odoojsonrpcclient.core.utils

import android.util.Base64
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


private const val algorithm = "AES"
private const val transformation = "AES/CBC/PKCS5Padding"
private const val ivSize = 16

/**
 * A 32 characters long token key
 */
private const val tokenKey = "fqJfdzGDvfwbedsKSUGty3VZ9taXxMVw"

fun String.encryptAES(): String {
    val tokenBytes = tokenKey.toByteArray(Charsets.UTF_8)
    val secretKey = SecretKeySpec(tokenBytes, algorithm)

    val ivByteArray = ByteArray(ivSize)
    val iv = IvParameterSpec(ivByteArray)

    val cipher = Cipher.getInstance(transformation)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)

    val cipherText = cipher.doFinal(toByteArray(Charsets.UTF_8))
    val ivAndCipherText = getCombinedArray(ivByteArray, cipherText)

    return Base64.encodeToString(ivAndCipherText, Base64.NO_WRAP)
}

fun String.decryptAES(): String {
    val tokenBytes = tokenKey.toByteArray(Charsets.UTF_8)
    val secretKey = SecretKeySpec(tokenBytes, algorithm)

    val ivAndCipherText = Base64.decode(this, Base64.NO_WRAP)
    val cipherText = Arrays.copyOfRange(ivAndCipherText, ivSize, ivAndCipherText.size)

    val ivByteArray = Arrays.copyOfRange(ivAndCipherText, 0, ivSize)
    val iv = IvParameterSpec(ivByteArray)

    val cipher = Cipher.getInstance(transformation)
    cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)

    return cipher.doFinal(cipherText).toString(Charsets.UTF_8)
}

private fun getCombinedArray(one: ByteArray, two: ByteArray): ByteArray {
    val combined = ByteArray(one.size + two.size)
    for (i in combined.indices) {
        combined[i] = if (i < one.size) one[i] else two[i - one.size]
    }
    return combined
}