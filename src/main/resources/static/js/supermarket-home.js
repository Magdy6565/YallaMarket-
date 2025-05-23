// supermarket-home.js - JavaScript for the supermarket homepage

// Global variables
let currentCategoryId = null;
let currentVendorFilter = []; // Changed to array for multi-select
let cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
let vendors = [];
let categoriesData = [];
let currentOffset = 0;
const LIMIT = 4; // Number of products per page

// Wait for DOM to be fully loaded
document.addEventListener("DOMContentLoaded", function () {
  // --- DOM Element Selections ---
  const categoriesContainer = document.getElementById("categoriesContainer");
  const productsGrid = document.getElementById("productsGrid");
  const emptyState = document.getElementById("emptyState");
  const vendorFilter = document.getElementById("vendorFilter");
  const filterButton = document.getElementById("filterButton");
  const productModal = document.getElementById("productModal");
  const closeModal = document.querySelector(".close-modal");
  const modalBody = document.getElementById("modalBody");
  const cartBadge = document.getElementById("cartBadge");

  // Update cart badge
  updateCartBadge();

  // Mobile menu toggle functionality
  const menuToggle = document.getElementById("menuToggle");
  const navbarLinks = document.getElementById("navbarLinks");

  if (menuToggle && navbarLinks) {
    menuToggle.addEventListener("click", function () {
      navbarLinks.classList.toggle("active");
    });
  }

  // User menu dropdown functionality
  const userMenu = document.getElementById("userMenu");
  const userMenuButton = userMenu
    ? userMenu.querySelector(".user-menu-button")
    : null;

  if (userMenuButton) {
    userMenuButton.addEventListener("click", function () {
      userMenu.classList.toggle("open");
    });

    // Close the dropdown when clicking outside
    document.addEventListener("click", function (event) {
      if (!userMenu.contains(event.target)) {
        userMenu.classList.remove("open");
      }
    });
  }

  // --- Core Functions ---

  /**
   * Fetch categories from the API
   */
  async function fetchCategories() {
    try {
      const response = await fetch("/api/my-products/categories");

      if (!response.ok) {
        throw new Error("Failed to fetch categories");
      }

      categoriesData = await response.json();
      renderCategories(categoriesData);
    } catch (error) {
      console.error("Error fetching categories:", error);
      categoriesContainer.innerHTML = `
                <div class="error">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Failed to load categories. Please try again later.</p>
                </div>
            `;
    }
  }

  /**
   * Render categories in the UI
   */
  function renderCategories(categories) {
    categoriesContainer.innerHTML = "";

    if (!categories || categories.length === 0) {
      categoriesContainer.innerHTML = `
                <div class="empty">
                    <p>No categories available</p>
                </div>
            `;
      return;
    }

    // Map category names to appropriate Font Awesome icons
    const categoryIcons = {
      Fruits: "fa-apple-whole",
      Vegetables: "fa-carrot",
      Meat: "fa-drumstick-bite",
      Dairy: "fa-cheese",
      Bakery: "fa-bread-slice",
      Beverages: "fa-mug-hot",
      Snacks: "fa-cookie",
      "Canned Goods": "fa-can-food",
      "Frozen Foods": "fa-snowflake",
      Household: "fa-broom",
      "Personal Care": "fa-pump-soap",
      "Baby Products": "fa-baby",
      "Pet Supplies": "fa-paw",
      Electronics: "fa-laptop",
      Clothing: "fa-tshirt",
      Toys: "fa-gamepad",
      Books: "fa-book",
      "Office Supplies": "fa-paperclip",
      "Sports & Outdoors": "fa-football",
      "Tools & Hardware": "fa-tools",
      "Home Decor": "fa-couch",
      Garden: "fa-leaf",
      "Health & Wellness": "fa-heart-pulse",
      Beauty: "fa-spa",
    };

    // Create category cards
    categories.forEach((category) => {
      const iconClass = categoryIcons[category] || "fa-tag";
      const categoryCard = document.createElement("div");
      categoryCard.className = "category-card";
      categoryCard.setAttribute("data-category-id", category.id);
      categoryCard.innerHTML = `
                <div class="category-icon">
                    <i class="fas ${iconClass}"></i>
                </div>
                <div class="category-name">${category}</div>
            `;

      // Add click event to fetch products of this category
      categoryCard.addEventListener("click", function () {
        // Remove active class from all categories
        document.querySelectorAll(".category-card.active").forEach((card) => {
          card.classList.remove("active");
        });

        // Add active class to clicked category
        this.classList.add("active");
        // Store the selected category ID
        currentCategoryId = category;

        // Reset offset when changing category
        currentOffset = 0;

        // Fetch vendors for this category
        fetchVendorsByCategory(category);

        // Fetch products for this category
        fetchProducts(category, currentVendorFilter);
      });

      categoriesContainer.appendChild(categoryCard);
    });
  }
  /**
   * Fetch vendors by category
   */
  async function fetchVendorsByCategory(category) {
    try {
      const response = await fetch(
        `/api/my-products/vendors-by-category/${encodeURIComponent(category)}`
      );

      if (!response.ok) {
        throw new Error("Failed to fetch vendors for category");
      }

      const categoryVendors = await response.json();
      populateVendorFilter(categoryVendors);
    } catch (error) {
      console.error("Error fetching vendors by category:", error);
      // Fallback to empty vendor list
      populateVendorFilter([]);
    }
  }

  /**
   * Fetch vendors list (fallback)
   */
  async function fetchVendors() {
    try {
      // Replace with your actual vendor API endpoint
      const response = await fetch("/api/user-management/users/role/1");

      if (!response.ok) {
        throw new Error("Failed to fetch vendors");
      }

      vendors = await response.json();
      populateVendorFilter(vendors);
    } catch (error) {
      console.error("Error fetching vendors:", error);
    }
  }

  /**
   * Populate vendor filter dropdown with multi-select support
   */
  function populateVendorFilter(vendors) {
    vendorFilter.innerHTML = "";

    // Add "All Vendors" option
    const allOption = document.createElement("option");
    allOption.value = "";
    allOption.textContent = "All Vendors";
    vendorFilter.appendChild(allOption);

    vendors.forEach((vendor) => {
      const option = document.createElement("option");
      option.value = vendor.id;
      option.textContent = vendor.username; // Use username as per VendorDTO
      vendorFilter.appendChild(option);
    });
    // Reset current vendor filter when vendors change
    currentVendorFilter = [];
  }

  /**
   * Get selected vendor IDs from multi-select dropdown
   */
  function getSelectedVendorIds() {
    const selectedOptions = Array.from(vendorFilter.selectedOptions);
    return selectedOptions
      .map((option) => option.value)
      .filter((value) => value !== ""); // Remove empty "All Vendors" option
  }
  /**
   * Fetch products by category and vendor filter
   */
  async function fetchProducts(categoryId, vendorIds = []) {
    // Show loading state
    productsGrid.innerHTML =
      '<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> Loading products...</div>';

    try {
      // Create URL with query parameters
      let url = `/api/my-products/category/${encodeURIComponent(
        categoryId
      )}?offset=${currentOffset}&limit=${LIMIT}`;

      // Add vendor IDs as query parameters if any are selected
      if (vendorIds && vendorIds.length > 0) {
        vendorIds.forEach((vendorId) => {
          url += `&vendorIds=${vendorId}`;
        });
      }

      const response = await fetch(url);

      if (!response.ok) {
        throw new Error("Failed to fetch products");
      }

      const products = await response.json();
      renderProducts(products);
    } catch (error) {
      console.error("Error fetching products:", error);
      productsGrid.innerHTML = `
                <div class="error">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Failed to load products. Please try again later.</p>
                </div>
            `;
    }
  }

  /**
   * Render products in the grid
   */
  function renderProducts(products) {
    productsGrid.innerHTML = ""; // Clear existing products
    emptyState.style.display = "none";

    if (!products || products.length === 0) {
      emptyState.style.display = "block";
      return;
    }

    products.forEach((product) => {
      const productCard = document.createElement("div");
      productCard.className = "product-card";

      // Determine if product is in stock
      const isOutOfStock = !product.quantity || product.quantity <= 0;

      productCard.innerHTML = `
                <img src="${
                  product.imageUrl || "https://via.placeholder.com/150"
                }" alt="${product.name}">
                <h3 class="product-name">${product.name}</h3>
//                <h3 class="data-product-id">${product.productId}</h3>
                <p class="product-price">$${parseFloat(product.price).toFixed(
                  2
                )}</p>
                <p class="product-quantity">${
                  product.quantity || "Out Of Stock"
                }</p>                <p class="product-vendor">${product.vendorUsername || "N/A"}</p>
                <p class="product-vendorRating">
                  ${displayRatingStars(product.vendorRating || 0)}
                  <span>${product.vendorRating ? parseFloat(product.vendorRating).toFixed(1) + '/5' : 'Not rated yet'}</span>
                </p>
                <div class="product-actions">
                    ${
                      !isOutOfStock
                        ? `<button class="add-to-cart-btn" data-product='${JSON.stringify(
                            product
                          )}'>Add to Cart</button>`
                        : `<button class="out-of-stock-btn" disabled>Out of Stock</button>`
                    }
                    <button class="view-product-btn" data-product-id="${
                      product.productId
                    }">View</button>
                </div>
            `;
      productsGrid.appendChild(productCard);
    });

    // Attach event listeners for "Add to Cart" buttons
    document.querySelectorAll(".add-to-cart-btn").forEach((button) => {
      button.addEventListener("click", function () {
        const product = JSON.parse(this.getAttribute("data-product"));
        addToCart(product);
      });
    });

    // Attach event listeners for "View Product" buttons
    document.querySelectorAll(".view-product-btn").forEach((button) => {
      button.addEventListener("click", function () {
        const productId = this.getAttribute("data-product-id");
        viewProductDetails(productId);
      });
    });
  }
  /**
   * Add product to cart
   */
  function addToCart(product) {
    // Ensure we have a valid product ID - could be productId or id
    const productId = product.productId || product.id;
    const productName = product.name;
    const productPrice = product.price;
    const productImageUrl = product.imageUrl;
    const vendorName = product.vendorName || product.vendorUsername;
    const category = product.category;
    const availableStock = product.quantity || 0;

    if (!productId) {
      console.error("Product ID is missing:", product);
      alert("Error: Cannot add product to cart - missing product ID");
      return;
    }

    // Convert productId to string for consistent comparison
    const productIdStr = String(productId);

    const existingItemIndex = cartItems.findIndex(
      (item) => String(item.id) === productIdStr
    );
    if (existingItemIndex > -1) {
      // If item exists, check if we can increase quantity based on available stock
      const currentQuantity = cartItems[existingItemIndex].quantity;

      if (currentQuantity >= availableStock) {
        alert(
          `Cannot add more of this product. Maximum available: ${availableStock}`
        );
        return;
      }

      // If stock allows, increase quantity
      cartItems[existingItemIndex].quantity += 1;
      // Also update max quantity in case it changed
      cartItems[existingItemIndex].maxQuantity = availableStock;
      console.log(
        `Increased quantity for ${productName}. New quantity: ${cartItems[existingItemIndex].quantity}`
      );
    } else {
      // If item does not exist, add it with quantity 1
      const newItem = {
        id: productIdStr, // Store as string for consistency
        name: productName,
        price: parseFloat(productPrice),
        imageUrl: productImageUrl,
        vendorName: vendorName,
        category: category,
        quantity: 1,
        maxQuantity: availableStock, // Store the max quantity for reference
      };
      cartItems.push(newItem);
      console.log(`Added new item to cart:`, newItem);
    }

    localStorage.setItem("cartItems", JSON.stringify(cartItems));
    updateCartBadge();

    // Show more detailed confirmation
    const totalQuantity =
      cartItems.find((item) => String(item.id) === productIdStr)?.quantity || 0;
    alert(`${productName} added to cart! (Total: ${totalQuantity})`);
  }

  /**
   * Update cart badge with quantity
   */
  function updateCartBadge() {
    const totalItems = cartItems.reduce(
      (total, item) => total + item.quantity,
      0
    );
    cartBadge.textContent = totalItems;

    // Hide badge if cart is empty
    cartBadge.style.display = totalItems > 0 ? "flex" : "none";
  }
  /**
   * View product details - redirects to product details page
   */
  function viewProductDetails(productId) {
    // Navigate to the product details page
    window.location.href = `/supermarket/product/${productId}`;
  }
  // Product details rendering has been moved to the supermarket-product-details.html page

  // --- Event Listeners ---    // Filter products by vendors
  if (filterButton) {
    filterButton.addEventListener("click", function () {
      if (currentCategoryId) {
        // Get selected vendor IDs from multi-select dropdown
        currentVendorFilter = getSelectedVendorIds();
        console.log("Selected vendors:", currentVendorFilter);

        currentOffset = 0; // Reset offset when applying vendor filter
        fetchProducts(currentCategoryId, currentVendorFilter);
      } else {
        alert("Please select a category first.");
      }
    });
  }

  // Add change listener to vendor filter dropdown to show selection count
  if (vendorFilter) {
    vendorFilter.addEventListener("change", function () {
      const selectedCount = Array.from(this.selectedOptions).length;
      const allSelected = selectedCount === this.options.length;

      if (
        selectedCount === 0 ||
        (selectedCount === 1 && this.selectedOptions[0].value === "")
      ) {
        filterButton.textContent = "Filter (All Vendors)";
      } else if (
        allSelected ||
        (selectedCount === this.options.length - 1 &&
          !Array.from(this.selectedOptions).some((opt) => opt.value === ""))
      ) {
        filterButton.textContent = "Filter (All Vendors)";
      } else {
        filterButton.textContent = `Filter (${selectedCount} selected)`;
      }
    });
  }
  // Modal code removed as we now navigate to product details page instead

  // Initial fetches
  fetchCategories();
  fetchVendors(); // Fetch all vendors initially
});

// --- CSS for Supermarket Home Page ---
const homeStyle = document.createElement("style");
homeStyle.textContent = `
    .main-content {
        display: flex;
        gap: 2rem;
        padding: 2rem 0;
    }

    .categories-sidebar {
        flex: 0 0 250px;
        background-color: white;
        border-radius: 8px;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        padding: 1.5rem;
        height: fit-content;
        position: sticky;
        top: 20px;
    }

    .categories-sidebar h2 {
        margin-top: 0;
        margin-bottom: 1.5rem;
        color: var(--dark-color);
        border-bottom: 1px solid #eee;
        padding-bottom: 1rem;
    }

    .category-card {
        display: flex;
        align-items: center;
        padding: 1rem;
        margin-bottom: 0.75rem;
        background-color: #f9f9f9;
        border-radius: 6px;
        cursor: pointer;
        transition: background-color 0.2s, transform 0.2s;
        border: 1px solid #eee;
    }

    .category-card:hover {
        background-color: #eef;
        transform: translateY(-2px);
    }

    .category-card.active {
        background-color: var(--primary-color);
        color: white;
        border-color: var(--primary-color);
        box-shadow: 0 4px 8px rgba(62, 142, 208, 0.2);
    }

    .category-card.active .category-icon,
    .category-card.active .category-name {
        color: white;
    }

    .category-icon {
        font-size: 1.5rem;
        color: var(--primary-color);
        margin-right: 1rem;
        width: 30px; /* Fixed width for alignment */
        text-align: center;
    }

    .category-name {
        font-weight: 500;
        font-size: 1.1rem;
        color: var(--dark-color);
    }

    .products-section {
        flex: 1;
    }

    .filter-controls {
        background-color: white;
        border-radius: 8px;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        padding: 1.5rem;
        margin-bottom: 2rem;
        display: flex;
        gap: 1rem;
        align-items: center;
    }

    .filter-controls label {
        font-weight: 500;
        color: var(--dark-color);
    }

    .filter-controls select {
        padding: 0.75rem;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 1rem;
        flex: 1;
        max-width: 300px;
    }

    .filter-controls button {
        background-color: var(--primary-color);
        color: white;
        border: none;
        border-radius: 4px;
        padding: 0.75rem 1.5rem;
        font-size: 1rem;
        cursor: pointer;
        transition: background-color 0.2s;
    }

    .filter-controls button:hover {
        background-color: #347bb5;
    }

    .products-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
        gap: 1.5rem;
    }

    .product-card {
        background-color: white;
        border-radius: 8px;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        overflow: hidden;
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        padding-bottom: 1rem;
        transition: transform 0.2s ease-in-out;
    }

    .product-card:hover {
        transform: translateY(-5px);
    }

    .product-card img {
        width: 100%;
        height: 200px;
        object-fit: cover;
        margin-bottom: 1rem;
    }

    .product-name {
        font-size: 1.2rem;
        color: var(--dark-color);
        margin: 0 1rem 0.5rem;
        text-overflow: ellipsis;
        white-space: nowrap;
        overflow: hidden;
        width: calc(100% - 2rem);
    }

    .product-price {
        font-size: 1.3rem;
        font-weight: bold;
        color: var(--primary-color);
        margin-bottom: 0.75rem;
    }

    .product-vendor {
        font-size: 0.9rem;
        color: #666;
        margin-bottom: 1rem;
    }

    .product-actions {
        display: flex;
        gap: 0.5rem;
        padding: 0 1rem;
        width: 100%;
        box-sizing: border-box;
    }

    .product-card .add-to-cart-btn,
    .product-card .view-product-btn {
        flex: 1;
        padding: 0.75rem 1rem;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 1rem;
        font-weight: 500;
        transition: background-color 0.2s, color 0.2s;
    }

    .product-card .add-to-cart-btn {
        background-color: var(--accent-color);
        color: white;
    }

    .product-card .add-to-cart-btn:hover {
        background-color: #e09400;
    }

    .product-card .view-product-btn {
        background-color: var(--primary-color);
        color: white;
    }

    .product-card .view-product-btn:hover {
        background-color: #347bb5;
    }

    .empty-state, .loading-spinner, .error {
        text-align: center;
        padding: 3rem;
        color: #888;
        font-size: 1.2rem;
    }

    .empty-state i, .loading-spinner i, .error i {
        font-size: 3rem;
        margin-bottom: 1rem;
        color: #ccc;
    }

    .loading-spinner i {
        color: var(--primary-color);
    }

    .error i {
        color: var(--danger-color);
    }

    /* Product Modal Styles */
    .product-modal {
        display: none; /* Hidden by default */
        position: fixed; /* Stay in place */
        z-index: 1000; /* Sit on top */
        left: 0;
        top: 0;
        width: 100%; /* Full width */
        height: 100%; /* Full height */
        overflow: auto; /* Enable scroll if needed */
        background-color: rgba(0,0,0,0.4); /* Black w/ opacity */
        justify-content: center;
        align-items: center;
    }

    .modal-content {
        background-color: #fefefe;
        margin: auto;
        padding: 20px;
        border-radius: 8px;
        box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        max-width: 700px;
        width: 90%;
        position: relative;
    }

    .close-modal {
        color: #aaa;
        float: right;
        font-size: 28px;
        font-weight: bold;
        position: absolute;
        right: 20px;
        top: 10px;
    }

    .close-modal:hover,
    .close-modal:focus {
        color: black;
        text-decoration: none;
        cursor: pointer;
    }

    .modal-body {
        padding-top: 20px;
    }

    .product-detail-content {
        display: flex;
        gap: 2rem;
        align-items: flex-start;
    }

    .product-detail-image {
        width: 250px;
        height: 250px;
        object-fit: cover;
        border-radius: 8px;
    }

    .product-detail-info {
        flex: 1;
    }

    .product-detail-name {
        font-size: 1.8rem;
        margin-top: 0;
        margin-bottom: 0.5rem;
        color: var(--dark-color);
    }

    .product-detail-category,
    .product-detail-vendor {
        font-size: 0.95rem;
        color: #777;
        margin-bottom: 0.5rem;
    }

    .product-detail-description {
        margin-top: 1rem;
        margin-bottom: 1.5rem;
        line-height: 1.6;
        color: #555;
    }

    .product-detail-price {
        font-size: 1.6rem;
        font-weight: bold;
        color: var(--primary-color);
        margin-bottom: 1.5rem;
    }    .product-detail-quantity {
        font-size: 1rem;
        color: #555;
        margin-bottom: 1rem;
    }

    .add-to-cart-modal-btn {
        background-color: var(--accent-color);
        color: white;
    }
    
    .out-of-stock-btn,
    .out-of-stock-modal-btn {
        background-color: #ccc;
        color: #666;
        cursor: not-allowed;
    }
        border: none;
        border-radius: 4px;
        padding: 1rem 2rem;
        font-size: 1.1rem;
        font-weight: 500;
        cursor: pointer;
        transition: background-color 0.2s;
        width: auto; /* Adjust to content */
    }

    .add-to-cart-modal-btn:hover {
        background-color: #e09400;
    }

    /* Media queries for responsive design */
    @media screen and (max-width: 992px) {
        .main-content {
            flex-direction: column;
            padding: 1rem;
        }

        .categories-sidebar {
            width: 100%;
            position: static;
        }

        .filter-controls {
            flex-direction: column;
            align-items: stretch;
        }

        .filter-controls select,
        .filter-controls button {
            max-width: none;
            width: 100%;
        }

        .product-detail-content {
            flex-direction: column;
            align-items: center;
            text-align: center;
        }

        .product-detail-image {
            width: 80%;
            height: auto;
            max-width: 250px;
            margin-bottom: 1rem;
        }
    }

    @media screen and (max-width: 576px) {
        .modal-content {
            padding: 15px;
        }
        .product-detail-name {
            font-size: 1.5rem;
        }
        .product-detail-price {
            font-size: 1.3rem;
        }
        .add-to-cart-modal-btn {
            padding: 0.8rem 1.5rem;
            font-size: 1rem;
        }
    }
`;
document.head.appendChild(homeStyle);

/**
   * Display star rating based on the given rating value
   * @param {number} rating The rating value (0-5)
   * @returns {string} HTML string with star icons
   */
  function displayRatingStars(rating) {
    if (!rating || isNaN(rating)) rating = 0;
    
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5 ? 1 : 0;
    const emptyStars = 5 - fullStars - halfStar;
    
    let starsHtml = '';
    
    // Add full stars
    for (let i = 0; i < fullStars; i++) {
      starsHtml += '<i class="fas fa-star"></i>';
    }
    
    // Add half star if needed
    if (halfStar) {
      starsHtml += '<i class="fas fa-star-half-alt"></i>';
    }
    
    // Add empty stars
    for (let i = 0; i < emptyStars; i++) {
      starsHtml += '<i class="far fa-star"></i>';
    }
    
    return starsHtml;
  }
