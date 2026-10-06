package com.project.Viastastore.Controller;
import com.project.Viastastore.Model.RegionDiscount;
import com.project.Viastastore.Service.RegionDiscountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/region-discount")
public class RegionDiscountController{
  private final RegionDiscountService service;
  public RegionDiscountController(RegionDiscountService service){
    this.service=service;

  }
  @GetMapping("/region")
  public List<RegionDiscount> getAllDiscount(){
    return service.getAllDiscount();
  }
   @GetMapping("/price")
  public double getDiscountPrice(@RequestParam double price, @RequestParam String region){
    return service.calculateDiscountPrice(price,region);
  }

@PostMapping
public RegionDiscount addDiscount(@RequestBody RegionDiscount discount){
  return service.saveDiscount(discount);
}
@PutMapping("/{id}")
public RegionDiscount addDiscount(@RequestVariable Long id, @RequestBody RegionDiscount discount){
  return service.updateDiscount(id,discount);
}

@DeleteDiscount("/{id}")
public String deleteDiscount(@PathVariable Long id){
  service.deleteDiscount(id);
  return "Discount deleted Sucessfully ";
}



}