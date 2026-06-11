package com.wondealer.repository;

import com.wondealer.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermsRepository extends JpaRepository<Terms, Long> {

    //DataInitializer 여기에 저장된 약관 중 isRequired가 true 인것만 골라오기 위함
    List<Terms> findByIsRequiredTrue();
}





