package com.immortals.miniurl.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UrlShortenerDto {

    @NotBlank(message = "Original URL cannot be empty")
    @Pattern(regexp = "^(https?://).+", message = "Original URL must start with http:// or https://")
    private String originalUrl;

    private Boolean customAlias;
    private Boolean premiumUser;
    private Boolean highThroughput;
    private Boolean needsDeterminism;
    private Boolean internalTool;

    @Size(max = 30, message = "Alias must be 0–30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Alias can only contain alphanumeric characters, dashes, or underscores")
    private String customAliasName;

    @Size(max = 255, message = "Note must be under 255 characters")
    private String note;

    @Size(max = 20, message = "tags must contain at most 20 elements")
    private List<@Size(max = 30, message = "Each tag must be under 30 characters") String> tags;

    private Long amount;

    @Pattern(regexp = "^(seconds|minutes|hours|Days|Months)$", message = "unitTime must be seconds, minutes, or hours")
    private String unitTime;
}
