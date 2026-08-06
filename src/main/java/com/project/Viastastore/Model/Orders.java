package com.project.Viastastore.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Orders {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne
	@JoinColumn(nullable = false)
	private Products product;
	
	@ManyToOne
	@JoinColumn(nullable = false)
	private Users user;
	
	@Column(nullable = false)
	private String productName;
	private String description;
	
	@Column(name = "product_price", nullable = false)
	private double procuctPrice;
	@Column(name = "procuct_price", nullable = false)
	private double procuctPriceLegacy;
	@Column(nullable = false)
	private double discount;
	@Column(name = "final_price", nullable = false)
	private double finalprice;
	@Column(name = "finalprice", nullable = false)
	private double finalpriceLegacy;
	
	@Column(nullable = false)
	private String color;
	@Column(nullable = false)
	private String size;
	private double shippingCharge;
	@Column(nullable = false)
	private double totalAmount;
	
	@Column(nullable = false)
	private String shippingAddress;
	@Column(nullable = false)
	private String pincode;
	@Column(nullable = false)
	private String contactNo;
	@Column(name = "costumer_name", nullable = false)
	private String costumerName;
	@Column(name = "customer_name", nullable = false)
	private String customerName;
	@Column(nullable = false)
	private int quantity;
	
	@Column(nullable = false, unique = true)
	private String orderId;
	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;
	
	private String paymentId;
	private String paymentSignature;
 
	private LocalDateTime orderedAt;
	private LocalDateTime cancelledAt;
	private LocalDateTime deliveredAt;
	
	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus;
	public enum OrderStatus{
		 Processing, Confirmed, Shipped, Out_For_Delivery, Delivered, Cancelled
	}
	
	public enum PaymentStatus{
		Pending, Success, Refunded, Cancelled
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Products getProduct() {
		return product;
	}

	public void setProduct(Products product) {
		this.product = product;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getProcuctPrice() {
		return procuctPrice;
	}

	public void setProcuctPrice(double procuctPrice) {
		this.procuctPrice = procuctPrice;
		this.procuctPriceLegacy = procuctPrice;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public double getFinalprice() {
		return finalprice;
	}

	public void setFinalprice(double finalprice) {
		this.finalprice = finalprice;
		this.finalpriceLegacy = finalprice;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public double getShippingCharge() {
		return shippingCharge;
	}

	public void setShippingCharge(double shippingCharge) {
		this.shippingCharge = shippingCharge;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getContactNo() {
		return contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
		this.costumerName = customerName;
	}

	public String getCostumerName() {
		return costumerName;
	}

	public void setCostumerName(String costumerName) {
		this.costumerName = costumerName;
		this.customerName = costumerName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getPaymentSignature() {
		return paymentSignature;
	}

	public void setPaymentSignature(String paymentSignature) {
		this.paymentSignature = paymentSignature;
	}

	public LocalDateTime getOrderedAt() {
		return orderedAt;
	}

	public void setOrderedAt(LocalDateTime orderedAt) {
		this.orderedAt = orderedAt;
	}

	public LocalDateTime getCancelledAt() {
		return cancelledAt;
	}

	public void setCancelledAt(LocalDateTime cancelledAt) {
		this.cancelledAt = cancelledAt;
	}

	public LocalDateTime getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(LocalDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	
	
}
