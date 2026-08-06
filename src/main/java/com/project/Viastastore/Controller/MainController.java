package com.project.Viastastore.Controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.web.bind.annotation.ResponseBody;

import com.project.Viastastore.Dto.EnquiryDto;
import com.project.Viastastore.Dto.SavedAddressDto;
import com.project.Viastastore.Dto.UserDto;
import com.project.Viastastore.MailService.PaymentService;
import com.project.Viastastore.MailService.SendMailService;
import com.project.Viastastore.Model.Cart;
import com.project.Viastastore.Model.Category;
import com.project.Viastastore.Model.Enquiry;
import com.project.Viastastore.Model.Orders;
import com.project.Viastastore.Model.Orders.OrderStatus;
import com.project.Viastastore.Model.Orders.PaymentStatus;
import com.project.Viastastore.Model.Products;
import com.project.Viastastore.Model.SavedAddress;
import com.project.Viastastore.Model.Users;
import com.project.Viastastore.Model.Users.UserRole;
import com.project.Viastastore.Model.Users.UserStatus;
import com.project.Viastastore.Repository.CartRepo;
import com.project.Viastastore.Repository.CategoryRepo;
import com.project.Viastastore.Repository.EnquiryRepo;
import com.project.Viastastore.Repository.OrderRepo;
import com.project.Viastastore.Repository.ProductRepo;
import com.project.Viastastore.Repository.SavedAddressRepo;
import com.project.Viastastore.Repository.UserRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

	@Autowired private EnquiryRepo enquiryRepo;
	@Autowired private UserRepo userRepo;
	@Autowired private HttpSession session;
	@Autowired private SendMailService sendMailService;
	@Autowired private CategoryRepo categoryRepo;
	@Autowired private ProductRepo productRepo;
	@Autowired private CartRepo cartRepo;
	@Autowired private SavedAddressRepo addressRepo;
	@Autowired private OrderRepo orderRepo;
	@Autowired private PaymentService paymentService;

	// ── Static pages ──────────────────────────────────────────────────────────

	@GetMapping("/")
	public String showIndex() { return "index"; }

	@GetMapping("/about")
	public String showAbout() { return "about"; }

	@GetMapping("/blog")
	public String showBlog() { return "blog"; }

	// ── Auth ──────────────────────────────────────────────────────────────────

	@GetMapping("/register")
	public String showRegister(Model model) {
		model.addAttribute("dto", new UserDto());
		return "register";
	}

	@PostMapping("/register")
	public String userRegister(@ModelAttribute UserDto dto, RedirectAttributes attributes, HttpSession session) {
		try {
			if (userRepo.existsByEmail(dto.getEmail())) {
				attributes.addFlashAttribute("msg", "User already exists");
				return "redirect:/register";
			}
			Users user = new Users();
			user.setName(dto.getName());
			user.setContactNo(dto.getContactNo());
			user.setEmail(dto.getEmail());
			user.setGender(dto.getGender());
			user.setPassword(dto.getPassword());
			user.setRole(UserRole.User);
			user.setStatus(UserStatus.Unverified);
			user.setRegisterAt(LocalDateTime.now());

			String otp = 100000 + new SecureRandom().nextInt(900000) + "";
			user.setOtp(otp);
			user.setGeneratedAt(LocalDateTime.now());
			user.setExpiryTime(LocalDateTime.now().plusMinutes(5));
			userRepo.save(user);

			sendMailService.sendOtpMail(user);
			System.err.println("OTP : "+otp);
			session.setAttribute("email", user.getEmail());
			attributes.addFlashAttribute("msg", "Registration successful, please verify OTP");
			return "redirect:/verify-otp";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/register";
	}

	@GetMapping("/verify-otp")
	public String showVerifyOtp(HttpSession session) {
		if (session.getAttribute("email") == null) return "redirect:/register";
		return "verify-otp";
	}

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") String otp, RedirectAttributes attributes, HttpSession session) {
		try {
			String email = (String) session.getAttribute("email");
			Users user = userRepo.findByEmail(email);
			if (!otp.equals(user.getOtp())) {
				attributes.addFlashAttribute("msg", "Invalid OTP");
				return "redirect:/verify-otp";
			}
			if (ChronoUnit.MINUTES.between(user.getGeneratedAt(), LocalDateTime.now()) > 5) {
				attributes.addFlashAttribute("msg", "OTP expired");
				return "redirect:/verify-otp";
			}
			user.setStatus(UserStatus.Verified);
			userRepo.save(user);
			attributes.addFlashAttribute("msg", "Registration successful");
			return "redirect:/login";
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/verify-otp";
	}

	@GetMapping("/resend-otp")
	public String resendOtp(RedirectAttributes attributes, HttpSession session) {
		try {
			String email = (String) session.getAttribute("email");
			Users user = userRepo.findByEmail(email);
			String otp = 100000 + new SecureRandom().nextInt(900000) + "";
			user.setOtp(otp);
			user.setGeneratedAt(LocalDateTime.now());
			user.setExpiryTime(LocalDateTime.now().plusMinutes(5));
			userRepo.save(user);
			sendMailService.sendOtpMail(user);
			System.err.println("Resend OTP : "+otp);
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Something went wrong");
		}
		return "redirect:/verify-otp";
	}

	@GetMapping("/login")
	public String showLogin(@RequestParam(value = "redirect", required = false) String redirect, Model model) {
		model.addAttribute("redirect", redirect != null ? redirect : "/");
		return "login";
	}

	@PostMapping("/login")
	public String login(HttpServletRequest request, RedirectAttributes attributes, HttpSession session) {
		try {
			String email = request.getParameter("email");
			String password = request.getParameter("password");
			String redirect = request.getParameter("redirect");
			if (redirect == null || redirect.isBlank()) redirect = "/";
			if (!userRepo.existsByEmail(email)) {
				attributes.addFlashAttribute("msg", "User does not exist");
				return "redirect:/login";
			}
			Users user = userRepo.findByEmail(email);
			if (email.equals(user.getEmail()) && password.equals(user.getPassword())) {
				if (user.getRole().equals(UserRole.User)) {
					if (user.getStatus().equals(UserStatus.Unverified)) {
						sendMailService.sendOtpMail(user);
						session.setAttribute("email", user.getEmail());
						return "redirect:/verify-otp";
					} else if (user.getStatus().equals(UserStatus.Disabled)) {
						attributes.addFlashAttribute("msg", "Account disabled, contact administrator");
						return "redirect:/login";
					}
					session.setAttribute("LoggedInUser", user);
					return "redirect:" + redirect;
				} else if (user.getRole().equals(UserRole.Admin)) {
					session.setAttribute("LoggedInAdmin", user);
					return "redirect:/Admin/Dashboard";
				}
			} else {
				attributes.addFlashAttribute("msg", "Invalid credentials");
			}
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Something went wrong");
		}
		return "redirect:/login";
	}

	@GetMapping("/userLogout")
	public String userLogout() {
		session.removeAttribute("LoggedInUser");
		return "redirect:/login";
	}

	// ── Shop & Product ────────────────────────────────────────────────────────

	@GetMapping("/shop")
	public String showShop(@RequestParam(value = "id", required = false) Long id, Model model) {
		List<Category> categories = categoryRepo.findAllByIsVisible(true);
		model.addAttribute("categories", categories);
		if (id == null) {
			model.addAttribute("products", productRepo.findAll());
		} else {
			Category category = categoryRepo.findById(id).get();
			model.addAttribute("products", productRepo.findAllByCategory(category));
		}
		return "shop";
	}

	@GetMapping("/Product/{id}")
	public String showProduct(@PathVariable("id") long id, Model model) {
		model.addAttribute("product", productRepo.findById(id).get());
		return "Product";
	}

	// ── Cart ──────────────────────────────────────────────────────────────────

	@GetMapping("/cart")
	public String showCart(Model model, RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInUser") == null) {
			attributes.addFlashAttribute("msg", "Please login first");
			return "redirect:/login?redirect=/cart";
		}
		Users user = (Users) session.getAttribute("LoggedInUser");
		List<Cart> cartItems = cartRepo.findAllByUser(user);
		model.addAttribute("cartItems", cartItems);
		return "cart";
	}

	@GetMapping("/cart/add/{productId}")
	public String addToCart(@PathVariable("productId") long productId,
							@RequestParam(value = "size", defaultValue = "") String size,
							@RequestParam(value = "color", defaultValue = "") String color,
							RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInUser") == null) {
			attributes.addFlashAttribute("msg", "Please login first");
			return "redirect:/login?redirect=/Product/" + productId;
		}
		try {
			Users user = (Users) session.getAttribute("LoggedInUser");
			Products product = productRepo.findById(productId).get();

			if (cartRepo.existsByUserAndProduct(user, product)) {
				// increment quantity
				List<Cart> items = cartRepo.findAllByUser(user);
				for (Cart c : items) {
					if (c.getProduct().getId() == productId) {
						c.setQuantity(c.getQuantity() + 1);
						c.setFinalPrice(c.getProductPrice() * c.getQuantity());
						cartRepo.save(c);
						break;
					}
				}
				attributes.addFlashAttribute("msg", "Quantity updated in cart");
			} else {
				Cart cart = new Cart();
				cart.setUser(user);
				cart.setProduct(product);
				cart.setQuantity(1);
				cart.setSize(size);
				cart.setColor(color);
				cart.setProductPrice(product.getFinalPrice());
				cart.setFinalPrice(product.getFinalPrice());
				cartRepo.save(cart);
				attributes.addFlashAttribute("msg", "Added to cart!");
			}
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Could not add to cart");
		}
		return "redirect:/cart";
	}

	@GetMapping("/cart/remove/{cartId}")
	public String removeFromCart(@PathVariable("cartId") long cartId, RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInUser") == null) return "redirect:/login";
		try {
			cartRepo.deleteById(cartId);
			attributes.addFlashAttribute("msg", "Item removed");
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Could not remove item");
		}
		return "redirect:/cart";
	}

	@GetMapping("/cart/update/{cartId}")
	public String updateCartQty(@PathVariable("cartId") long cartId,
								@RequestParam("qty") int qty,
								RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInUser") == null) return "redirect:/login";
		try {
			Cart cart = cartRepo.findById(cartId).get();
			if (qty <= 0) {
				cartRepo.delete(cart);
			} else {
				cart.setQuantity(qty);
				cart.setFinalPrice(cart.getProductPrice() * qty);
				cartRepo.save(cart);
			}
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Could not update cart");
		}
		return "redirect:/cart";
	}

	// ── Checkout ──────────────────────────────────────────────────────────────

	@GetMapping("/Checkout")
	public String showCheckout(Model model, RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInUser") == null) {
			attributes.addFlashAttribute("msg", "Please login first");
			return "redirect:/login";
		}
		Users user = (Users) session.getAttribute("LoggedInUser");
		List<Cart> cartItems = cartRepo.findAllByUser(user);
		if (cartItems.isEmpty()) {
			attributes.addFlashAttribute("msg", "Your cart is empty");
			return "redirect:/cart";
		}
		List<SavedAddress> addresses = addressRepo.findAllByUser(user);
		model.addAttribute("cartItems", cartItems);
		model.addAttribute("addresses", addresses);

		double subtotal = cartItems.stream().mapToDouble(Cart::getFinalPrice).sum();
		double shipping = subtotal >= 5000 ? 0 : 150;
		model.addAttribute("subtotal", subtotal);
		model.addAttribute("shipping", shipping);
		model.addAttribute("total", subtotal + shipping);
		return "Checkout";
	}

	@PostMapping("/PlaceOrder")
	public String placeOrder(@RequestParam("customerName") String customerName,
							 @RequestParam("contactNo") String contactNo,
							 @RequestParam("shippingAddress") String shippingAddress,
							 @RequestParam("pincode") String pincode,
							 @RequestParam("paymentMethod") String paymentMethod,
							 RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInUser") == null) return "redirect:/login";
		try {
			Users user = (Users) session.getAttribute("LoggedInUser");
			List<Cart> cartItems = cartRepo.findAllByUser(user);
			if (cartItems.isEmpty()) {
				attributes.addFlashAttribute("msg", "Cart is empty");
				return "redirect:/cart";
			}

			double subtotal = cartItems.stream().mapToDouble(Cart::getFinalPrice).sum();
			double shipping = subtotal >= 5000 ? 0 : 150;
			double total = subtotal + shipping;

			String orderCustomerName = customerName != null && !customerName.trim().isEmpty()
				? customerName.trim() : (user.getName() != null ? user.getName() : "");
			String orderContactNo = contactNo != null && !contactNo.trim().isEmpty()
				? contactNo.trim() : (user.getContactNo() != null ? user.getContactNo() : "");
			String orderShippingAddress = shippingAddress != null && !shippingAddress.trim().isEmpty()
				? shippingAddress.trim() : "";
			String orderPincode = pincode != null && !pincode.trim().isEmpty()
				? pincode.trim() : "";

			String groupOrderId = "VIA-" + System.currentTimeMillis();

			for (Cart cart : cartItems) {
				Orders order = new Orders();
				order.setUser(user);
				order.setProduct(cart.getProduct());
				order.setProductName(cart.getProduct().getProductName());
				order.setDescription(cart.getProduct().getProductDescription());
				order.setProcuctPrice(cart.getProduct().getSellingPrice());
				order.setDiscount(cart.getProduct().getDiscount());
				order.setFinalprice(cart.getProduct().getFinalPrice());
				order.setColor(cart.getColor() != null ? cart.getColor() : "");
				order.setSize(cart.getSize() != null ? cart.getSize() : "");
				order.setQuantity(cart.getQuantity());
				order.setShippingCharge(shipping / cartItems.size());
				order.setTotalAmount(cart.getFinalPrice() + (shipping / cartItems.size()));
				order.setCustomerName(orderCustomerName);
				order.setContactNo(orderContactNo);
				order.setShippingAddress(orderShippingAddress);
				order.setPincode(orderPincode);
				order.setOrderId(groupOrderId + "-" + cart.getProduct().getId());
				order.setOrderStatus(OrderStatus.Processing);
				order.setPaymentStatus(paymentMethod.equals("cod") ? PaymentStatus.Pending : PaymentStatus.Success);
				order.setOrderedAt(LocalDateTime.now());
				orderRepo.save(order);
			}

			// clear cart after order
			for (Cart cart : cartItems) cartRepo.delete(cart);

			return "redirect:/TrackOrder/" + groupOrderId;
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Order failed: " + e.getMessage());
			return "redirect:/Checkout";
		}
	}

	// ── Track Order ───────────────────────────────────────────────────────────

	@GetMapping("/TrackOrder/{orderId}")
	public String trackOrder(@PathVariable("orderId") String orderId, Model model, RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInUser") == null) {
			attributes.addFlashAttribute("msg", "Please login first");
			return "redirect:/login";
		}
		try {
			Users user = (Users) session.getAttribute("LoggedInUser");
			List<Orders> allOrders = orderRepo.findAllByUser(user);
			// find orders matching the group orderId prefix
			List<Orders> groupOrders = allOrders.stream()
				.filter(o -> o.getOrderId().startsWith(orderId))
				.toList();
			if (groupOrders.isEmpty()) {
				attributes.addFlashAttribute("msg", "Order not found");
				return "redirect:/MyOrders";
			}
			model.addAttribute("orders", groupOrders);
			model.addAttribute("order", groupOrders.get(0));
			model.addAttribute("orderId", orderId);
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Could not load order");
			return "redirect:/MyOrders";
		}
		return "TrackOrder";
	}

	// ── My Orders ─────────────────────────────────────────────────────────────

	@GetMapping("/MyOrders")
	public String myOrders(Model model, RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInUser") == null) {
			attributes.addFlashAttribute("msg", "Please login first");
			return "redirect:/login";
		}
		Users user = (Users) session.getAttribute("LoggedInUser");
		List<Orders> orders = orderRepo.findAllByUser(user);
		model.addAttribute("orders", orders);
		return "MyOrders";
	}

	// ── Profile & Address ─────────────────────────────────────────────────────

	@GetMapping("/MyProfile")
	public String myProfile(RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInUser") == null) {
			attributes.addFlashAttribute("msg", "Please login first");
			return "redirect:/login";
		}
		return "MyProfile";
	}

	@GetMapping("/SavedAddress")
	public String showSavedAddress(Model model, RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInUser") == null) {
			attributes.addFlashAttribute("msg", "Please login first");
			return "redirect:/login";
		}
		Users user = (Users) session.getAttribute("LoggedInUser");
		model.addAttribute("addresses", addressRepo.findAllByUser(user));
		model.addAttribute("dto", new SavedAddressDto());
		return "SavedAddress";
	}

	@PostMapping("/SavedAddress")
	public String saveAddress(@ModelAttribute SavedAddressDto dto, RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInUser") == null) return "redirect:/login";
		try {
			Users user = (Users) session.getAttribute("LoggedInUser");
			SavedAddress address = new SavedAddress();
			address.setCustomerName(dto.getCustomerName());
			address.setContactNo(dto.getContactNo());
			address.setPincode(dto.getPincode());
			address.setAddress(dto.getAddress());
			address.setUser(user);
			address.setActive(true);
			addressRepo.save(address);
			attributes.addFlashAttribute("msg", "Address saved successfully!");
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Failed to save address");
		}
		return "redirect:/SavedAddress";
	}

	@GetMapping("/SavedAddress/delete/{id}")
	public String deleteAddress(@PathVariable("id") long id, RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInUser") == null) return "redirect:/login";
		try {
			addressRepo.deleteById(id);
			attributes.addFlashAttribute("msg", "Address deleted");
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Could not delete address");
		}
		return "redirect:/SavedAddress";
	}

	// ── Payment ──────────────────────────────────────────────────────────────

	@PostMapping("/Payment/initiate")
	@ResponseBody
	public Map<String, Object> initiatePayment(
			@RequestParam("customerName") String customerName,
			@RequestParam("contactNo") String contactNo,
			@RequestParam("shippingAddress") String shippingAddress,
			@RequestParam("pincode") String pincode,
			HttpSession session) {
		Map<String, Object> response = new java.util.HashMap<>();
		try {
			if (session.getAttribute("LoggedInUser") == null) {
				response.put("error", "Not logged in"); return response;
			}
			session.setAttribute("pendingCustomerName", customerName);
			session.setAttribute("pendingContactNo", contactNo);
			session.setAttribute("pendingShippingAddress", shippingAddress);
			session.setAttribute("pendingPincode", pincode);
			response = paymentService.createPayment(session);
		} catch (Exception e) {
			response.put("error", e.getMessage());
		}
		return response;
	}

	@PostMapping("/Payment/verify")
	@ResponseBody
	public Map<String, Object> verifyPayment(
			@RequestParam("razorpay_payment_id") String paymentId,
			@RequestParam("razorpay_order_id") String razorOrderId,
			@RequestParam("razorpay_signature") String signature,
			HttpSession session) {
		Map<String, Object> response = new java.util.HashMap<>();
		try {
			String result = paymentService.verifyPayment(paymentId, razorOrderId, signature, session);
			if ("failed".equals(result)) {
				response.put("status", "failed");
			} else {
				response.put("status", "success");
				response.put("orderId", result);
			}
		} catch (Exception e) {
			response.put("status", "failed");
			response.put("error", e.getMessage());
		}
		return response;
	}

	// ── Contact ───────────────────────────────────────────────────────────────

	@GetMapping("/ContactUs")
	public String showContactUs(Model model) {
		model.addAttribute("dto", new EnquiryDto());
		return "ContactUs";
	}

	@PostMapping("/SubmitEnquiry")
	public String submitEnquiry(@ModelAttribute EnquiryDto dto, RedirectAttributes attributes) {
		try {
			Enquiry enquiry = new Enquiry();
			enquiry.setName(dto.getName());
			enquiry.setContactNo(dto.getContactNo());
			enquiry.setEmail(dto.getEmail());
			enquiry.setAddress(dto.getAddress());
			enquiry.setMessage(dto.getMessage());
			enquiry.setEnquiryType(dto.getEnquiryType());
			enquiry.setEnquiryAt(LocalDateTime.now());
			enquiryRepo.save(enquiry);
			attributes.addFlashAttribute("msg", "Enquiry submitted successfully.");
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Something went wrong");
		}
		return "redirect:/ContactUs";
	}
}
