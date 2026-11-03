package com.kargox.controller;

import com.kargox.dto.booking.SavedAddressRequest;
import com.kargox.dto.booking.SavedAddressResponse;
import com.kargox.dto.common.ApiResponse;
import com.kargox.entity.Address;
import com.kargox.entity.User;
import com.kargox.exception.ResourceNotFoundException;
import com.kargox.repository.AddressRepository;
import com.kargox.repository.UserRepository;
import com.kargox.security.SecurityUser;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Saved Addresses", description = "Customer saved pickup/destination addresses")
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<SavedAddressResponse>> create(@AuthenticationPrincipal
                                                                    SecurityUser securityUser,
                                                                    @Valid @RequestBody
                                                                    SavedAddressRequest request) {

        User user = userRepository.findById(securityUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = new Address();
        address.setUser(user);
        address.setLabel(request.getLabel());
        address.setFormattedAddress(request.getFormattedAddress());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        addressRepository.save(address);

        return ResponseEntity.ok(ApiResponse.ok("Address saved", toResponse(address)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavedAddressResponse>>> myAddresses(@AuthenticationPrincipal SecurityUser user) {

        List<SavedAddressResponse> list = addressRepository.findByUserIdAndDeletedFalse(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal
                                                    SecurityUser user,
                                                    @PathVariable
                                                    Long id) {

        Address address = addressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Address not found");
        }
        address.setDeleted(true);
        addressRepository.save(address);

        return ResponseEntity.ok(ApiResponse.ok("Address removed", null));
    }

    private SavedAddressResponse toResponse(Address a) {

        SavedAddressResponse r = new SavedAddressResponse();
        r.setId(a.getId());
        r.setLabel(a.getLabel());
        r.setFormattedAddress(a.getFormattedAddress());
        r.setLatitude(a.getLatitude());
        r.setLongitude(a.getLongitude());

        return r;
    }
}
