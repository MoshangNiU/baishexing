package com.yunlan.config;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 微信支付通知 AES-256-GCM 解密工具
 *
 * 微信支付回调的 resource.ciphertext 是用 APIv3Key 作为 AES-256-GCM 密钥加密的。
 * 解密后得到支付结果的明文 JSON。
 */
public class WechatPayDecryptor {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BITS = 128;

    /**
     * 解密微信支付回调密文
     *
     * @param apiV3Key     APIv3密钥（32字节字符串，配置在 application.yml）
     * @param associatedData 附加数据（resource.associated_data）
     * @param nonce        随机串（resource.nonce）
     * @param ciphertext   密文（resource.ciphertext，Base64编码）
     * @return 解密后的明文JSON
     */
    public static String decrypt(String apiV3Key, String associatedData, String nonce, String ciphertext) {
        try {
            byte[] keyBytes = apiV3Key.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length != 32) {
                throw new IllegalArgumentException("APIv3Key must be 32 bytes, got " + keyBytes.length);
            }

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            byte[] cipherBytes = Base64.getDecoder().decode(ciphertext);
            GCMParameterSpec paramSpec = new GCMParameterSpec(TAG_LENGTH_BITS, nonce.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
            if (associatedData != null && !associatedData.isEmpty()) {
                cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            }
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("微信支付回调解密失败", e);
        }
    }
}
