package com.team3.repository;

import com.team3.model.Payment;
import com.team3.util.JsonFileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * [JSON 결제 저장소 구현체]
 * 인터페이스(PaymentRepository)의 내용을 실제로 실행
 * JsonFileManager라는 도구를 이용해서 실제로 'payments.json' 파일에 R/W함.
 */
public class JsonPaymentRepository implements PaymentRepository {

    // 파일을 다루는 매니저 (Main에서 만들어진 걸 받아옴)
    private final JsonFileManager<Payment> fileManager;

    // 생성자: Main.java에서 넣어주는 fileManager를 받아서 내 변수에 넣음. (의존성 주입)
    public JsonPaymentRepository(JsonFileManager<Payment> fileManager) {
        this.fileManager = fileManager;
    }

    /**
     * 결제 정보를 파일에 저장하는 로직
     * 1. 파일에서 기존 목록을 다 읽어온다.
     * 2. 새 결제 정보를 목록 끝에 추가한다.
     * 3. 목록 전체를 다시 파일에 덮어쓴다.
     */
    @Override
    public void save(Payment payment) {
        // 1. 기존 데이터 로딩
        List<Payment> payments = fileManager.readAll();
        
        // 파일이 없거나 비어있으면 새 리스트 생성
        if (payments == null) {
            payments = new ArrayList<>();
        }
        
        // 2. 리스트에 데이터 추가
        payments.add(payment);
        
        // 3. 파일 저장
        fileManager.writeAll(payments);
    }

    /**
     * 파일에 있는 모든 결제 내역을 읽어오는 로직
     */
    @Override
    public List<Payment> findAll() {
        List<Payment> payments = fileManager.readAll();
        
        // 데이터가 있으면 반환, 없으면(null) 빈 리스트 반환
        return payments != null ? payments : new ArrayList<>();
    }
    
    /*
    * 파일에 있는 모든 결제 내역 삭제
    */
    @Override
    public void deleteAll(){
        // 빈 리스트 덮어씌워서 파일 내용 전체 삭제
        fileManager.writeAll(new ArrayList<>());
    }

}

