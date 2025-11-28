package com.team3.repository;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.team3.model.Customer;
import com.team3.util.JsonFileManager;

public class JsonCustomerRepository implements CustomerRepository {
    private static final Logger logger = LoggerFactory.getLogger(JsonCustomerRepository.class);
    private final JsonFileManager<Customer> fileManager;

    public JsonCustomerRepository(JsonFileManager<Customer> fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public boolean save(Customer customer) {
        List<Customer> list = fileManager.readAll();
        list.add(customer);
        fileManager.writeAll(list);
        logger.info("고객 저장 완료: {}", customer.getName());
        return true;
    }

    @Override
    public List<Customer> findAll() {
        return fileManager.readAll();
    }

    @Override
    public List<Customer> findByName(String name) {
        return fileManager.readAll().stream()
                .filter(c -> c.getName().contains(name)) // 부분 일치 검색 허용
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> findByRoomNumber(String roomNumber) {
        return fileManager.readAll().stream()
                .filter(c -> c.getRoomNumber().equals(roomNumber))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String id) {
        List<Customer> list = fileManager.readAll();
        boolean removed = list.removeIf(c -> c.getId().equals(id));
        if (removed) {
            fileManager.writeAll(list);
            return true;
        }
        return false;
    }
}