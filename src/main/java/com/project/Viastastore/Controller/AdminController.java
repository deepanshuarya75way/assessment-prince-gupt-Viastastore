package com.project.Viastastore.Controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.Viastastore.Dto.CategoryDto;
import com.project.Viastastore.Dto.ProductDto;
import com.project.Viastastore.Model.Category;
import com.project.Viastastore.Model.Enquiry;
import com.project.Viastastore.Model.Orders;
import com.project.Viastastore.Model.Orders.OrderStatus;
import com.project.Viastastore.Model.Products;
import com.project.Viastastore.Model.Products.ProductStatus;
import com.project.Viastastore.Model.Users;
import com.project.Viastastore.Model.Users.UserRole;
import com.project.Viastastore.Model.Users.UserStatus;
import com.project.Viastastore.Repository.CategoryRepo;
import com.project.Viastastore.Repository.EnquiryRepo;
import com.project.Viastastore.Repository.OrderRepo;
import com.project.Viastastore.Repository.ProductRepo;
import com.project.Viastastore.Repository.UserRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Admin")
public class AdminController {
	
	@Autowired
	private HttpSession session;
	
	@Autowired
	private EnquiryRepo enquiryRepo;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private CategoryRepo categoryRepo;
	
	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private OrderRepo orderRepo;
	
	@GetMapping("/Dashboard")
	public String showDashboard() {
		
		if(session.getAttribute("LoggedInAdmin")== null) {
			return "redirect:/login";
		}
		return "Admin/Dashboard";
	}
	
	@GetMapping("/ManageUsers")
	public String showManageUsers(@RequestParam(value = "status", required = false)  UserStatus status, Model model) {
		
		if(session.getAttribute("LoggedInAdmin")==null) {
			return "redirect:/login";
		}
		if(status == null) {
			List<Users> users = userRepo.findAllByRole(UserRole.User);
			model.addAttribute("users", users);
		}
		else {
			List<Users> users = userRepo.findAllByRoleAndStatus(UserRole.User, status );
			model.addAttribute("users", users);
		}
		return "Admin/ManageUsers";
	}
	@GetMapping("/UpdateUserStatus/{id}")
	public String UpdateUserStatus(@PathVariable("id") long id, RedirectAttributes attributes, HttpServletRequest request) {
		
		Users user = userRepo.findById(id).get();
		if(user.getStatus().equals(UserStatus.Verified)) {
			user.setStatus(UserStatus.Disabled);
		}
		else if(user.getStatus().equals(UserStatus.Disabled)) {
			user.setStatus(UserStatus.Unverified);
			
		}
		userRepo.save(user);
		attributes.addFlashAttribute("msg", "User status successfully updated");
		return "redirect:"+ request.getHeader("referer");
	}
	
	@GetMapping("/ManageOrders")
	public String showManageOrders(@RequestParam(value = "status", required = false) OrderStatus status, Model model) {
		if (session.getAttribute("LoggedInAdmin") == null) return "redirect:/login";
		List<Orders> orders = (status == null) ? orderRepo.findAll() : orderRepo.findAllByOrderStatus(status);
		model.addAttribute("orders", orders);
		model.addAttribute("processingCount", orderRepo.findAllByOrderStatus(OrderStatus.Processing).size());
		model.addAttribute("deliveredCount",  orderRepo.findAllByOrderStatus(OrderStatus.Delivered).size());
		model.addAttribute("cancelledCount",  orderRepo.findAllByOrderStatus(OrderStatus.Cancelled).size());
		return "Admin/ManageOrders";
	}

	@GetMapping("/UpdateOrder/{id}")
	public String showUpdateOrder(@PathVariable("id") long id, Model model) {
		if (session.getAttribute("LoggedInAdmin") == null) return "redirect:/login";
		model.addAttribute("order", orderRepo.findById(id).get());
		return "Admin/UpdateOrder";
	}

