package store.mealforyou.dto;

import store.mealforyou.entity.Address;

public record AddressDTO(
        String zipCode,
        String roadAddress,
        String detailAddress
) {
    public Address toEmbeddable() {
        return Address.builder()
                .zipCode(zipCode)
                .roadAddress(roadAddress)
                .detailAddress(detailAddress)
                .build();
    }
}
