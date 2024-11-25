package com.behl.imagegen;

record ImageGenerationRequest(
    String prompt,
    String username,
    Integer height,
    Integer width
) {}