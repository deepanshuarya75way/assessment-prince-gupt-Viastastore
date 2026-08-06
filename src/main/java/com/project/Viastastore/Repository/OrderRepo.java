package com.project.Viastastore.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.Viastastore.Model.Orders;
import com.project.Viastastore.Model.Users;

public interface OrderRepo extends JpaRepository<Orders, Long> {

	List<Orders> findAllByUser(Users user);
	Orders findByOrderId(String orderId);
	List<Orders> findAllByOrderStatus(Orders.OrderStatus orderStatus);

}
