package com.example.crafteria_server.domain.ads.repository;

import com.example.crafteria_server.domain.ads.entity.AdvertisementImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementImageRepository extends JpaRepository<AdvertisementImage, Long> {
}
