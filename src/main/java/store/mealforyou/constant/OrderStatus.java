package store.mealforyou.constant;

public enum OrderStatus {
    // 주문 완료
    ORDERED,

    // 배송 중
    SHIPPING,

    // 배송 완료
    DELIVERED,

    // 회원 탈퇴로 인해 유효하지 않은 주문
    DELETED;
}