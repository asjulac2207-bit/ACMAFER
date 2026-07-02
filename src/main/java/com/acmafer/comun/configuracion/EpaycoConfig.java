package com.acmafer.comun.configuracion;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "epayco")
@Getter
@Setter
public class EpaycoConfig {
    private String publicKey;
    private String privateKey;
    private String customerId;
    private String pKey;
    private boolean test;
    private String responseUrl;
    private String confirmationUrl;
}