package com.darkona.aardvark.repository;

import com.darkona.aardvark.domain.Phone;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PhoneRepository extends CrudRepository<Phone, UUID> {
}
