// src/main/resources/static/js/vendor-orders.js

document.addEventListener("DOMContentLoaded", function () {
  // --- DOM Element Selections ---
  const ordersList = document.getElementById("ordersList"); // Main container for orders
  const orderStatusFilterSelect = document.getElementById("orderStatusFilter"); // The status dropdown
  const filterOrdersButton = document.getElementById("filterOrdersButton"); // The filter button

  // Mobile menu toggle
  const menuToggle = document.getElementById("menuToggle");
  const navbarLinks = document.getElementById("navbarLinks");
  if (menuToggle && navbarLinks) {
    menuToggle.addEventListener("click", function () {
      navbarLinks.classList.toggle("active");
    });
  }

  // User menu dropdown functionality
  const userMenu = document.getElementById("userMenu");
  const userMenuButton = userMenu ? userMenu.querySelector(".user-menu-button") : null;

  if (userMenuButton) {
    userMenuButton.addEventListener("click", function () {
      userMenu.classList.toggle("open"); // Use 'open' class for visibility
    });

    // Close the dropdown when clicking outside
    document.addEventListener("click", function (event) {
      if (!userMenu.contains(event.target)) {
        userMenu.classList.remove("open");
      }
    });
  }

  /**
   * Fetches orders from the backend based on the selected status
   * and displays them in the ordersList container.
   * @param {string} statusCode - The status code (1-6) to filter by. Empty string for all orders.
   */
  async function fetchAndDisplayOrders(statusCode = "") {
    ordersList.innerHTML = '<p>Loading orders...</p>'; // Show loading message

    let url = '/api/vendor/orders'; // Default: get all orders
    if (statusCode) {
      url = `/api/vendor/orders/filter?status=${encodeURIComponent(statusCode)}`;
    }

    try {
      // Add Authorization header if your order endpoints are secured
      const token = localStorage.getItem('jwtToken'); // Assuming you store JWT in localStorage
      const headers = token ? { 'Authorization': `Bearer ${token}` } : {};

      const response = await fetch(url, { headers });

      if (!response.ok) {
        const errorText = await response.text(); // Get raw text for non-OK responses
        throw new Error(errorText || `HTTP error! status: ${response.status}`);
      }

      const orders = await response.json();
      renderOrders(orders); // Call function to render products
    } catch (error) {
      console.error("Error loading orders:", error);
      ordersList.innerHTML = `<div class="error-message">Failed to load orders: ${error.message || error}.</div>`;
    }
  }


  /**
   * Renders a list of orders into the ordersList container.
   * @param {Array<Object>} orders - An array of order objects.
   */
  function renderOrders(orders) {
    ordersList.innerHTML = ''; // Clear previous orders

    if (orders.length === 0) {
      // If no orders, inject the empty state HTML directly into ordersList
      ordersList.innerHTML = `
        <div class="empty">
            <i class="fas fa-box-open"></i>
            <p>No orders found matching your filter criteria.</p>
        </div>
      `;
      return;
    }

    ordersList.innerHTML = orders
      .map(
        (order) => {
          // Determine status class for styling
          const statusClass = `status-${order.status ? order.status.toUpperCase() : 'UNKNOWN'}`;

          return `
            <div class="product-card">
                <div class="product-info">
                    <h3>Order #${order.orderId || 'N/A'}</h3>
                    <p><strong>Status:</strong> <span class="order-status-display ${statusClass}">${order.status || 'N/A'}</span></p>
                    <p><strong>Total:</strong> $${
                      order.totalAmount !== null ? order.totalAmount.toFixed(2) : "N/A"
                    }</p>
                    <p><strong>Date:</strong> ${order.orderDate ? new Date(order.orderDate).toLocaleString() : "N/A"}</p>

                    <h4>Order Items:</h4>
                    <ul class="order-items-list">
                        ${
                          order.orderItems && order.orderItems.length > 0
                            ? order.orderItems
                                .map(
                                  (item) => `
                                <li>
                                    <strong>${item.productName || "N/A"}</strong>
                                    (Category: ${item.productCategory || "N/A"},
                                    Quantity: ${item.quantity || 0},
                                    Price: $${item.price ? item.price.toFixed(2) : 'N/A'})
                                </li>
                              `
                                )
                                .join("")
                            : "<li>No items in this order.</li>"
                        }
                    </ul>

                    <a href="/vendor-orders/${
                      order.orderId // Assuming 'id' is the primary key for order details
                    }" class="details-btn">View Details</a>
                </div>
            </div>
          `;
        }
      )
      .join("");
  }

  // --- Initial Page Load Actions ---
  // Fetch and display all orders when the page first loads
  fetchAndDisplayOrders();

  // --- Event Listeners for Filtering ---
  if (filterOrdersButton) {
    filterOrdersButton.addEventListener('click', () => {
      const selectedStatusCode = orderStatusFilterSelect.value;
      fetchAndDisplayOrders(selectedStatusCode);
    });
  }

  /**
   * Function to view a specific order with ID 15
   * Uses the endpoint /api/vendor/orders/15
   */
  async function viewSpecificOrder() {
    ordersList.innerHTML = '<p>Loading specific order...</p>'; // Show loading message
    
    try {
      // Add Authorization header if your order endpoints are secured
      const token = localStorage.getItem('jwtToken'); // Assuming you store JWT in localStorage
      const headers = token ? { 'Authorization': `Bearer ${token}` } : {};

      const response = await fetch('/api/vendor/orders/15', { headers });

      if (!response.ok) {
        const errorText = await response.text(); // Get raw text for non-OK responses
        throw new Error(errorText || `HTTP error! status: ${response.status}`);
      }

      const order = await response.json();
      // We're displaying a single order, so wrap it in an array for renderOrders
      renderSpecificOrder(order);
    } catch (error) {
      console.error("Error loading specific order:", error);
      ordersList.innerHTML = `<div class="error-message">Failed to load specific order: ${error.message || error}.</div>`;
    }
  }

  /**
   * Renders a specific order with more detailed information
   * @param {Object} order - An order object.
   */
  function renderSpecificOrder(order) {
    ordersList.innerHTML = ''; // Clear previous orders

    if (!order) {
      // If no order, inject the empty state HTML directly into ordersList
      ordersList.innerHTML = `
        <div class="empty">
            <i class="fas fa-box-open"></i>
            <p>No order found with ID 15.</p>
        </div>
      `;
      return;
    }

    // Determine status class for styling
    const statusClass = `status-${order.status ? order.status.toUpperCase() : 'UNKNOWN'}`;

    // Create a detailed order view
    const orderCard = document.createElement('div');
    orderCard.className = 'product-card detailed';
    orderCard.innerHTML = `
      <div class="product-info">
        <h3>Order #${order.orderId || 'N/A'}</h3>
        <p><strong>Status:</strong> <span class="order-status-display ${statusClass}">${order.status || 'N/A'}</span></p>
        <p><strong>Total:</strong> $${order.totalAmount !== null ? order.totalAmount.toFixed(2) : "N/A"}</p>
        <p><strong>Date:</strong> ${order.orderDate ? new Date(order.orderDate).toLocaleString() : "N/A"}</p>
        <p><strong>Store ID:</strong> ${order.storeId || 'N/A'}</p>
        
        <h4>Order Items:</h4>
        <ul class="order-items-list">
          ${
            order.orderItems && order.orderItems.length > 0
              ? order.orderItems
                  .map(
                    (item) => `
                  <li>
                    <strong>${item.productName || "N/A"}</strong>
                    <p>Category: ${item.productCategory || "N/A"}</p>
                    <p>Quantity: ${item.quantity || 0}</p>
                    <p>Price: $${item.priceEach ? item.priceEach.toFixed(2) : 'N/A'}</p>
                    <p>Subtotal: $${item.quantity && item.priceEach ? (item.quantity * item.priceEach).toFixed(2) : 'N/A'}</p>
                    <button class="view-product-btn" data-product-id="${item.productId}">View Product</button>
                  </li>
                `
                  )
                  .join("")
              : "<li>No items in this order.</li>"
          }
        </ul>
        
        <a href="/vendor-orders" class="btn back-btn">Back to All Orders</a>
      </div>
    `;

    ordersList.appendChild(orderCard);

    // Add event listeners to the view product buttons
    ordersList.querySelectorAll('.view-product-btn').forEach(button => {
      button.addEventListener('click', function() {
        const productId = this.getAttribute('data-product-id');
        // Navigate to product details page
        window.location.href = `/products/${productId}`;
      });
    });
  }

  // Add a button to view the specific order with ID 15
//  const viewSpecificOrderBtn = document.createElement('button');
//  viewSpecificOrderBtn.className = 'btn view-specific-btn';
//  viewSpecificOrderBtn.innerText = 'View Order #15';
//  viewSpecificOrderBtn.addEventListener('click', viewSpecificOrder);
//
  // Insert the button after the filter button
//  if (filterOrdersButton && filterOrdersButton.parentNode) {
//    filterOrdersButton.parentNode.insertBefore(viewSpecificOrderBtn, filterOrdersButton.nextSibling);
//  } else {
//    // If filter button doesn't exist, add to the beginning of the page
//    const firstElement = ordersList.parentNode.firstChild;
//    ordersList.parentNode.insertBefore(viewSpecificOrderBtn, firstElement);
//  }
}); // End DOMContentLoaded