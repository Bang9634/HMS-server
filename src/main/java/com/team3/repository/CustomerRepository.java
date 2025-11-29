package com.team3.repository;

import java.util.List;
import java.util.Optional;
import com.team3.model.Customer;

public interface CustomerRepository {
    boolean save(Customer customer);
    List<Customer> findAll();
    
    // SFR-703: 성(이름) 또는 객실 번호로 조회
    List<Customer> findByName(String name);
    List<Customer> findByRoomNumber(String roomNumber);
    
    boolean deleteById(String id);
}