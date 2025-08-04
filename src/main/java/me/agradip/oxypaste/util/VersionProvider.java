package me.agradip.oxypaste.util;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class VersionProvider {

    private final BuildProperties buildProperties;

    public VersionProvider(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public String getVersion() {
        return buildProperties.getVersion();
    }
}

