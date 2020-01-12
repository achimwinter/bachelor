package com.example.bachelor.smime

import android.R.id
import android.security.keystore.KeyProperties
import org.bouncycastle.asn1.bsi.BSIObjectIdentifiers.algorithm
import java.security.SecureRandom
import java.security.Signature
import javax.crypto.Cipher


class SmimeUtils {

    val ENCRYPTION_ALG = KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
    val SIGNATURE_ALG = KeyProperties.SIGNATURE_PADDING_RSA_PKCS1

    fun decrypt(encryptedMessage: ByteArray): ByteArray {
        val keypair = KeyTool().getKeyPair()
        val cipher = Cipher.getInstance(ENCRYPTION_ALG)
        cipher.init(Cipher.DECRYPT_MODE, keypair?.private)

        return cipher.doFinal(encryptedMessage)
    }

    fun encrypt(message: ByteArray): ByteArray {
        val keypair = KeyTool().getKeyPair()
        val cipher = Cipher.getInstance(ENCRYPTION_ALG)
        cipher.init(Cipher.ENCRYPT_MODE, keypair?.private)

        return cipher.doFinal(message)
    }

    fun sign(message: ByteArray): ByteArray {
        val keypair = KeyTool().getKeyPair()
        val signatureInstance = Signature.getInstance(SIGNATURE_ALG)
        signatureInstance.initSign(keypair?.private, SecureRandom())
        signatureInstance.update(message)
        return signatureInstance.sign()
    }

    // Would require other Public Keys
//    fun verify(publicKeyAlias: String, message: ByteArray): ByteArray {
//        val instance =
//            Signature.getInstance(SIGNATURE_ALG)
//        instance.initVerify(publicKey(alias))
//        instance.update(id.message.getBytes("UTF-8"))
//        return instance.verify(signature)
//    }
}