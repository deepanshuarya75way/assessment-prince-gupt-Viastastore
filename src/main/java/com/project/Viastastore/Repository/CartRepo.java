package com.project.Viastastore.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.Viastastore.Model.Cart;
import com.project.Viastastore.Model.Products;
import com.project.Viastastore.Model.Users;

public interface CartRepo extends JpaRepository<Cart, Long> {

	boolean existsByUserAndProduct(Users user, Products product);
	List<Cart> findAllByUser(Users user);
	void deleteByUserAndProduct(Users user, Products product);

}
