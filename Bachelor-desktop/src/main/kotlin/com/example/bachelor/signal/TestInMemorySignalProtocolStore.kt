package com.example.bachelor.signal

import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore
import org.whispersystems.libsignal.util.KeyHelper

class TestInMemorySignalProtocolStore(identityKeyPair: IdentityKeyPair?, registrationId: Int) : InMemorySignalProtocolStore(identityKeyPair, registrationId) {

    init {
        InMemorySignalProtocolStore(generateIdentityKeyPair(), generateRegistrationId())
    }
    private fun generateIdentityKeyPair(): IdentityKeyPair {
        val identityKeyPairKeys = Curve.generateKeyPair()
        return IdentityKeyPair(IdentityKey(identityKeyPairKeys.publicKey),
                identityKeyPairKeys.privateKey)
    }

    private fun generateRegistrationId(): Int {
        return KeyHelper.generateRegistrationId(false)
    }
}