package com.example.bachelor.signal

import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore
import org.whispersystems.libsignal.util.KeyHelper


class TestInMemorySignalProtocolStore : InMemorySignalProtocolStore(identityKeypair(), registrationId())

private fun identityKeypair(): IdentityKeyPair {
    val identityKeyPairKeys = Curve.generateKeyPair()
    return IdentityKeyPair(IdentityKey(identityKeyPairKeys.publicKey),
            identityKeyPairKeys.privateKey)
}

private fun registrationId(): Int {
    return KeyHelper.generateRegistrationId(false)
}