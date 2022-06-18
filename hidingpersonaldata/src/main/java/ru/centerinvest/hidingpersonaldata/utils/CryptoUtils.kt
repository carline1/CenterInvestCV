package ru.centerinvest.hidingpersonaldata.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import java.security.SecureRandom


//class CryptoUtils(private val context: Context) {
//
//    private var keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
//
//    init {
//        keyStore.load(null)
//        if (!keyStore.containsAlias(KEY_ALIAS)) {
//            // Generate a key pair for encryption
//            val start: Calendar = Calendar.getInstance()
//            val end: Calendar = Calendar.getInstance()
//            end.add(Calendar.YEAR, 30)
//            val spec = KeyPairGeneratorSpec.Builder(context)
//                .setAlias(KEY_ALIAS)
//                .setSubject(X500Principal("CN=$KEY_ALIAS"))
//                .setSerialNumber(BigInteger.TEN)
//                .setStartDate(start.time)
//                .setEndDate(end.time)
//                .build()
//            val kpg: KeyPairGenerator =
//                KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
//            kpg.initialize(spec)
//            kpg.generateKeyPair()
//        }
//
//        fun rsaEncrypt(secret: ByteArray): ByteArray {
//            val privateKeyEntry: KeyStore.PrivateKeyEntry =
//                keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
//            // Encrypt the text
//            val inputCipher =
//                Cipher.getInstance(RSA_MODE, "AndroidOpenSSL")
//            inputCipher.init(
//                Cipher.ENCRYPT_MODE,
//                privateKeyEntry.certificate.publicKey
//            )
//            val outputStream = ByteArrayOutputStream()
//            val cipherOutputStream = CipherOutputStream(outputStream, inputCipher)
//            cipherOutputStream.write(secret)
//            cipherOutputStream.close()
//            return outputStream.toByteArray()
//        }
//
//        val pref = context.getSharedPreferences(SHARED_PREFENCE_NAME, Context.MODE_PRIVATE)
//        var enryptedKeyB64 = pref.getString(ENCRYPTED_KEY, null)
//        if (enryptedKeyB64 == null) {
//            val key = ByteArray(16)
//            val secureRandom = SecureRandom()
//            secureRandom.nextBytes(key)
//            val encryptedKey: ByteArray = rsaEncrypt(key)
//            enryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT)
//            val edit = pref.edit()
//            edit.putString(ENCRYPTED_KEY, enryptedKeyB64)
//            edit.apply()
//        }
//    }
//
//    private fun rsaDecrypt(encrypted: ByteArray): ByteArray {
//        val privateKeyEntry: KeyStore.PrivateKeyEntry =
//            keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
//        val output = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL")
//        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.privateKey)
//        val cipherInputStream = CipherInputStream(
//            ByteArrayInputStream(encrypted), output
//        )
//        val values = ArrayList<Any>()
//        var nextByte: Int
//        while (cipherInputStream.read().also { nextByte = it } != -1) {
//            values.add(nextByte.toByte())
//        }
//        val bytes = ByteArray(values.size)
//        for (i in bytes.indices) {
//            bytes[i] = values[i] as Byte
//        }
//        return bytes
//    }
//
//    private fun getSecretKey(context: Context): Key {
//        val pref = context.getSharedPreferences(SHARED_PREFENCE_NAME, Context.MODE_PRIVATE)
//        val enryptedKeyB64 = pref.getString(ENCRYPTED_KEY, null)
//        // need to check null, omitted here
//        val encryptedKey: ByteArray = Base64.decode(enryptedKeyB64, Base64.DEFAULT)
//        val key: ByteArray = rsaDecrypt(encryptedKey)
//        return SecretKeySpec(key, "AES")
//    }
//
//    fun encrypt(context: Context, input: ByteArray?): String {
//        val c: Cipher = Cipher.getInstance(AES_MODE, "BC")
//        c.init(Cipher.ENCRYPT_MODE, getSecretKey(context))
//        val encodedBytes: ByteArray = c.doFinal(input)
//        return Base64.encodeToString(encodedBytes, Base64.DEFAULT)
//    }
//
//
//    fun decrypt(context: Context, encrypted: ByteArray?): ByteArray? {
//        val c: Cipher = Cipher.getInstance(AES_MODE, "BC")
//        c.init(Cipher.DECRYPT_MODE, getSecretKey(context))
//        return c.doFinal(encrypted)
//    }
//
//    companion object {
//        const val SHARED_PREFENCE_NAME = "SHARED_PREFENCE_NAME"
//        const val ANDROID_KEYSTORE = "ANDROID_KEYSTORE"
//        const val KEY_ALIAS = "KEY_ALIAS"
//        const val ENCRYPTED_KEY = "ENCRYPTED_KEY"
//        private const val RSA_MODE = "RSA/ECB/PKCS1Padding"
//        private const val AES_MODE = "AES/ECB/PKCS7Padding"
//    }
//
//}