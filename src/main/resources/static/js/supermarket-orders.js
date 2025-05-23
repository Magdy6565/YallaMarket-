// supermarket-orders.js - JavaScript for the supermarket orders page

// Wait for DOM to be fully loaded
document.addEventListener("DOMContentLoaded", function () {
  // --- DOM Element Selections ---
  const ordersContainer = document.getElementById("ordersContainer");
  const emptyState = document.getElementById("emptyState");
  const orderStatusFilter = document.getElementById("orderStatusFilter");
  const filterOrdersButton = document.getElementById("filterOrdersButton");
  const orderDetailModal = document.getElementById("orderDetailModal");
  const closeModal = document.querySelector(".close-modal");
  const orderModalBody = document.getElementById("orderModalBody");
  const cartBadge = document.getElementById("cartBadge");

  // Load cart items from localStorage and update badge
  const cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
  updateCartBadge();

  function updateCartBadge() {
    const totalItems = cartItems.reduce(
      (total, item) => total + item.quantity,
      0
    );
    cartBadge.textContent = totalItems;
    cartBadge.style.display = totalItems > 0 ? "flex" : "none";
  }

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
   * Fetch orders from the API with optional status filter
   */ async function fetchOrders(status = "") {
    // Show loading spinner
    ordersContainer.innerHTML =
      '<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> Loading orders...</div>';

    try {
      // Construct the URL with optional status filter
      let url = "/api/store/orders"; // Use vendor orders API
      if (status && status !== "") {
        url += `/filter?status=${status}`; // Filter vendor orders
      }
      console.log(url);
      const response = await fetch(url, {
        credentials: "include",
        headers: {
          Accept: "application/json",
        },
      });
      if (!response.ok) {
        throw new Error("Failed to fetch orders");
      }
      const orders = await response.json();
      console.log(orders);
      renderOrders(orders);
    } catch (error) {
      console.error("Error fetching orders:", error);
      ordersContainer.innerHTML = `
                <div class="error">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Failed to load orders. Please try again later.</p>
                </div>
            `;
    }
  }

  /**
   * Render orders in the container
   */ function renderOrders(orders) {
    if (!orders || orders.length === 0) {
      ordersContainer.innerHTML = "";
      emptyState.style.display = "flex";
      return;
    }

    emptyState.style.display = "none";
    ordersContainer.innerHTML = "";

    // Create orders list
    const ordersList = document.createElement("div");
    ordersList.className = "orders-list";

    orders.forEach((order) => {
      const orderDate = new Date(
        order.orderDate || Date.now()
      ).toLocaleDateString();

      // Determine status class for styling
      const statusMap = {
        PENDING: "pending",
        PLACED: "pending",
        APPROVED: "approved",
        SHIPPED: "shipped",
        DELIVERED: "delivered",
        CANCELLED: "cancelled",
        DENIED: "cancelled",
      };

      const statusClass = statusMap[order.status] || "pending";

      const orderCard = document.createElement("div");
      orderCard.className = "order-card";
      orderCard.innerHTML = `
                <div class="order-header">
                    <div class="order-id">Order #${order.orderId}</div>
                    <div class="order-status ${statusClass}">Status : ${
        order.status
      }</div>
                </div>
                <div class="order-info">
                    <div class="order-date">Ordered on: ${orderDate}</div>
                    <div class="order-total">Total: $${parseFloat(
                      order.totalAmount
                    ).toFixed(2)}</div>
                    <div class="order-items-count">${
                      order.itemCount || "Multiple"
                    } items</div>
                </div>                
                <div class="order-actions">
                    <button class="details-btn" data-order-id="${
                      order.orderId
                    }">
                        <i class="fas fa-info-circle"></i> View Details
                    </button>
                    <button class="invoice-btn" data-order-id="${
                      order.orderId
                    }">
                        <i class="fas fa-file-invoice"></i> Show Invoice
                    </button>
                </div>
            `;

      ordersList.appendChild(orderCard);
    });

    ordersContainer.appendChild(ordersList);

    // Add event listeners to details buttons
    document.querySelectorAll(".details-btn").forEach((button) => {
      button.addEventListener("click", function () {
        const orderId = this.getAttribute("data-order-id");
        openOrderDetailModal(orderId);
      });
    });

    // Add event listeners to invoice buttons
    document.querySelectorAll(".invoice-btn").forEach((button) => {
      button.addEventListener("click", function () {
        const orderId = this.getAttribute("data-order-id");
        openInvoiceModal(orderId);
      });
    });
  }
  /**
   * Open order detail modal with order info
   */
  async function openOrderDetailModal(orderId) {
    try {
      // Show loading in modal
      orderModalBody.innerHTML =
        '<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> Loading order details...</div>';

      // Open modal
      orderDetailModal.style.display = "block"; // Fetch order details with credentials: 'include' to ensure cookies are sent
      const response = await fetch(`/api/store/orders/${orderId}`, {
        // Use vendor orders endpoint
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch order details: ${response.status}`);
      }

      const order = await response.json();
      console.log("Order details:", order);

      // Format date
      const orderDate = new Date(
        order.orderDate || Date.now()
      ).toLocaleDateString();

      // Status class for styling
      const statusMap = {
        PENDING: "pending",
        PLACED: "pending",
        APPROVED: "approved",
        SHIPPED: "shipped",
        DELIVERED: "delivered",
        CANCELLED: "cancelled",
        DENIED: "cancelled",
      };

      const statusClass = statusMap[order.status] || "pending"; // Create content for modal
      let itemsHtml = "";
      if (order.orderItems && order.orderItems.length > 0) {
        itemsHtml = order.orderItems
          .map(
            (item) => `
                    <div class="order-item">
                        <div class="item-details">
                            <h4>${item.productName}</h4>
                            <div class="item-category">${
                              item.productCategory || "General"
                            }</div>
                            <div class="item-price-qty">
                                <span class="item-price">$${parseFloat(
                                  item.priceEach
                                ).toFixed(2)}</span>
                                <span class="item-qty">x${item.quantity}</span>
                            </div>
                        </div>
                        <div class="item-total">
                            $${parseFloat(
                              item.priceEach * item.quantity
                            ).toFixed(2)}
                        </div>
                        <div class="item-actions">
                            <button class="refund-btn" data-order-item-id="${
                              item.orderItemId
                            }" data-product-name="${item.productName}">
                                <i class="fas fa-undo"></i> Refund
                            </button>
                            <button class="view-product-btn" data-product-id="${
                              item.productId
                            }">
                                <i class="fas fa-eye"></i> View Product
                            </button>
                        </div>
                    </div>
                `
          )
          .join("");
      } else {
        itemsHtml = "<p>No items found for this order.</p>";
      }

      // Prepare invoice section HTML if invoice exists
      let invoiceHtml = "";
      if (order.invoice) {
        const issueDate = new Date(
          order.invoice.issueDate
        ).toLocaleDateString();
        const dueDate = new Date(order.invoice.dueDate).toLocaleDateString();

        invoiceHtml = `
          <div class="order-invoice-section">
            <h3>Invoice Details</h3>
            <div class="invoice-group"><strong>Invoice ID:</strong> ${
              order.invoice.invoiceId
            }</div>
            <div class="invoice-group"><strong>Issue Date:</strong> ${issueDate}</div>
            <div class="invoice-group"><strong>Due Date:</strong> ${dueDate}</div>
            <div class="invoice-group"><strong>Status:</strong> ${
              order.invoice.status
            }</div>
            ${
              order.invoice.pdfLink
                ? `<a href="${order.invoice.pdfLink}" target="_blank" class="btn">Download PDF</a>`
                : ""
            }
          </div>
        `;
      }

      // Render order details in modal
      orderModalBody.innerHTML = `
                <div class="order-detail">
                    <div class="order-detail-header">
                        <h2>Order #${order.orderId}</h2>
                        <div class="order-status ${statusClass}">${
        order.status
      }</div>
                    </div>
                    
                    <div class="order-detail-info">
                        <div class="order-info-group">
                            <div class="info-label">Order Date:</div>
                            <div class="info-value">${orderDate}</div>
                        </div>
                        <div class="order-info-group">
                            <div class="info-label">Store ID:</div>
                            <div class="info-value">${order.storeId}</div>
                        </div>
                        <div class="order-info-group">
                            <div class="info-label">Total Amount:</div>
                            <div class="info-value">$${parseFloat(
                              order.totalAmount
                            ).toFixed(2)}</div>
                        </div>
                    </div>
                    
                    <div class="order-items-section">
                        <h3>Order Items</h3>
                        <div class="order-items">
                            ${itemsHtml}
                        </div>
                    </div>
                    
                    <div class="order-detail-summary">
                        <div class="summary-row total">
                            <div class="summary-label">Total:</div>
                            <div class="summary-value">$${parseFloat(
                              order.totalAmount
                            ).toFixed(2)}</div>
                        </div>
                    </div>
                    
                    ${invoiceHtml}
                    
                    ${
                      order.status === "PLACED" || order.status === "PENDING"
                        ? `
                        <div class="order-detail-actions">
                            <button class="cancel-order-btn" data-order-id="${order.orderId}">
                                <i class="fas fa-times-circle"></i> Cancel Order
                            </button>
                        </div>
                    `
                        : ""
                    }
                </div>
            `; // Add event listener for cancel button if exists
      const cancelBtn = orderModalBody.querySelector(".cancel-order-btn");
      if (cancelBtn) {
        cancelBtn.addEventListener("click", function () {
          cancelOrder(order.orderId);
        });
      }

      // Add event listeners for refund buttons
      const refundBtns = orderModalBody.querySelectorAll(".refund-btn");
      refundBtns.forEach((btn) => {
        btn.addEventListener("click", function () {
          const orderItemId = this.getAttribute("data-order-item-id");
          const productName = this.getAttribute("data-product-name");
          processRefund(orderItemId, productName);
        });
      });

      // Add event listeners for view product buttons
      const viewProductBtns =
        orderModalBody.querySelectorAll(".view-product-btn");
      viewProductBtns.forEach((btn) => {
        btn.addEventListener("click", function () {
          const productId = this.getAttribute("data-product-id");
          viewProductDetails(productId);
        });
      });
    } catch (error) {
      console.error("Error loading order details:", error);
      orderModalBody.innerHTML = `
                <div class="error">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Failed to load order details. Please try again later.</p>
                </div>
            `;
    }
  }
  /**
   * Cancel an order
   */
  async function cancelOrder(orderId) {
    if (!confirm("Are you sure you want to cancel this order?")) {
      return;
    }

    try {
      const response = await fetch(`/api/store/orders/${orderId}/cancel`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include", // Include cookies for authentication if needed
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
          `Failed to cancel order: ${response.status} - ${errorText}`
        );
      } // Close modal and refresh orders list
      orderDetailModal.style.display = "none";
      fetchOrders(orderStatusFilter.value);

      // Show success message
      customAlert.success("Order cancelled successfully", "Order Cancelled");
    } catch (error) {
      console.error("Error cancelling order:", error);
      customAlert.error(
        "Failed to cancel order. Please try again later.",
        "Cancellation Failed"
      );
    }
  }
  /**
   * Process refund for an order item
   */
  async function processRefund(orderItemId, productName) {
    const reason = prompt(
      `Please provide a reason for refunding ${productName}:`,
      ""
    );
    if (!reason) return; // User cancelled

    // Show loading message
    const refundBtn = document.querySelector(
      `[data-order-item-id="${orderItemId}"]`
    );
    if (refundBtn) {
      const originalText = refundBtn.innerHTML;
      refundBtn.innerHTML =
        '<i class="fas fa-spinner fa-spin"></i> Processing...';
      refundBtn.disabled = true;
    }

    try {
      // Find the payment ID related to this order (in a real app, you'd get this from the order or API)
      // For now we use a placeholder ID of 2 as per the API requirements
      const paymentId = 2;

      const response = await fetch("/api/refunds", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include", // Include cookies for authentication
        body: JSON.stringify({
          orderItemId: orderItemId,
          paymentId: paymentId,
          reason: reason,
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
          `Failed to process refund: ${response.status} - ${errorText}`
        );
      }

      const result = await response.json();

      // Update the UI to reflect the refunded item
      const itemElement = document.querySelector(
        `.order-item:has([data-order-item-id="${orderItemId}"])`
      );
      if (itemElement) {
        itemElement.classList.add("refunded");
        itemElement.querySelector(".item-actions").innerHTML =
          '<span class="refund-status">Refund Requested</span>';
      }

      customAlert.success(
        "Refund request submitted successfully",
        "Refund Requested"
      );

      // After a short delay, refresh the order details to show the updated state
      setTimeout(() => {
        // Re-fetch the current order details
        const currentOrderId = new URLSearchParams(window.location.search).get(
          "orderId"
        );
        if (currentOrderId) {
          openOrderDetailModal(currentOrderId);
        } else {
          // Close modal and refresh orders list
          orderDetailModal.style.display = "none";
          fetchOrders(orderStatusFilter.value);
        }
      }, 1500);
    } catch (error) {
      console.error("Error processing refund:", error);
      customAlert.error(
        "Failed to process refund. Please try again later.",
        "Refund Failed"
      );

      // Reset button if it exists
      if (refundBtn) {
        refundBtn.innerHTML = '<i class="fas fa-undo"></i> Refund';
        refundBtn.disabled = false;
      }
    }
  }

  /**
   * Open invoice modal with order info
   */
  async function openInvoiceModal(orderId) {
    try {
      // Show loading in modal
      orderModalBody.innerHTML =
        '<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i> Loading invoice details...</div>';

      // Open modal
      orderDetailModal.style.display = "block";
      // Fetch order details with credentials: 'include' to ensure cookies are sent
      const response = await fetch(`/api/store/orders/${orderId}`, {
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch order details: ${response.status}`);
      }

      const order = await response.json();

      // Check if order has invoice
      if (!order.invoice) {
        orderModalBody.innerHTML =
          '<div class="error">No invoice available for this order.</div>';
        return;
      }

      // Format dates
      const issueDate = new Date(order.invoice.issueDate).toLocaleDateString();
      const dueDate = new Date(order.invoice.dueDate).toLocaleDateString();

      // Create invoice content
      orderModalBody.innerHTML = `
        <div class="invoice-detail">
          <div class="invoice-header">
            <h2>Invoice #${order.invoice.invoiceId}</h2>
            <div class="invoice-status">${order.invoice.status}</div>
          </div>
          
          <div class="invoice-info">
            <div class="invoice-info-group">
              <div class="info-label">Issue Date:</div>
              <div class="info-value">${issueDate}</div>
            </div>
            <div class="invoice-info-group">
              <div class="info-label">Due Date:</div>
              <div class="info-value">${dueDate}</div>
            </div>
            <div class="invoice-info-group">
              <div class="info-label">Total Amount:</div>
              <div class="info-value">$${parseFloat(
                order.invoice.total
              ).toFixed(2)}</div>
            </div>
            <div class="invoice-info-group">
              <div class="info-label">Order Reference:</div>
              <div class="info-value">Order #${order.orderId}</div>
            </div>
          </div>
          
          ${
            order.invoice.pdfLink
              ? `<div class="invoice-actions">
              <a href="${order.invoice.pdfLink}" target="_blank" class="btn">
                <i class="fas fa-file-pdf"></i> Download PDF
              </a>
            </div>`
              : `<div class="note">PDF download not available for this invoice.</div>`
          }
        </div>
      `;
    } catch (error) {
      console.error("Error loading invoice details:", error);
      orderModalBody.innerHTML = `
        <div class="error">
          <i class="fas fa-exclamation-circle"></i>
          <p>Failed to load invoice details. Please try again later.</p>
        </div>
      `;
    }
  }

  /**
   * View product details
   */
  function viewProductDetails(productId) {
    // Store the order ID if we're currently viewing an order detail
    const currentOrderId = document
      .querySelector(".order-detail h2")
      ?.textContent?.replace("Order #", "");

    // Navigate to the product details page
    window.location.href = `/supermarket/product/${productId}${
      currentOrderId ? `?returnTo=orders&orderId=${currentOrderId}` : ""
    }`;
  }

  // --- Event Listeners ---

  // Filter button click
  if (filterOrdersButton) {
    filterOrdersButton.addEventListener("click", function () {
      const status = orderStatusFilter.value;
      fetchOrders(status);
    });
  }

  // Modal close button
  if (closeModal) {
    closeModal.addEventListener("click", function () {
      orderDetailModal.style.display = "none";
    });
  }

  // Close modal when clicking outside
  window.addEventListener("click", function (event) {
    if (event.target === orderDetailModal) {
      orderDetailModal.style.display = "none";
    }
  });
  // --- Initial Setup ---
  fetchOrders();

  // Check if we need to open a specific order detail modal (when returning from product details)
  const urlParams = new URLSearchParams(window.location.search);
  const openOrderId = urlParams.get("openOrder");

  if (openOrderId) {
    // We need to wait for orders to load before opening the modal
    setTimeout(() => {
      openOrderDetailModal(openOrderId);
      // Clear the URL parameter to avoid reopening on page refresh
      window.history.replaceState({}, document.title, "/supermarket/orders");
    }, 1000); // Give time for orders to load
  }
});
