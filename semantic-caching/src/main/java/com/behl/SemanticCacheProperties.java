package com.behl;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "com.behl.semantic.cache")
record SemanticCacheProperties(

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "1.0", inclusive = true)
    Double similarityThreshold,

    @NotBlank
    String contentField,

    @NotBlank
    String embeddingField,

    @NotBlank
    String metadataField

) {
}