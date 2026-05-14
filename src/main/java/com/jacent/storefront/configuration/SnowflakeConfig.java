package com.jacent.storefront.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.snowflake.client.jdbc.SnowflakeBasicDataSource;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.io.FileReader;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(SnowflakeProperties.class)
public class SnowflakeConfig {

    private final SnowflakeProperties props;

    public SnowflakeConfig(SnowflakeProperties props) {
        this.props = props;
    }

//    @Bean
//    public DataSource snowflakeDataSource() throws Exception {
//        PrivateKey privateKey = loadPrivateKey(props.getPrivateKeyFile(), props.getPrivateKeyPassphrase());
//
//        Properties jdbcProps = new Properties();
//        jdbcProps.put("user", props.getUser());
//        jdbcProps.put("privateKey", privateKey);          // ✅ PrivateKey object
//        jdbcProps.put("db", props.getDatabase());
//        jdbcProps.put("schema", props.getSchema());
//        jdbcProps.put("warehouse", props.getWarehouse());
//        jdbcProps.put("role", props.getRole());
//
//        // ✅ Use HikariCP — pass PrivateKey via connectionInitSql workaround
//        // Actually simplest: anonymous DataSource implementation
//        String url = props.getUrl();
//        return new AbstractDataSource() {
//            @Override
//            public Connection getConnection() throws SQLException {
//                return DriverManager.getConnection(url, jdbcProps);
//            }
//
//            @Override
//            public Connection getConnection(String username, String password) throws SQLException {
//                return DriverManager.getConnection(url, jdbcProps);
//            }
//        };
//    }

    @Bean
    public DataSource snowflakeDataSource() throws Exception {
        PrivateKey privateKey = loadPrivateKey(props.getPrivateKeyFile(), props.getPrivateKeyPassphrase());

        // Build underlying Snowflake DataSource
        SnowflakeBasicDataSource snowflakeDS = new SnowflakeBasicDataSource();
        snowflakeDS.setUrl(props.getUrl());
        snowflakeDS.setUser(props.getUser());
        snowflakeDS.setPrivateKey(privateKey);
        snowflakeDS.setDatabaseName(props.getDatabase());
        snowflakeDS.setSchema(props.getSchema());
        snowflakeDS.setWarehouse(props.getWarehouse());
        snowflakeDS.setRole(props.getRole());

        // Wrap with HikariCP
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSource(snowflakeDS);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(hikariConfig);
    }

    private PrivateKey loadPrivateKey(String keyFile, String passphrase) throws Exception {
        BouncyCastleProvider provider = new BouncyCastleProvider();

        PEMParser pemParser = new PEMParser(new FileReader(Paths.get(keyFile).toFile()));
        Object pemObject = pemParser.readObject();
        pemParser.close();

        PrivateKeyInfo privateKeyInfo = null;

        if (pemObject instanceof PKCS8EncryptedPrivateKeyInfo) {
            PKCS8EncryptedPrivateKeyInfo encryptedKey = (PKCS8EncryptedPrivateKeyInfo) pemObject;

            // ✅ Pass provider instance directly — NOT by name "BC"
            InputDecryptorProvider decryptor = new JceOpenSSLPKCS8DecryptorProviderBuilder()
                    .setProvider(provider)                    // ✅ instance, not "BC"
                    .build(passphrase.toCharArray());

            privateKeyInfo = encryptedKey.decryptPrivateKeyInfo(decryptor);

        } else if (pemObject instanceof PrivateKeyInfo) {
            privateKeyInfo = (PrivateKeyInfo) pemObject;
        }

        // ✅ Pass provider instance directly — NOT by name "BC"
        return new JcaPEMKeyConverter()
                .setProvider(provider)                        // ✅ instance, not "BC"
                .getPrivateKey(privateKeyInfo);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}