	@PostMapping("/UpdateOrderStatus/{id}")
	public String updateOrderStatus(@PathVariable("id") long id,
									@RequestParam("orderStatus") OrderStatus orderStatus,
								RedirectAttributes attributes) {
		if (session.getAttribute("LoggedInAdmin") == null) return "redirect:/login";
		try {
			Orders order = orderRepo.findById(id).get();
			order.setOrderStatus(orderStatus);
			if (orderStatus == OrderStatus.Delivered) order.setDeliveredAt(java.time.LocalDateTime.now());
			if (orderStatus == OrderStatus.Cancelled) order.setCancelledAt(java.time.LocalDateTime.now());
			orderRepo.save(order);
			attributes.addFlashAttribute("msg", "Order status updated successfully");
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Failed to update order status");
		}
		return "redirect:/Admin/ManageOrders";
	}
	
	
	@GetMapping("/AddCategory")
	public String showAddCategory(Model model) {
		
		if(session.getAttribute("LoggedInAdmin")==null) {
			return "redirect:/login";
		}
		
		CategoryDto dto = new  CategoryDto();
		model.addAttribute("dto", dto);
		
		List<Category> categories = categoryRepo.findAll();
		model.addAttribute("categories", categories);
		return "Admin/AddCategory";
	}
	
	@PostMapping("/AddCategory")
	public String addCategory(@ModelAttribute CategoryDto dto, RedirectAttributes attributes) {

			Category category = new Category();
			
			if(categoryRepo.existsByCategoryName(dto.getCategoryName())) {
				
				attributes.addFlashAttribute("msg", "Category alrady exist!!");
				return "redirect:/Admin/AddCategory";
			}
			
			category.setCategoryName(dto.getCategoryName());
			category.setCategoryIcon(dto.getCategoryIcon());
			category.setVisible(true);
			
			categoryRepo.save(category);
			attributes.addFlashAttribute("msg", "Category Successfully added");
			
		return "redirect:/Admin/AddCategory";
	}
	
	@GetMapping("/EditCategory/{id}")
	public String showEditCategory(@PathVariable("id") long id, Model model) {
		if(session.getAttribute("LoggedInAdmin")==null) {
			return "redirect:/login";
		}
		Category category = categoryRepo.findById(id).orElse(null);
		if(category == null) {
			return "redirect:/Admin/AddCategory";
		}
		CategoryDto dto = new CategoryDto();
		dto.setCategoryName(category.getCategoryName());
		dto.setCategoryIcon(category.getCategoryIcon());
		model.addAttribute("dto", dto);
		model.addAttribute("category", category);
		model.addAttribute("categories", categoryRepo.findAll());
		return "Admin/EditCategory";
	}
	
	@PostMapping("/EditCategory/{id}")
	public String editCategory(@PathVariable("id") long id, @ModelAttribute CategoryDto dto, RedirectAttributes attributes) {
		Category category = categoryRepo.findById(id).orElse(null);
		if(category == null) {
			attributes.addFlashAttribute("msg", "Category not found");
			return "redirect:/Admin/AddCategory";
		}
		category.setCategoryName(dto.getCategoryName());
		category.setCategoryIcon(dto.getCategoryIcon());
		categoryRepo.save(category);
		attributes.addFlashAttribute("msg", "Category updated successfully");
		return "redirect:/Admin/AddCategory";
	}
	
	@GetMapping("/DeleteCategory/{id}")
	public String deleteCategory(@PathVariable("id") long id, RedirectAttributes attributes) {
		try {
			categoryRepo.deleteById(id);
			attributes.addFlashAttribute("msg", "Category deleted successfully");
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Unable to delete category");
		}
		return "redirect:/Admin/AddCategory";
	}
	
	@GetMapping("/ToggleCategoryVisibility/{id}")
	public String toggleCategoryVisibility(@PathVariable("id") long id, RedirectAttributes attributes) {
		Category category = categoryRepo.findById(id).orElse(null);
		if(category != null) {
			category.setVisible(!category.isVisible());
			categoryRepo.save(category);
			attributes.addFlashAttribute("msg", "Category visibility updated");
		} else {
			attributes.addFlashAttribute("msg", "Category not found");
		}
		return "redirect:/Admin/AddCategory";
	}
	
