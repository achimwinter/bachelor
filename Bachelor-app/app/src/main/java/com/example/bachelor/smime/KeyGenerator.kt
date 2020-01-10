package com.example.bachelor.smime

import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.ExtensionsGenerator
import org.bouncycastle.cms.CMSAttributeTableGenerator.DIGEST
import org.bouncycastle.jcajce.provider.digest.SHA3
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.*
import java.util.*
import javax.security.auth.x500.X500Principal
import kotlin.collections.HashMap


class KeyGenerator {

    private val principalName =
        "C=DE,ST=Bayern,L=Nuremberg,O=adorsys,OU=it,CN=adorsys.de/emailAddress=acw@adorsys.de"
    private val keyAlias = "bachelor"
    private val keyStoreProvider = "AndroidKeyStore"

    fun generateKeyPair(): KeyPair {

        if (doesKeyPairExist()) {
            val privateKeyEntry = getKeyStoreInstance().getEntry("bachelor", null) as KeyStore.PrivateKeyEntry
            return KeyPair(privateKeyEntry.certificate.publicKey, privateKeyEntry.privateKey)
        }

        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 5)

        KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            keyStoreProvider
        )
            .apply {
                val certBuilder = KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or
                            KeyProperties.PURPOSE_DECRYPT or
                            KeyProperties.PURPOSE_VERIFY or
                            KeyProperties.PURPOSE_SIGN
                )
                        // Disabled, Emulator doesnt support biometric
//                    .setUserAuthenticationRequired(true)
                    .setKeySize(4096)
                    .setKeyValidityEnd(end.time)
                    .setKeyValidityStart(start.time)
                    .setDigests(KeyProperties.DIGEST_SHA512)
                    .setCertificateSerialNumber(BigInteger.ONE)
                    .setCertificateSubject(X500Principal(principalName))
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    initialize(
                        certBuilder
//                            .setIsStrongBoxBacked(true)
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
        val csrBuilder =
            JcaPKCS10CertificationRequestBuilder(X500Principal(principalName), keyPair.public)

        val extensionsGenerator = ExtensionsGenerator()
        extensionsGenerator.addExtension(
            Extension.basicConstraints, true, BasicConstraints(
                true
            )
        )

        csrBuilder.addAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
            extensionsGenerator.generate()
        )

        return csrBuilder.build(signer)
    }

    private fun doesKeyPairExist(): Boolean {
        return getKeyStoreInstance().getKey(keyAlias, null) != null
    }

    private fun getKeyStoreInstance(): KeyStore {
        val keyStore = KeyStore.getInstance(keyStoreProvider)

        keyStore.load(null)

        return keyStore
    }


    private class JCESigner(privateKey: PrivateKey, signatureAlgorithm: String) : ContentSigner {

        val algorithms = HashMap<String, AlgorithmIdentifier>()
        val mAlgo = signatureAlgorithm.toLowerCase(Locale.ENGLISH)
        val outputStream = ByteArrayOutputStream()
        val signature = Signature.getInstance(signatureAlgorithm)

        init {
            algorithms["SHA512withRSA".toLowerCase(Locale.ENGLISH)] =
                AlgorithmIdentifier(ASN1ObjectIdentifier("1.2.840.113549.1.1.11"))
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

