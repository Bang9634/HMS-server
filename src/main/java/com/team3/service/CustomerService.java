package com.team3.service;

import java.time.LocalDateTime; // 시간 사용을 위해 필요
import java.util.List;
import java.util.UUID;          // ID 생성을 위해 필요
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.team3.model.Customer;
import com.team3.repository.CustomerRepository;

public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public boolean addCustomer(Customer customer) {
        // 필수 값 검증 (이름, 전화번호) - SFR-701, 702
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("고객 이름은 필수입니다.");
        }
        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }

        if (customer.getId() == null) {
            customer.setId(UUID.randomUUID().toString());
        }
        if (customer.getCreatedAt() == null) {
            customer.setCreatedAt(LocalDateTime.now().toString());
        }

        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // SFR-703: 검색 기능 (이름 또는 객실 번호)
    public List<Customer> searchCustomers(String keyword, String type) {
        if ("ROOM".equals(type)) {
            return customerRepository.findByRoomNumber(keyword);
        } else {
            return customerRepository.findByName(keyword);
        }
    }

    public boolean deleteCustomer(String id) {
        return customerRepository.deleteById(id);
    }
}