// products.js - JavaScript for the products page functionality

// Global variable to store the authenticated user's ID if needed for product filtering by user
// (Not directly used in this specific filtering logic, but good to have if your /my-products
// endpoint needs it, e.g., via a JWT token in headers)
let currentUserId = null;

// Wait for DOM to be fully loaded
document.addEventListener("DOMContentLoaded", function () {
  // --- DOM Element Selections ---
  // IMPORTANT CHANGE: We're now targeting the existing 'products-grid' div
  const productsGridContainer = document.querySelector(".products-grid");
  const categoryFilterSelect = document.getElementById("categoryFilter"); // The dropdown for categories
  const filterButton = document.getElementById("filterButton"); // The filter button

  // Mobile menu toggle functionality
  const menuToggle = document.getElementById("menuToggle");
  const navbarLinks = document.getElementById("navbarLinks");

  if (menuToggle && navbarLinks) {
    menuToggle.addEventListener("click", function () {
      navbarLinks.classList.toggle("active");
    });
  } // User menu / My Account dropdown functionality
  const userMenu = document.getElementById("userMenu");
  const userMenuButton = userMenu
    ? userMenu.querySelector(".user-menu-button")
    : null;

  if (userMenuButton) {
    // Toggle the dropdown when clicking the button (using class approach)
    userMenuButton.addEventListener("click", function (event) {
      event.preventDefault();
      event.stopPropagation(); // Prevent click from propagating to document
      userMenu.classList.toggle("open");
      console.log(
        "User menu toggled, open status:",
        userMenu.classList.contains("open")
      );
    });

    // Close the dropdown when clicking outside
    document.addEventListener("click", function (event) {
      if (userMenu && !userMenu.contains(event.target)) {
        userMenu.classList.remove("open");
      }
    });
  }

  // --- Core Functions for Products and Categories ---

  /**
   * Fetches products from the backend based on the selected category
   * and displays them in the productsGridContainer.
   * @param {string} category - The category to filter by. Empty string for all products.
   */
  async function fetchAndDisplayProducts(category = "") {
    // Clear existing products and show loading message
    productsGridContainer.innerHTML = "<p>Loading products...</p>";

    let url = "/api/my-products"; // Default: get all products
    if (category) {
      // If a category is selected, use the filter endpoint
      url = `/api/my-products/filter?category=${encodeURIComponent(category)}`;
    }

    try {
      // Add Authorization header if your product endpoints are secured
      const token = localStorage.getItem("jwtToken");
      const headers = token ? { Authorization: `Bearer ${token}` } : {};

      const response = await fetch(url, { headers });

      if (!response.ok) {
        const errorData = await response.json();
        console.error("Failed to fetch products:", errorData);
        productsGridContainer.innerHTML = `<p style="color: red;">Error loading products: ${
          errorData.message || "An unknown error occurred"
        }</p>`;
        return;
      }
      const products = await response.json();
      renderProducts(products); // Call function to render products
    } catch (error) {
      console.error("Network or unexpected error fetching products:", error);
      productsGridContainer.innerHTML = `<p style="color: red;">Failed to connect to the server or an unexpected error occurred.</p>`;
    }
  }

  /**
   * Renders a list of products into the productsGridContainer.
   * @param {Array<Object>} products - An array of product objects.
   */
  function renderProducts(products) {
    productsGridContainer.innerHTML = ""; // Clear previous products

    if (products.length === 0) {
      productsGridContainer.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-box-open"></i>
                    <h2>No products found</h2>
                    <p>No products match your filter. Try a different category or add new products.</p>
                    <a href="/add-product" class="btn">
                        <i class="fas fa-plus"></i> Add New Product
                    </a>
                </div>
            `;
      return;
    }

    products.forEach((product) => {
      const productCard = document.createElement("div");
      productCard.className = "product-card"; // Use your existing CSS class for styling

      // Build the HTML for the product card dynamically
      // Note: Thymeleaf's `th:text` etc. cannot be used directly in JS strings.
      // We use standard JS string interpolation.
      const quantityClass =
        product.quantity > 10
          ? "in-stock"
          : product.quantity > 0
          ? "low-stock"
          : "out-of-stock";
      const stockIcon =
        product.quantity > 10
          ? '<i class="fas fa-check-circle"></i>'
          : product.quantity > 0
          ? '<i class="fas fa-exclamation-circle"></i>'
          : '<i class="fas fa-times-circle"></i>';
      const stockText =
        product.quantity > 10
          ? "In Stock"
          : product.quantity > 0
          ? `Low Stock: ${product.quantity} left`
          : "Out of Stock";

      productCard.innerHTML = `
                <div class="product-image">
                    <img src="${
                      product.imageUrl || "https://via.placeholder.com/250"
                    }" alt="${product.name || "Product Image"}">
                </div>
                <div class="product-info">
                    <h3 class="product-name">${
                      product.name || "Product Name"
                    }</h3>
                    <span class="product-category">${
                      product.category || "N/A"
                    }</span>
                    <div class="product-price">$${
                      product.price ? product.price.toFixed(2) : "N/A"
                    }</div>

                    <div class="product-stock ${quantityClass}">
                        <span>${stockIcon} ${stockText}</span>
                    </div>

                    <div class="product-actions">
                        <a href="/products/${
                          product.productId
                        }" class="view-btn">
                            <i class="fas fa-eye"></i> View
                        </a>
                        <a href="/edit-product/${
                          product.productId
                        }" class="edit-btn">
                            <i class="fas fa-edit"></i> Edit
                        </a>
                        <button onclick="confirmDelete(${
                          product.productId
                        })" class="btn btn-sm btn-delete">
                            <i class="fas fa-trash"></i> Delete
                        </button>
                    </div>
                </div>
            `;
      productsGridContainer.appendChild(productCard);
    });

    // Re-apply hover effects to newly rendered product cards
    applyProductCardHoverEffects();
  }

  /**
   * Fetches distinct categories from the backend and populates the category filter dropdown.
   */
  async function populateCategoriesDropdown() {
    try {
      const response = await fetch("/api/my-products/categories"); // Endpoint for distinct categories
      if (!response.ok) {
        console.error("Failed to fetch categories:", await response.json());
        return;
      }
      const categories = await response.json();

      // Clear existing options except "All Categories"
      categoryFilterSelect.innerHTML =
        '<option value="">All Categories</option>';

      categories.forEach((category) => {
        if (category) {
          // Ensure category string is not null or empty
          const option = document.createElement("option");
          option.value = category;
          option.textContent = category;
          categoryFilterSelect.appendChild(option);
        }
      });
    } catch (error) {
      console.error("Network or unexpected error fetching categories:", error);
    }
  }

  /**
   * Applies hover effects to product cards.
   * This function is called initially and after products are re-rendered.
   */
  function applyProductCardHoverEffects() {
    const productCards = document.querySelectorAll(".product-card");
    productCards.forEach((card) => {
      // Remove existing listeners to prevent duplicates if called multiple times
      card.removeEventListener("mouseenter", handleMouseEnter);
      card.removeEventListener("mouseleave", handleMouseLeave);

      // Add new listeners
      card.addEventListener("mouseenter", handleMouseEnter);
      card.addEventListener("mouseleave", handleMouseLeave);
    });
  }

  function handleMouseEnter() {
    this.style.transform = "translateY(-5px)";
    this.style.boxShadow = "0 5px 15px rgba(0, 0, 0, 0.1)";
  }

  function handleMouseLeave() {
    this.style.transform = "translateY(0)";
    this.style.boxShadow = "0 2px 10px rgba(0, 0, 0, 0.1)";
  }

  // --- Initial Page Load Actions ---

  // 1. Populate the categories dropdown
  populateCategoriesDropdown();

  // 2. Fetch and display all products initially
  // This will replace the Thymeleaf-rendered products
  fetchAndDisplayProducts();

  // --- Event Listeners for Filtering ---

  // Event listener for the filter button click
  if (filterButton) {
    filterButton.addEventListener("click", () => {
      const selectedCategory = categoryFilterSelect.value;
      fetchAndDisplayProducts(selectedCategory);
    });
  }

  // Optional: Filter immediately when the dropdown selection changes
  // if (categoryFilterSelect) {
  //     categoryFilterSelect.addEventListener('change', () => {
  //         const selectedCategory = categoryFilterSelect.value;
  //         fetchAndDisplayProducts(selectedCategory);
  //     });
  // }
}); // End DOMContentLoaded

// --- Global Functions (accessible from HTML, e.g., onclick="confirmDelete(...)") ---

/**
 * Confirms product deletion and sends a DELETE request to the backend.
 * @param {number} productId - The ID of the product to delete.
 */
function confirmDelete(productId) {
  // Using custom confirm dialog for better UX and consistency
  customAlert
    .confirm(
      "Are you sure you want to delete this product?",
      "Delete Product",
      "This action cannot be undone.",
      "Delete",
      "Cancel"
    )
    .then((result) => {
      if (result) {
        // Send delete request to the API
        const token = localStorage.getItem("jwtToken");
        const headers = { "Content-Type": "application/json" };
        if (token) {
          headers["Authorization"] = `Bearer ${token}`;
        }

        fetch(`/api/my-products/${productId}`, {
          // Adjust endpoint if needed
          method: "DELETE",
          headers: headers,
        })
          .then((response) => {
            if (response.ok) {
              customAlert
                .success("Product deleted successfully!", "Success")
                .then(() => {
                  // Refresh the product list after deletion
                  // Reloads the entire page, which also re-runs fetchAndDisplayProducts
                  window.location.reload();
                });
            } else {
              // Try to parse error message from backend
              response
                .json()
                .then((errorData) => {
                  customAlert.error(
                    `Failed to delete product: ${
                      errorData.message || "An unknown error occurred"
                    }`,
                    "Delete Failed"
                  );
                })
                .catch(() => {
                  customAlert.error(
                    "Failed to delete product. An unknown error occurred.",
                    "Delete Failed"
                  );
                });
            }
          })
          .catch((error) => {
            console.error("Error deleting product:", error);
            customAlert.error(
              "An error occurred while deleting the product. Please try again.",
              "Error"
            );
          });
      }
    });
}
