// supermarket-basket.js - JavaScript for the supermarket shopping basket

// Wait for DOM to be fully loaded
document.addEventListener("DOMContentLoaded", function () {
  // --- DOM Element Selections ---
  const cartContainer = document.getElementById("cartContainer");
  const emptyCartState = document.getElementById("emptyCartState");
  const cartSummary = document.getElementById("cartSummary");
  const cartSubtotal = document.getElementById("cartSubtotal");
  const cartShipping = document.getElementById("cartShipping");
  const cartTotal = document.getElementById("cartTotal");
  const shippingAddressInput = document.getElementById("shippingAddress");
  const paymentMethodSelect = document.getElementById("paymentMethod");
  const checkoutBtn = document.getElementById("checkoutBtn");
  const orderConfirmationModal = document.getElementById(
    "orderConfirmationModal"
  );
  const confirmedOrderId = document.getElementById("confirmedOrderId");
  const continueShoppingBtn = document.getElementById("continueShoppingBtn");
  const closeModal = document.querySelector(".close-modal");
  const cartBadge = document.getElementById("cartBadge");

  // Constants
  const SHIPPING_FEE = 5.0; // Fixed shipping fee

  // Load cart items from localStorage
  let cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];

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
   * Render cart items and update cart summary
   */
  function renderCart() {
    // Update cart badge
    updateCartBadge();

    // Check if cart is empty
    if (cartItems.length === 0) {
      emptyCartState.style.display = "flex";
      cartSummary.style.display = "none";
      cartContainer.innerHTML = "";
      return;
    }

    // Hide empty state and show summary
    emptyCartState.style.display = "none";
    cartSummary.style.display = "block";

    // Clear cart container
    cartContainer.innerHTML = "";

    // Create cart items list
    const cartItemsList = document.createElement("div");
    cartItemsList.className = "cart-items";

    // Group by vendor for better organization
    const itemsByVendor = {};

    cartItems.forEach((item) => {
      const vendorName = item.vendorName || "Unknown Vendor";
      if (!itemsByVendor[vendorName]) {
        itemsByVendor[vendorName] = [];
      }
      itemsByVendor[vendorName].push(item);
    });

    // Create vendor sections
    for (const [vendorName, items] of Object.entries(itemsByVendor)) {
      const vendorSection = document.createElement("div");
      vendorSection.className = "vendor-section";
      vendorSection.innerHTML = `
                <div class="vendor-header">
                    <h3>${vendorName}</h3>
                </div>
                <div class="vendor-items"></div>
            `;

      const vendorItemsContainer = vendorSection.querySelector(".vendor-items");

      // Add items to vendor container
      items.forEach((item) => {
        const itemElement = document.createElement("div");
        itemElement.className = "cart-item";
        itemElement.innerHTML = `
                    <div class="cart-item-image">
                        <img src="${
                          item.imageUrl || "https://via.placeholder.com/80"
                        }" alt="${item.name}">
                    </div>
                    <div class="cart-item-info">
                        <h3 class="cart-item-name">${item.name}</h3>
                        <div class="cart-item-category">${
                          item.category || "Uncategorized"
                        }</div>
                        <div class="cart-item-price">$${parseFloat(
                          item.price
                        ).toFixed(2)}</div>
                    </div>                    <div class="cart-item-quantity">
                        <button class="quantity-btn decrease" data-product-id="${
                          item.id
                        }">-</button>
                        <input type="number" class="quantity-input" value="${
                          item.quantity
                        }" min="0" max="${
          item.maxQuantity || 99
        }" data-product-id="${item.id}">
                        <button class="quantity-btn increase" data-product-id="${
                          item.id
                        }" ${
          item.quantity >= (item.maxQuantity || 99) ? "disabled" : ""
        }>+</button>
                    </div>
                    <div class="cart-item-total">
                        $${parseFloat(item.price * item.quantity).toFixed(2)}
                    </div>
                    <button class="remove-item-btn" data-product-id="${
                      item.id
                    }">
                        <i class="fas fa-trash"></i>
                    </button>
                `;

        vendorItemsContainer.appendChild(itemElement);
      });

      cartItemsList.appendChild(vendorSection);
    }

    cartContainer.appendChild(cartItemsList);

    // Add event listeners to cart item buttons
    attachCartItemEvents();

    // Update cart summary
    updateCartSummary();
  }

  /**
   * Attach event listeners to cart item elements
   */
  function attachCartItemEvents() {
    // Decrease quantity buttons
    document.querySelectorAll(".quantity-btn.decrease").forEach((button) => {
      button.addEventListener("click", function () {
        const productId = this.getAttribute("data-product-id");
        updateItemQuantity(productId, -1);
      });
    });

    // Increase quantity buttons
    document.querySelectorAll(".quantity-btn.increase").forEach((button) => {
      button.addEventListener("click", function () {
        const productId = this.getAttribute("data-product-id");
        updateItemQuantity(productId, 1);
      });
    }); // Quantity input fields
    document.querySelectorAll(".quantity-input").forEach((input) => {
      input.addEventListener("change", function () {
        const productId = this.getAttribute("data-product-id");
        const newQuantity = parseInt(this.value);

        // Allow quantity to be 0 for removal
        if (isNaN(newQuantity) || newQuantity < 0) {
          this.value = 0;
          updateItemQuantity(productId, 0, 0);
        } else {
          updateItemQuantity(productId, 0, newQuantity);
        }
      });
    });

    // Remove item buttons
    document.querySelectorAll(".remove-item-btn").forEach((button) => {
      button.addEventListener("click", function () {
        const productId = this.getAttribute("data-product-id");
        removeCartItem(productId);
      });
    });
  }
  /**
   * Update cart item quantity
   */
  async function updateItemQuantity(productId, change, absoluteValue = null) {
    // Find item in cart - use string comparison for consistent ID handling
    const itemIndex = cartItems.findIndex(
      (item) => String(item.id) === String(productId)
    );

    if (itemIndex === -1) return;

    const currentProduct = cartItems[itemIndex];
    let newQuantity;

    // Calculate new quantity
    if (absoluteValue !== null) {
      newQuantity = absoluteValue;
    } else {
      newQuantity = currentProduct.quantity + change;
    }

    // If quantity is 0 or less, remove the item
    if (newQuantity <= 0) {
      removeCartItem(productId); // Call existing remove function
      return; // Exit to prevent further processing for this item
    }

    // Check product stock before updating quantity
    try {
      // Fetch the current product to check available stock
      const response = await fetch(`/api/my-products/${productId}`);

      if (!response.ok) {
        throw new Error("Failed to fetch product details");
      }

      const productDetails = await response.json();
      const availableStock = productDetails.quantity || 0; // Limit quantity to available stock
      if (newQuantity > availableStock) {
        customAlert.warning(
          `Only ${availableStock} items available in stock.`,
          "Stock Limit"
        );
        newQuantity = availableStock;
      }

      // Update quantity
      cartItems[itemIndex].quantity = newQuantity;

      // Save updated cart to localStorage
      localStorage.setItem("cartItems", JSON.stringify(cartItems));

      // Re-render cart
      renderCart();
    } catch (error) {
      console.error("Error checking product stock:", error);
      // Update quantity anyway but log the error
      cartItems[itemIndex].quantity = newQuantity;
      localStorage.setItem("cartItems", JSON.stringify(cartItems));
      renderCart();
    }
  }
  /**
   * Remove item from cart
   */
  function removeCartItem(productId) {
    // Filter out the item - use string comparison for consistent ID handling
    cartItems = cartItems.filter(
      (item) => String(item.id) !== String(productId)
    );

    // Save updated cart to localStorage
    localStorage.setItem("cartItems", JSON.stringify(cartItems));

    // Re-render cart
    renderCart();
  }

  /**
   * Update cart summary calculations
   */
  function updateCartSummary() {
    // Calculate subtotal
    const subtotal = cartItems.reduce((total, item) => {
      return total + parseFloat(item.price) * item.quantity;
    }, 0);

    // Set shipping cost - could be dynamic based on order size
    const shipping = subtotal > 0 ? SHIPPING_FEE : 0;

    // Calculate total
    const total = subtotal + shipping;

    // Update UI
    cartSubtotal.textContent = `$${subtotal.toFixed(2)}`;
    cartShipping.textContent = `$${shipping.toFixed(2)}`;
    cartTotal.textContent = `$${total.toFixed(2)}`;
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
   * Process checkout
   */
  async function processCheckout() {
    // Validate form
    const shippingAddress = shippingAddressInput.value.trim();
    const paymentMethod = paymentMethodSelect.value;
    if (!shippingAddress) {
      customAlert.warning(
        "Please enter a shipping address",
        "Missing Information"
      );
      shippingAddressInput.focus();
      return;
    }

    if (!paymentMethod) {
      customAlert.warning(
        "Please select a payment method",
        "Missing Information"
      );
      paymentMethodSelect.focus();
      return;
    }

    // Disable checkout button and show loading state
    checkoutBtn.disabled = true;
    checkoutBtn.innerHTML =
      '<i class="fas fa-spinner fa-spin"></i> Processing...';

    try {
      // Calculate order totals
      const subtotal = cartItems.reduce((total, item) => {
        return total + parseFloat(item.price) * item.quantity;
      }, 0);

      const shipping = subtotal > 0 ? SHIPPING_FEE : 0;
      const total = subtotal + shipping;

      // Create order items structure
      console.log(cartItems);
      const orderItems = cartItems
        .map((item) => ({
          productId: parseInt(item.id, 10), // Ensure productId is an integer
          quantity: item.quantity,
        }))
        .filter((item) => !isNaN(item.productId)); // Filter out items with NaN productId
      //            console.log(orderItems);      // Check if there are any valid items to order
      if (orderItems.length === 0 && cartItems.length > 0) {
        customAlert.error(
          "Some items in your cart have invalid product IDs. Please remove them and try again.",
          "Invalid Cart Items"
        );
        // Re-enable checkout button
        checkoutBtn.disabled = false;
        checkoutBtn.innerHTML =
          '<i class="fas fa-credit-card"></i> Proceed to Checkout';
        return;
      } // Create order object
      const order = {
        items: orderItems,
        shippingAddress: shippingAddress,
        paymentMethod: paymentMethod,
        // Include these values for better order tracking
        subtotal: subtotal,
        shippingCost: shipping,
        total: total,
      };

      console.log(order);
      // Send order to server
      const response = await fetch("/api/vendor/orders/place", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(order),
      });
      console.log(response);

      if (!response.ok) {
        // Try to parse error as JSON if possible
        const errorText = await response.text();
        let errorMessage = "Failed to place order";
        try {
          const errorData = JSON.parse(errorText);
          errorMessage = errorData.message || errorMessage;
        } catch (e) {
          // If not JSON, use the error text
          errorMessage = errorText || errorMessage;
        }
        throw new Error(errorMessage);
      }

      // Try to parse the response as JSON
      let orderData = {};
      try {
        const responseText = await response.text();
        // Try to parse as JSON if possible
        try {
          orderData = JSON.parse(responseText);
          console.log("Response parsed as JSON:", orderData);
        } catch (e) {
          // Not JSON, handle as text response
          console.log("Response is text:", responseText);
          // Create a simple object with order message
          orderData = { message: responseText, id: "N/A" };
        }
      } catch (e) {
        console.log("Could not read response body:", e);
      }

      // Clear cart after successful order
      cartItems = [];
      localStorage.setItem("cartItems", JSON.stringify(cartItems)); // Show confirmation modal
      const orderIdContainer = document.getElementById("orderIdContainer");
      if (orderData.id) {
        confirmedOrderId.textContent = orderData.id;
        orderIdContainer.style.display = "block";
      } else {
        // Hide the order ID section if no ID is available
        orderIdContainer.style.display = "none";
      }
      orderConfirmationModal.style.display = "block";
    } catch (error) {
      console.error("Error placing order:", error);
      customAlert.error(
        `Order placement failed: ${error.message || "Please try again later"}`,
        "Order Failed"
      );

      // Re-enable checkout button
      checkoutBtn.disabled = false;
      checkoutBtn.innerHTML =
        '<i class="fas fa-credit-card"></i> Proceed to Checkout';
    }
  }

  // --- Event Listeners ---

  // Checkout button
  if (checkoutBtn) {
    checkoutBtn.addEventListener("click", processCheckout);
  }

  // Continue shopping button in confirmation modal
  if (continueShoppingBtn) {
    continueShoppingBtn.addEventListener("click", function () {
      orderConfirmationModal.style.display = "none";
      window.location.href = "/supermarket/home";
    });
  }

  // Modal close button
  if (closeModal) {
    closeModal.addEventListener("click", function () {
      orderConfirmationModal.style.display = "none";
      window.location.href = "/supermarket/orders";
    });
  }

  // Close modal when clicking outside
  window.addEventListener("click", function (event) {
    if (event.target === orderConfirmationModal) {
      orderConfirmationModal.style.display = "none";
      window.location.href = "/supermarket/orders";
    }
  });

  // --- CSS for Cart Page ---
  const style = document.createElement("style");
  style.textContent = `
        .cart-container {
            margin-bottom: 2rem;
        }

        .cart-items {
            display: flex;
            flex-direction: column;
            gap: 1.5rem;
        }

        .vendor-section {
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }

        .vendor-header {
            background-color: #f5f5f5;
            padding: 1rem 1.5rem;
            border-bottom: 1px solid #eaeaea;
        }

        .vendor-header h3 {
            margin: 0;
            color: var(--primary-color);
        }

        .vendor-items {
            padding: 1rem 0;
        }

        .cart-item {
            display: flex;
            align-items: center;
            padding: 1rem 1.5rem;
            border-bottom: 1px solid #eaeaea;
        }

        .cart-item:last-child {
            border-bottom: none;
        }

        .cart-item-image {
            flex: 0 0 80px;
        }

        .cart-item-image img {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border-radius: 4px;
        }

        .cart-item-info {
            flex: 1;
            padding: 0 1rem;
        }

        .cart-item-name {
            margin: 0 0 0.5rem;
            font-size: 1.1rem;
        }

        .cart-item-category {
            color: #666;
            font-size: 0.9rem;
            margin-bottom: 0.5rem;
        }

        .cart-item-price {
            font-weight: 500;
            color: var(--primary-color);
        }

        .cart-item-quantity {
            display: flex;
            align-items: center;
            margin: 0 2rem;
        }

        .cart-item-total {
            font-weight: bold;
            font-size: 1.1rem;
            color: var(--dark-color);
            margin-right: 1rem;
            width: 80px;
            text-align: right;
        }

        .remove-item-btn {
            background: none;
            border: none;
            color: var(--danger-color);
            cursor: pointer;
            font-size: 1.2rem;
            padding: 0.5rem;
            opacity: 0.7;
            transition: opacity 0.2s;
        }

        .remove-item-btn:hover {
            opacity: 1;
        }

        /* Cart Summary Styles */
        .cart-summary {
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            padding: 1.5rem;
            margin-top: 2rem;
        }

        .cart-summary h2 {
            margin-top: 0;
            margin-bottom: 1.5rem;
            padding-bottom: 1rem;
            border-bottom: 1px solid #eaeaea;
        }

        .summary-item {
            display: flex;
            justify-content: space-between;
            margin-bottom: 1rem;
        }

        .summary-item.total {
            border-top: 1px solid #eaeaea;
            margin-top: 1rem;
            padding-top: 1rem;
            font-weight: bold;
            font-size: 1.2rem;
        }

        .checkout-form {
            margin-top: 2rem;
        }

        .form-group {
            margin-bottom: 1.5rem;
        }

        .form-group label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 500;
        }

        .form-control {
            width: 100%;
            padding: 0.8rem;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 1rem;
        }

        .form-control:focus {
            outline: none;
            border-color: var(--primary-color);
            box-shadow: 0 0 0 2px rgba(62, 142, 208, 0.2);
        }

        .checkout-btn {
            background-color: var(--success-color);
            color: white;
            border: none;
            border-radius: 4px;
            padding: 1rem 2rem;
            font-size: 1.1rem;
            font-weight: 500;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
            width: 100%;
            transition: background-color 0.2s;
        }

        .checkout-btn:hover {
            background-color: #3baa77;
        }

        .checkout-btn:disabled {
            background-color: #aaa;
            cursor: not-allowed;
        }

        /* Order Confirmation Modal */
        .confirmation-message {
            text-align: center;
            margin: 2rem 0;
        }

        .confirmation-message i {
            font-size: 4rem;
            color: var(--success-color);
            margin-bottom: 1.5rem;
        }

        .confirmation-message p {
            font-size: 1.2rem;
            margin-bottom: 0.5rem;
        }

        .confirmation-actions {
            display: flex;
            gap: 1rem;
            margin-top: 2rem;
        }

        .confirmation-actions .btn {
            flex: 1;
            padding: 1rem;
            text-align: center;
            border-radius: 4px;
            text-decoration: none;
            font-weight: 500;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
        }

        .view-orders-btn {
            background-color: var(--primary-color);
            color: white;
        }

        .view-orders-btn:hover {
            background-color: #347bb5;
        }

        .continue-shopping-btn {
            background-color: var(--dark-color);
            color: white;
        }

        .continue-shopping-btn:hover {
            background-color: #4a4a4a;
        }

        /* Media queries for responsive design */
        @media screen and (max-width: 768px) {
            .cart-item {
                flex-wrap: wrap;
                gap: 1rem;
            }

            .cart-item-info {
                flex: 0 0 100%;
                padding: 0;
                order: -1;
            }

            .cart-item-quantity {
                margin: 0;
                flex: 1;
                justify-content: flex-start;
            }

            .cart-item-total {
                margin-right: 0;
                text-align: right;
            }

            .confirmation-actions {
                flex-direction: column;
            }
        }
    `;

  document.head.appendChild(style);

  // --- Initialize the Cart ---
  renderCart();
});
