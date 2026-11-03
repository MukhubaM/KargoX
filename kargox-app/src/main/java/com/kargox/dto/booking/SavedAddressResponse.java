package com.kargox.dto.booking;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedAddressResponse {

    private Long id;
    private String label;
    private String formattedAddress;
    private Double latitude;
    private Double longitude;
}
