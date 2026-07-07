package com.itech;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

public class EncryptedHikariFactory implements ObjectFactory {

    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable<?, ?> environment) throws Exception {

        Reference ref = (Reference) obj;
        HikariConfig config = new HikariConfig();

        for (int i = 0; i < ref.size(); i++) {
            RefAddr addr = ref.get(i);
            String value = (String) addr.getContent();
            if (value == null) {
                continue;
            }

            switch (addr.getType()) {
                case "driverClassName":
                    config.setDriverClassName(value);
                    break;
                case "url":
                    config.setJdbcUrl(value);
                    break;
                case "username":
                    config.setUsername(value);
                    break;
                case "password":
                    if (value.startsWith(ENC_PREFIX) && value.endsWith(ENC_SUFFIX)) {
                        String cipherText = value.substring(ENC_PREFIX.length(), value.length() - ENC_SUFFIX.length());
                        config.setPassword(decrypt(cipherText));
                    } else {
                        config.setPassword(value);
                    }
                    break;
                default:
                    break;
            }
        }

        return new HikariDataSource(config);
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
