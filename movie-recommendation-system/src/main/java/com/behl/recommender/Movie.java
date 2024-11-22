package com.behl.recommender;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Movie(
    @JsonProperty("Title") String title,
    @JsonProperty("Year") String year,
    @JsonProperty("Language") String language,
    @JsonProperty("Plot") String plot
) {}