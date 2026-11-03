package com.kargox.dto.driver;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverRegisterRequest {

    @NotBlank
    private String licenseNumber;

    private String licenseDocumentUrl;
    private String idDocumentUrl;
}
