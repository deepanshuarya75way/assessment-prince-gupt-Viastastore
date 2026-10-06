package com.project.Viastastore.Model;
import jakarta.persistence.*;

@Entity
@Table(name="region-discount")
public class RegionDiscount{
  @id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;
  @column (nullable=false)
  private String region;
   @column (nullable=false)
  private Double discountPercentage;
   @column (nullable=false)
  private Boolean active=true;
  private RegionDiscount(){
    public Long getId(){

    }
    public void setId(Long id){

    }
    public  String getRegion(){

    }
    public void String setRegion(String region){

    }
    public Double getDiscountPercentage(){
     
    }
    public void setDiscountPercentage(Double discountPercentage){
      this.discountPercentage=discountPercentage;
    }
    public Boolean getActive(){
      return active;
    }
    public void setActive(Boolean active){
      this.active=active;
    }
    
  }

}