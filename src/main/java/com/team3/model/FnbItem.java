package com.team3.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 식음료(F&B) 주문 및 예약 정보 모델
 * @since 2025-11-29
 */
public class FnbItem {

    // 식사 유형 (SFR-801)
    public enum MealType {
        BREAKFAST("조식"), LUNCH("중식"), DINNER("석식"), 
        SNACK("간식"), DRINK("음료"), ALCOHOL("주류");
        
        private final String label;
        MealType(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    // 서비스 유형 (SFR-804: 레스토랑/룸서비스)
    public enum ServiceType {
        RESTAURANT("레스토랑"), ROOM_SERVICE("룸서비스");
        
        private final String label;
        ServiceType(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    // 결제 수단 (SFR-802, 803)
    public enum PaymentMethod {
        ROOM_CHARGE("객실 청구"), 
        CREDIT_CARD("신용카드"), 
        CASH("현금"), 
        CHECK("수표"); // 수표 추가됨
        
        private final String label;
        PaymentMethod(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private String id;              // 고유 ID
    private String roomId;          // 객실 번호 (객실 청구 시 필수)
    private String customerName;    // 고객명
    
    private ServiceType serviceType;// 레스토랑 or 룸서비스
    private MealType mealType;      // 조식/중식/석식
    private String menuName;        // 주문 항목 (예: 스테이크, 와인)
    
    private int price;              // 단가
    private int count;              // 수량
    private int totalAmount;        // 총액 (price * count)
    
    private PaymentMethod paymentMethod; // 결제 방식
    private String orderTime;       // 주문/예약 일시
    private String createdAt;       // 기록 생성일

    public FnbItem() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now().toString();
    }

    public FnbItem(String roomId, String customerName, ServiceType serviceType, MealType mealType, 
                   String menuName, int price, int count, PaymentMethod paymentMethod) {
        this();
        this.roomId = roomId;
        this.customerName = customerName;
        this.serviceType = serviceType;
        this.mealType = mealType;
        this.menuName = menuName;
        this.price = price;
        this.count = count;
        this.totalAmount = price * count;
        this.paymentMethod = paymentMethod;
        this.orderTime = LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }
    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getOrderTime() { return orderTime; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }
    public String getCreatedAt() { return createdAt; }
}