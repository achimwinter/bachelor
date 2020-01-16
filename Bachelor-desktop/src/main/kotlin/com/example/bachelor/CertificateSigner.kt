package com.example.bachelor

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cms.CMSSignedData
import org.bouncycastle.cms.SignerId
import org.bouncycastle.cms.SignerInformationVerifier
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.crypto.params.AsymmetricKeyParameter
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMEncryptedKeyPair
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.bouncycastle.util.Selector
import org.bouncycastle.util.Store
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.math.BigInteger
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

fun verifySignature(smimeBody: ByteArray): Boolean {
    var returnFlag = true
    val signedData = CMSSignedData(smimeBody)
    val certificateStore = signedData.certificates
    val signerInformations = signedData.signerInfos.signers.iterator()

    while (signerInformations.hasNext()) {
        val signerInformation = signerInformations.next()
        val certificate = getCertificate(certificateStore, signerInformation.sid)
        val verfifier = getVerifier(certificate)
        if (!signerInformation.verify(verfifier)) {
            returnFlag = false
        }
    }
    return returnFlag
}

fun sign(inputCSR: PKCS10CertificationRequest?, requestPublicKey: PublicKey): X509Certificate? {
    val signatureAlgId: AlgorithmIdentifier = DefaultSignatureAlgorithmIdentifierFinder().find("SHA512withRSA")
    val digestAlgId: AlgorithmIdentifier = DefaultDigestAlgorithmIdentifierFinder().find(signatureAlgId)
    val caPrivate = readPrivateKey("/Users/achim/certs/ca.key", "test")
    val asymmetricKeyParameter: AsymmetricKeyParameter = PrivateKeyFactory.createKey(caPrivate?.encoded)
    val keyInfo: SubjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(requestPublicKey.encoded)

    val start = Calendar.getInstance()
    val end = Calendar.getInstance()
    end.add(Calendar.YEAR, 5)

    val myCertificateGenerator = X509v3CertificateBuilder(
            X500Name("CN=adorsys"),
            BigInteger.ONE,
            start.time,
            end.time,
            inputCSR?.subject,
            keyInfo)

    val sigGen: ContentSigner = BcRSAContentSignerBuilder(signatureAlgId, digestAlgId).build(asymmetricKeyParameter)

    val holder: X509CertificateHolder = myCertificateGenerator.build(sigGen)
    val eeX509CertificateStructure = holder.toASN1Structure()

    val cf = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME)
    // Read Certificate
    val is1: InputStream = ByteArrayInputStream(eeX509CertificateStructure.encoded)
    val certificate: X509Certificate = cf.generateCertificate(is1) as X509Certificate
    is1.close()
    File("/Users/achim/certs/clientPub.crt").writeBytes(certificate.encoded)
    return certificate
}

private fun readPrivateKey(privateKeyPath: String, password: String): PrivateKey? {
    val fileReader = FileReader(privateKeyPath)
    val keyReader = PEMParser(fileReader)

    val converter = JcaPEMKeyConverter()
    val decryptionProvider = JcePEMDecryptorProviderBuilder().build(password.toCharArray())

    val keyPair = keyReader.readObject()
    val keyInfo: PrivateKeyInfo

    keyInfo = if (keyPair is PEMEncryptedKeyPair) {
        val decryptedKeyPair = keyPair.decryptKeyPair(decryptionProvider)
        decryptedKeyPair.privateKeyInfo
    } else {
        val pemKeyPair = keyPair as PEMKeyPair
        pemKeyPair.privateKeyInfo
    }
    keyReader.close()
    return converter.getPrivateKey(keyInfo)
}

private fun getCertificate(certificates: Store<X509CertificateHolder>, signerId: SignerId): X509Certificate {
    val certificateHolder = certificates.getMatches(signerId as Selector<X509CertificateHolder>).iterator().next()
    val certificateConverter = JcaX509CertificateConverter()
    certificateConverter.setProvider(BouncyCastleProvider.PROVIDER_NAME)
    return certificateConverter.getCertificate(certificateHolder)
}

private fun getVerifier(certificate: X509Certificate): SignerInformationVerifier {
    val signerInfoVerifierBuilder = JcaSimpleSignerInfoVerifierBuilder()
    signerInfoVerifierBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME)
    return signerInfoVerifierBuilder.build(certificate)
}
