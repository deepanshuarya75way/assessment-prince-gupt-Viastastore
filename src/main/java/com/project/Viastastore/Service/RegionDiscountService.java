package com.project.Viastastore.Service;
import com.project.Viastastore.Model.RegionDiscount;
import com.project.Repository.RegionDiscountRepository;
import org.springframework.sterotype.Service;
import java.util.List;
import java.util.Optional;

public class RegionDiscountService{
   private final RegionDiscountRepository repository;
   public RegionDiscountService(RegionDiscountRepository repository){
    this.repository=repository;
   }
   public List<RegionDiscount> getAllDiscount(){
    return repository.findAll();
   }
   public Optional<RegionDiscount> getDiscountById(Long (id)){
    return repository.findById(id);
   }
   public RegionDiscount saveDiscount(RegionDiscount discount){
    return repository.saveDiscount(discount);

   }
   public RegionDiscount updateDiscount(Long id, RegionDiscount){ 
    
     RegionDiscount existing= repository.findById(id).orElseThrow(()-> new RuntimeException("Discount not Found"));

   existing.setRegion(updateDiscount.getRegion());
   existing.setDiscountPercentage(updateDiscount.getDiscountPercentage());
   existing.setActive(updateDiscount.getActive());
   return repository.save(existing);
   }

   public void deleteDiscount(Long id){
    repository.deleteById(id);
   }

   public double getDiscountPercentage(String region){
    if(region==null || region.trim().isEmpty()){
      return 0.0;
    }
    return repository.findByRegionlIgnoreCaseAndActiveTrue(region.ontrim()).map(RegionDiscount::getDiscountPercentage).orElse(0.0);

   }
   public double calculateDiscountPrice(double originalPrice, String region){
    double discount=getDiscountPercentage(region);
    return originalPrice-(originalPrice*discount/100);
   }
}