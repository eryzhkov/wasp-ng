package ru.vsu.uic.wasp.ng.core.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "git")
@Validated
@Getter
public class GitInfoProperties {

    @NotBlank
    private final String commitIdAbbrev;
    @NotBlank
    private final String branch;
    @NotBlank
    private final String buildTime;
    @NotBlank
    private final String buildVersion;
    private final String tags;

    @ConstructorBinding
    public GitInfoProperties(String commitIdAbbrev, String branch, String buildTime, String buildVersion, String tags) {
        this.commitIdAbbrev = commitIdAbbrev;
        this.branch = branch;
        this.buildTime = buildTime;
        this.buildVersion = buildVersion;
        this.tags = tags;
    }

}
