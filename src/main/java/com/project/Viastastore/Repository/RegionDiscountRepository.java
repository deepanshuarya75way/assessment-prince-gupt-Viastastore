package com.project.Viastastore.Repository;
import com.project.Model.RegionDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionDiscountRepository extends JpaRepository<RegionDiscount, Long>{
  Optional<RegionDiscount>findAllByRegionIgnoreCaseAndActiveTrue(String region);
}