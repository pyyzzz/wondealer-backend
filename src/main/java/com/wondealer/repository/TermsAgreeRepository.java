package com.wondealer.repository;

import com.wondealer.entity.TermsAgree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermsAgreeRepository extends JpaRepository<TermsAgree, Long> {
}
