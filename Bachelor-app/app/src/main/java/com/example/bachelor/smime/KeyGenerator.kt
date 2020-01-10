package com.example.bachelor.smime

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Log
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.ExtensionsGenerator
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Signature
import java.util.*
import javax.security.auth.x500.X500Principal
import kotlin.collections.HashMap


class KeyGenerator {

    private val principalName = "C=DE,ST=Bayern,L=Nuremberg,O=adorsys,OU=it,CN=adorsys.de/emailAddress=acw@adorsys.de"
    val keyAlias = "bachelor"

    fun generateKeyPair(): KeyPair {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 5)

        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA,
            "AndroidKeyStore")
            .apply {
                val certBuilder = KeyGenParameterSpec.Builder(keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or
                            KeyProperties.PURPOSE_DECRYPT or
                            KeyProperties.PURPOSE_VERIFY or
                            KeyProperties.PURPOSE_SIGN)
                    .setKeyValidityStart(start.time)
                    .setKeyValidityEnd(end.time)
                    .setCertificateSerialNumber(BigInteger.ONE)
                    .setCertificateSubject(X500Principal("CN=adorsys"))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    initialize(
                        certBuilder
                            .setIsStrongBoxBacked(true)
                            .build()
                    )
                } else {
                    initialize(certBuilder.build())
                }
            }
            .also {
                return it.genKeyPair()
            }
    }

    fun generateCSR(keyPair: KeyPair): PKCS10CertificationRequest? {
        val signer = JCESigner(keyPair.private, "SHA512withRSA")
        val csrBuilder = JcaPKCS10CertificationRequestBuilder(X500Principal(principalName), keyPair.public)

        val extensionsGenerator = ExtensionsGenerator()
        extensionsGenerator.addExtension(
            Extension.basicConstraints, true, BasicConstraints(
                true)
        )

        csrBuilder.addAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
            extensionsGenerator.generate())

        return csrBuilder.build(signer)
    }



    private class JCESigner(privateKey: PrivateKey, signatureAlgorithm: String): ContentSigner {

        val algorithms = HashMap<String, AlgorithmIdentifier>()
        val mAlgo = signatureAlgorithm.toLowerCase(Locale.ENGLISH)
        val outputStream = ByteArrayOutputStream()
        val signature = Signature.getInstance(signatureAlgorithm)

        init {
            algorithms["SHA512withRSA".toLowerCase(Locale.ENGLISH)] = AlgorithmIdentifier(ASN1ObjectIdentifier("1.2.840.113549.1.1.11"))
            signature.initSign(privateKey)
        }


        override fun getAlgorithmIdentifier(): AlgorithmIdentifier? {
            return algorithms[mAlgo]
        }

        override fun getOutputStream(): OutputStream {
            return outputStream
        }

        override fun getSignature(): ByteArray {
            signature.update(outputStream.toByteArray())
            return signature.sign()
        }

    }
}

