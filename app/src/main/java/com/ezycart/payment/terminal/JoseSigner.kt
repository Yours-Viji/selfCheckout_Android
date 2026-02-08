package com.ezycart.payment.terminal

import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import java.security.PrivateKey

class JoseSigner(private val privateKey: PrivateKey) {

    fun sign(payload: String): String {
        val jws = JsonWebSignature()
        jws.payload = payload
        jws.key = privateKey
        jws.algorithmHeaderValue = AlgorithmIdentifiers.RSA_USING_SHA256
        jws.setHeader("typ", "JWT")
        return jws.compactSerialization
    }
}