	@GetMapping("/AddProduct")
	public String showAddProduct(Model model) {
		
		if(session.getAttribute("LoggedInAdmin")==null) {
			return "redirect:/login";
		}
		
		ProductDto dto = new ProductDto();
		model.addAttribute("dto", dto);
		
		List<Category> categories = categoryRepo.findAllByIsVisible(true);
		model.addAttribute("categories", categories);
		return "Admin/AddProduct";
	}
	
	@PostMapping("/AddProduct")
	public String AddProduct(@ModelAttribute("dto") ProductDto dto, @RequestParam("images") MultipartFile images[], RedirectAttributes attributes) {
		
		try {
			if(images.length<2) {
				attributes.addFlashAttribute("msg", "Please atleast 2 images");
			}
			if(images.length<5) {
				attributes.addFlashAttribute("msg", "You can upload maximum 5 images");
			}
			
			// Product Image file Uploading
			
			String uploadDir = "public/ProductImages/";
			
			File folder = new File(uploadDir);
			if(!folder.exists()) {
				folder.mkdirs();
			}
			List<String> productImages = new ArrayList<>();
			for(MultipartFile image : images) {
				String storageFilename = UUID.randomUUID()+"_"+image.getName();
				Path uploadPath = Paths.get(uploadDir, storageFilename);
				InputStream inputStream = image.getInputStream();
				Files.copy(inputStream, uploadPath, StandardCopyOption.REPLACE_EXISTING);
				
				productImages.add(storageFilename);
			}
			// Product Data Uploading
			
			Products product = new Products();
			
			product.setProductName(dto.getProductName());
			product.setProductDescription(dto.getProductDescription());
			product.setBrandName(dto.getBrandName());
			product.setCategory(dto.getCategory());
			product.setGender(dto.getGender());
			
			//Pricing Details
			product.setCostPrice(dto.getCostPrice());
			product.setSellingPrice(dto.getSellingPrice());
			product.setDiscount(dto.getDiscount());
			double finalPrice = dto.getSellingPrice() - (dto.getSellingPrice() * dto.getDiscount())/100;
			product.setFinalPrice(finalPrice);
			
			product.setColors(dto.getColors());
			product.setSizes(dto.getSizes());
			
			product.setQuantity(dto.getQuantity());
			product.setShippingCharge(dto.getShippingCharge());
			product.setDeliveryTime(dto.getDeliveryTime());
			product.setReturnPolicy(dto.isReturnPolicy());
			product.setVisibility(true);
			product.setAddedAt(LocalDateTime.now());
			product.setStatus(ProductStatus.Available);
			product.setProductImages(productImages);
			
			productRepo.save(product);
			
			attributes.addFlashAttribute("msg", "Product added successfully!!"); 
			
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		
		return "redirect:/Admin/AddProduct";
	}
	
	
	@GetMapping("/ManageProduct")
	public String showManageProduct(Model model) {
		
		if(session.getAttribute("LoggedInAdmin")==null) {
			return "redirect:/login";
		}
		
		List<Products> products = productRepo.findAll();
		model.addAttribute("products", products);
		return "Admin/ManageProduct";
	}
	
	@GetMapping("/EditProduct/{id}")
	public String showEditProduct(@PathVariable("id") long id, Model model) {
		if(session.getAttribute("LoggedInAdmin")==null) {
			return "redirect:/login";
		}
		Products product = productRepo.findById(id).orElse(null);
		if(product == null) {
			return "redirect:/Admin/ManageProduct";
		}
		model.addAttribute("product", product);
		model.addAttribute("categories", categoryRepo.findAllByIsVisible(true));
		return "Admin/EditProduct";
	}
	
	@PostMapping("/EditProduct/{id}")
	public String editProduct(@PathVariable("id") long id,
						  @ModelAttribute("dto") ProductDto dto,
						  @RequestParam(value = "category", required = false) Long categoryId,
						  @RequestParam(value = "images", required = false) MultipartFile[] images,
						  RedirectAttributes attributes) {
		Products product = productRepo.findById(id).orElse(null);
		if(product == null) {
			attributes.addFlashAttribute("msg", "Product not found");
			return "redirect:/Admin/ManageProduct";
		}
		try {
			product.setProductName(dto.getProductName());
			product.setProductDescription(dto.getProductDescription());
			product.setBrandName(dto.getBrandName());
			product.setGender(dto.getGender());
			product.setCostPrice(dto.getCostPrice());
			product.setSellingPrice(dto.getSellingPrice());
			product.setDiscount(dto.getDiscount());
			double finalPrice = dto.getSellingPrice() - (dto.getSellingPrice() * dto.getDiscount())/100;
			product.setFinalPrice(finalPrice);
			product.setColors(dto.getColors());
			product.setSizes(dto.getSizes());
			product.setQuantity(dto.getQuantity());
			product.setShippingCharge(dto.getShippingCharge());
			product.setDeliveryTime(dto.getDeliveryTime());
			product.setReturnPolicy(dto.isReturnPolicy());
			if(categoryId != null) {
				categoryRepo.findById(categoryId).ifPresent(product::setCategory);
			}
			if(images != null && images.length > 0) {
				List<String> productImages = new ArrayList<>();
				String uploadDir = "public/ProductImages/";
				File folder = new File(uploadDir);
				if(!folder.exists()) {
					folder.mkdirs();
				}
				for(MultipartFile image : images) {
					if(image != null && !image.isEmpty()) {
						String storageFilename = UUID.randomUUID()+"_"+image.getOriginalFilename();
						Path uploadPath = Paths.get(uploadDir, storageFilename);
						try (InputStream inputStream = image.getInputStream()) {
							Files.copy(inputStream, uploadPath, StandardCopyOption.REPLACE_EXISTING);
						}
						productImages.add(storageFilename);
					}
				}
				if(!productImages.isEmpty()) {
					product.setProductImages(productImages);
				}
			}
			productRepo.save(product);
			attributes.addFlashAttribute("msg", "Product updated successfully");
		} catch(Exception e) {
			attributes.addFlashAttribute("msg", "Unable to update product");
		}
		return "redirect:/Admin/ManageProduct";
	}
	
	@GetMapping("/DeleteProduct/{id}")
	public String deleteProduct(@PathVariable("id") long id, RedirectAttributes attributes) {
		try {
			productRepo.deleteById(id);
			attributes.addFlashAttribute("msg", "Product deleted successfully");
		} catch(Exception e) {
			attributes.addFlashAttribute("msg", "Unable to delete product");
		}
		return "redirect:/Admin/ManageProduct";
	}
	
	@GetMapping("/ToggleProductVisibility/{id}")
	public String toggleProductVisibility(@PathVariable("id") long id, RedirectAttributes attributes) {
		Products product = productRepo.findById(id).orElse(null);
		if(product != null) {
			product.setVisibility(!product.isVisibility());
			productRepo.save(product);
			attributes.addFlashAttribute("msg", "Product visibility updated");
		} else {
			attributes.addFlashAttribute("msg", "Product not found");
		}
		return "redirect:/Admin/ManageProduct";
	}
	
	@GetMapping("/DeleteUser/{id}")
	public String deleteUser(@PathVariable("id") long id, RedirectAttributes attributes) {
		try {
			userRepo.deleteById(id);
			attributes.addFlashAttribute("msg", "User deleted successfully");
		} catch(Exception e) {
			attributes.addFlashAttribute("msg", "Unable to delete user");
		}
		return "redirect:/Admin/ManageUsers";
	}
	
	@GetMapping("/ViewEnquiry")
	public String showViewEnquiry(Model model) {
		
		if(session.getAttribute("LoggedInAdmin")==null) {
			return "redirect:/login";
		}
		
		List<Enquiry> enquiries = enquiryRepo.findAll();
		model.addAttribute("enquiries", enquiries);
		return "Admin/ViewEnquiry";
	}
	
	@GetMapping("/Feedbacks")
	public String showFeedbacks() {
		return "Admin/Feedbacks";
	}
	
	@GetMapping("/logout")
	public String logout() {
		session.removeAttribute("LoggedInAdmin");
		return "redirect:/login";
	}
	
	
	/*
	 * @GetMapping public String deleteEnquiry(@PathVariable("id") long id) {
	 * 
	 * return "redirect:/Admin/ViewEnquiry";
	 * 
	 * }
	 */
	
}
