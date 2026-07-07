package com.itech;

import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

public class EncryptedDataSourceFactory extends DataSourceFactory implements ObjectFactory {

    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable<?, ?> environment) throws Exception {

        javax.naming.Reference ref = (javax.naming.Reference) obj;

        for (int i = 0; i < ref.size(); i++) {
            javax.naming.RefAddr addr = ref.get(i);
            if ("password".equals(addr.getType())) {
                String raw = (String) addr.getContent();
                if (raw != null && raw.startsWith(ENC_PREFIX) && raw.endsWith(ENC_SUFFIX)) {
                    String cipherText = raw.substring(ENC_PREFIX.length(), raw.length() - ENC_SUFFIX.length());
                    String decrypted = decrypt(cipherText);

                    ref.remove(i);
                    ref.add(new javax.naming.StringRefAddr("password", decrypted));
                }
                break;
            }
        }

        return super.getObjectInstance(ref, name, nameCtx, environment);
    }

    private String decrypt(String cipherText) {
        String masterPassword = System.getProperty("master.password");
        if (masterPassword == null || masterPassword.isEmpty()) {
            throw new IllegalStateException(
                    "master.password JVM 옵션이 설정되지 않았습니다. setenv.sh를 확인하세요.");
        }

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(masterPassword);
        config.setAlgorithm("PBEWithHMACSHA512AndAES_256");
        config.setKeyObtentionIterations("1000");
        config.setProviderName("SunJCE");
        config.setIvGenerator(new org.jasypt.iv.RandomIvGenerator());
        encryptor.setConfig(config);

        return encryptor.decrypt(cipherText);
    }
}