package com.tad.www.api.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tad.www.api.auth.entity.Mail;

@Repository
public interface MailRepository extends JpaRepository<Mail, Long> {

}
