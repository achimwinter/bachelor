package com.example.bachelor.smime

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.bachelor.api.GrpcClient
import com.example.bachelor.signal.SessionGenerator
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.ExtensionsGenerator
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import org.whispersystems.libsignal.protocol.SignalMessage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.*
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.security.auth.x500.X500Principal
import kotlin.collections.HashMap


class KeyTool {

    private val principalName =
        "C=DE,ST=Bayern,L=Nuremberg,O=adorsys,OU=it,CN=adorsys.de/emailAddress=acw@adorsys.de"
    private val keyAlias = "bachelor"
    private val keyStoreProvider = "AndroidKeyStore"

    fun getKeyPair(): KeyPair? {
        if (doesKeyPairExist()) {
            val privateKeyEntry =
                getKeyStoreInstance().getEntry("bachelor", null) as KeyStore.PrivateKeyEntry
            return KeyPair(privateKeyEntry.certificate.publicKey, privateKeyEntry.privateKey)
        }
        return null
    }

    fun generateKeyPair() {
        if (doesKeyPairExist()) {
            return
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
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)

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
                val keypair = it.genKeyPair()
                val csr = generateCSR(keypair)

                val signingRequest = csr?.encoded
                val encryptedPublicKey =
                    SessionGenerator.sessionCipher.encrypt(keypair.public.encoded).serialize()

                val encryptedSignedCertificate =
                    GrpcClient.instance.signCertificate(signingRequest, encryptedPublicKey)


                val encryptedCertificate =
                    SignalMessage(encryptedSignedCertificate.x509Certificate.toByteArray())
                val certificate = SessionGenerator.sessionCipher.decrypt(encryptedCertificate)

                val certFactory = CertificateFactory.getInstance("X.509")
                val inputStream = ByteArrayInputStream(certificate)
                val signedCertificate =
                    certFactory.generateCertificate(inputStream) as X509Certificate
                inputStream.close()

                replaceCertificate(signedCertificate)
            }
    }

    fun replaceCertificate(certificate: X509Certificate) {
        if (doesKeyPairExist()) {
            getKeyStoreInstance().setCertificateEntry("signed", certificate)
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

    fun getKeyStoreInstance(): KeyStore {
        val keyStore = KeyStore.getInstance(keyStoreProvider)

        keyStore.load(null)

        return keyStore
    }

    /*
        Helper class to create the Certificate Signing Request (CSR)
     */
    private class JCESigner(privateKey: PrivateKey, signatureAlgorithm: String) : ContentSigner {

        val algorithms = HashMap<String, AlgorithmIdentifier>()
        val mAlgo = signatureAlgorithm.toLowerCase(Locale.ENGLISH)
        val outputStream = ByteArrayOutputStream()
        val signature = Signature.getInstance(signatureAlgorithm)

        init {
            algorithms["SHA512withRSA".toLowerCase(Locale.ENGLISH)] =
                DefaultSignatureAlgorithmIdentifierFinder().find("SHA512withRSA")
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

