// filepath: c:\Users\SourcesNet\Desktop\Sw_final\YallaMarket-\src\main\resources\static\js\profile.js
// Global variable to store the authenticated user's ID
// Initialize to null, it will be set after a successful fetch.
let currentUserId = null;

document.addEventListener("DOMContentLoaded", function () {

    // Mobile menu toggle functionality
    const menuToggle = document.getElementById('menuToggle');
    const navbarLinks = document.getElementById('navbarLinks');

    if (menuToggle && navbarLinks) {
        menuToggle.addEventListener('click', function() {
            navbarLinks.classList.toggle('active');
        });
    }

    // User menu dropdown functionality
    const userMenu = document.getElementById('userMenu');
    const userMenuButton = userMenu ? userMenu.querySelector('.user-menu-button') : null;

    if (userMenuButton) {
        userMenuButton.addEventListener('click', function() {
            userMenu.classList.toggle('open');
        });

        // Close the dropdown when clicking outside
        document.addEventListener('click', function(event) {
            if (!userMenu.contains(event.target)) {
                userMenu.classList.remove('open');
            }
        });
    }

  // --- Core Logic: Fetch and Display User Data on Load ---
  fetchUserProfileAndDisplay(); // Call this once the DOM is ready

  // --- Modal Button Event Listeners ---
  const editProfileBtn = document.getElementById("editProfileBtn");
  if (editProfileBtn) {
    editProfileBtn.addEventListener("click", showEditProfileModal);
  }
  // Change password functionality removed as requested

//  // Ensure this is uncommented if you have a Notification Settings button in your HTML
//  const notificationSettingsBtn = document.getElementById("notificationSettingsBtn");
//  if (notificationSettingsBtn) {
//    notificationSettingsBtn.addEventListener("click", showNotificationSettingsModal);
//  }
});

/**
 * Fetches user profile data from the backend (/users/me)
 * and populates the HTML elements.
 * Also stores the user ID in the global `currentUserId` variable for subsequent API calls.
 */
async function fetchUserProfileAndDisplay() {
  try {
    //    const token = localStorage.getItem('jwtToken'); // Get token from local storage

    const response = await fetch("/users/me", {
      method: "GET",
      headers: {
        //        'Authorization': `Bearer ${token}` // Include Authorization header
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      // Handle authentication/authorization issues explicitly
      if (response.status === 401 || response.status === 403) {
        console.warn("Authentication required or forbidden. Redirecting to login.");
        window.location.href = "/login"; // Redirect to login page
        return; // Stop execution
      }
      // For any other HTTP error (e.g., 404, 500 from the API itself)
      throw new Error(`Failed to fetch user profile: HTTP status ${response.status}`);
    }


    const userData = await response.json();
    console.log(userData);
    console.log("Fetched User Data:", userData); // Log fetched data for debugging

    // --- Store the user ID ---
    currentUserId = userData.id;
    console.log("Current User ID stored:", currentUserId); // Confirm ID is stored

    // --- Populate Basic Information ---
    // Use || "N/A" for fallbacks in case a field is missing or null
    document.getElementById("profile-username").textContent = userData.username || "N/A";
    document.getElementById("profile-role").textContent = getRoleName(userData.role);
    document.getElementById("profile-email").textContent = userData.email || "N/A";
    document.getElementById("profile-contactInfo").textContent = userData.contactInfo || "N/A";
    document.getElementById("profile-address").textContent = userData.address || "N/A";

    // --- Update Navigation based on role ---
    const navbarLinks = document.getElementById('navbarLinks');
    if (navbarLinks) {
      // Clear existing links
      navbarLinks.innerHTML = '';

      if (userData.role === 1) { // Vendor
        navbarLinks.innerHTML = `
          <li><a href="/products">My Products</a></li>
          <li><a href="/vendor-orders">Orders</a></li>
          <li><a href="/stats">Refunds</a></li>
        `;
      } else if (userData.role === 2) { // Supermarket
        navbarLinks.innerHTML = `
          <li><a href="/supermarket/home">Home</a></li>
          <li><a href="/supermarket/orders">Orders</a></li>
          <li>
            <a href="/supermarket/basket" class="cart-link">
              <i class="fas fa-shopping-cart"></i>
              <span class="cart-badge" id="cartBadge">0</span>
            </a>
          </li>
        `;

        // Update cart badge if we're in supermarket view
        const cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
        const totalItems = cartItems.reduce((total, item) => total + item.quantity, 0);
        const cartBadge = document.getElementById("cartBadge");
        if (cartBadge) {
          cartBadge.textContent = totalItems;
          cartBadge.style.display = totalItems > 0 ? "flex" : "none";
        }
      }
    }

    // Also update the logo link based on role
    const logoLink = document.querySelector('.navbar-logo');
    if (logoLink) {
      logoLink.href = userData.role === 2 ? '/supermarket/home' : '/products';
    }

    // For vendor users, show statistics section and load data
    if (userData.role === 1) { // Vendor
      // The original issue was here: 'try' block was opened without a closing brace before its own catch.
      // This 'try...catch' block belongs specifically to the vendor statistics loading.
      try {
        // Add statistics section dynamically for vendor users
        const profileContent = document.querySelector('.profile-content');
        const accountSettingsSection = document.querySelector('.profile-content .profile-section:last-child');
        // Create statistics section
        const statsSection = document.createElement('div');
        statsSection.className = 'profile-section';
        statsSection.innerHTML = `
          <h2><i class="fas fa-chart-line"></i> Account Statistics</h2>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon products">
                <i class="fas fa-box"></i>
              </div>
              <div class="stat-info">
                <h3>Total Products</h3>
                <p id="stats-totalProducts">0</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon orders">
                <i class="fas fa-shopping-cart"></i>
              </div>
              <div class="stat-info">
                <h3 id="stats-ordersLabel">Orders</h3>
                <p id="stats-totalOrders">0</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon revenue">
                <i class="fas fa-dollar-sign"></i>
              </div>
              <div class="stat-info">
                <h3 id="stats-revenueLabel">Revenue</h3>
                <p id="stats-totalRevenue">$0.00</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon rating">
                <i class="fas fa-star"></i>
              </div>
              <div class="stat-info">
                <h3>Vendor Rating</h3>
                <p id="stats-rating">0.0/5</p>
              </div>
            </div>
          </div>
        `;

        // Insert statistics section before account settings
        if (accountSettingsSection && profileContent) {
          profileContent.insertBefore(statsSection, accountSettingsSection);

          // Load statistics data
          await loadVendorStatistics(currentUserId);
        }
      } catch (error) { // This catch block now correctly closes the 'try' above it.
        console.error("Error loading vendor statistics:", error);
      }
    }
    // Statistics section is not shown for supermarket users

  } catch (error) { // This is the catch block for the outer `WorkspaceUserProfileAndDisplay` function.
    console.error("Critical error loading user profile:", error);
    // Display robust error messages on the page if data cannot be loaded
    document.getElementById("profile-username").textContent = "Error loading profile data";
    document.getElementById("profile-role").textContent = ""; // Clear role
    document.getElementById("profile-email").textContent = "Failed to load profile. Please ensure you are logged in and refresh.";
    document.getElementById("profile-contactInfo").textContent = "";
    document.getElementById("profile-address").textContent = "";
    //    document.getElementById("profile-memberSince").textContent = "";    // Only clear stats for vendor users if they exist
    // You cannot access userData.role here because userData might not be defined if the outer try failed before it was assigned.
    // So, this part needs to be careful or moved.
    // For now, I'll keep the check but be aware it might not always work.
    // A more robust approach might be to set a flag.
    // Assuming 'userData' would only be undefined if the *initial* fetch failed.
    // If the initial fetch succeeded, 'userData' exists, and we only reach this catch if a later operation (like DOM manipulation) failed.
    // However, the current structure means 'userData' might be undefined if the `await response.json()` or `Workspace` itself threw an error.

    // A safer way to handle this might be to wrap the entire try block inside an if(userData) or similar.
    // For now, removing the conditional check as it might cause an error if userData is null.
    // Instead, just try to clear elements if they exist.
    const statsElements = ["stats-totalProducts", "stats-totalOrders", "stats-totalRevenue", "stats-rating"];
    statsElements.forEach(id => {
      const element = document.getElementById(id);
      if (element) element.textContent = "N/A";
    });


    // Optionally disable edit buttons if data couldn't load
    if (document.getElementById("editProfileBtn")) document.getElementById("editProfileBtn").disabled = true;
    // We've removed the Change Password button functionality
  }
}
/**
 * Loads vendor statistics from the backend API
 */
async function loadVendorStatistics(vendorId) {
  try {
    const url = `/api/vendor/stats/${vendorId}`;
    
    // Add Authorization header if your endpoint is secured
//    const token = localStorage.getItem('jwtToken');
//    const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
    const headers = {}
    const response = await fetch(url, { headers });
    
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || `HTTP error! status: ${response.status}`);
    }
    
    // Parse the JSON response from the backend
    const backendData = await response.json();
    // Map backend DTO properties to frontend variables, with fallbacks
    const totalProducts = backendData.totalProductsListed !== undefined ? backendData.totalProductsListed : 0;
    const totalOrders = backendData.totalOrdersReceived !== undefined ? backendData.totalOrdersReceived : 0;
    const totalRevenue = backendData.totalRevenue !== undefined ? backendData.totalRevenue : 0.00;
    
    // Fetch vendor rating separately from the ratings API
    let vendorRating = 0;
    try {
      const ratingResponse = await fetch(`/api/ratings/vendor/${vendorId}/average`, { headers });
      if (ratingResponse.ok) {
        const ratingData = await ratingResponse.json();
        vendorRating = ratingData.rating || 0;
      }
    } catch (ratingError) {
      console.error("Error fetching vendor rating:", ratingError);
    }
    
    // Update the display with vendor data (isVendor = true)
    updateStatsDisplay(totalProducts, totalOrders, totalRevenue, vendorRating, true);
  } catch (error) {
    console.error("Error fetching vendor statistics:", error);
    // Update with default values
    updateStatsDisplay(0, 0, 0, 0, true);
  }
}

// Supermarket statistics functionality removed as requested

/**
 * Helper function to update the stats display on the profile page
 */
function updateStatsDisplay(totalProducts, totalOrders, totalAmount, rating, isVendor) {
  try {
    // Update HTML elements with the data
    const statsTotalProductsElement = document.getElementById("stats-totalProducts");
    if (statsTotalProductsElement) {
      statsTotalProductsElement.textContent = totalProducts;
    }

    const statsTotalOrdersElement = document.getElementById("stats-totalOrders");
    if (statsTotalOrdersElement) {
      statsTotalOrdersElement.textContent = totalOrders;
    }

    const statsTotalRevenueElement = document.getElementById("stats-totalRevenue");
    if (statsTotalRevenueElement) {
      statsTotalRevenueElement.textContent = `$${totalAmount.toFixed(2)}`;
    }

    // Update the rating display with real data from vendor_ratings table
    const statsRatingElement = document.getElementById("stats-rating");
    if (statsRatingElement) {
      // Format to one decimal place
      const formattedRating = (Math.round(rating * 10) / 10).toFixed(1);
      statsRatingElement.textContent = `${formattedRating}/5`;
    }

    // Adjust labels based on isVendor flag
    const statsOrdersLabelElement = document.getElementById("stats-ordersLabel");
    if (statsOrdersLabelElement) {
      statsOrdersLabelElement.textContent = isVendor ? 'Orders Received' : 'Orders Placed';
    }

    const statsRevenueLabelElement = document.getElementById("stats-revenueLabel");
    if (statsRevenueLabelElement) {
      statsRevenueLabelElement.textContent = isVendor ? 'Total Revenue' : 'Total Spent';
    }
  } catch (err) {
    console.error("Error updating stats display:", err);
  }
}

/**
 * Helper function to convert numeric role ID to a readable string.
 */
function getRoleName(roleId) {
  switch (roleId) {
    case 1:
      return "Vendor";
    case 2:
      return "Supermarket"; // Changed from "Customer" to "Supermarket"
    default:
      return "User"; // Default if role is unknown
  }
}

// --- Modal Creation and Handling Functions ---

function createModal(title, content) {
  // Remove any existing modals to prevent duplicates
  const existingModal = document.querySelector(".modal");
  if (existingModal) {
    existingModal.remove();
  }

  const modal = document.createElement("div");
  modal.className = "modal";
  modal.style.display = "block"; // Make sure it's visible by default

  modal.innerHTML = `
        <div class="modal-content">
            <button class="modal-close">&times;</button>
            <h2>${title}</h2>
            ${content}
        </div>
    `;

  // Add close functionality for the 'x' button
  const closeBtn = modal.querySelector(".modal-close");
  if (closeBtn) {
    closeBtn.addEventListener("click", () => modal.remove());
  }

  // Close modal when clicking outside the modal content
  modal.addEventListener("click", function (e) {
    if (e.target === modal) { // Only close if the click is directly on the modal background
      modal.remove();
    }
  });

  document.body.appendChild(modal); // Add modal to the document body
  return modal;
}

function showEditProfileModal() {
  // Pre-fill modal with current displayed values (which came from the last fetch)
  const currentUsername = document.getElementById("profile-username").textContent;
  const currentEmail = document.getElementById("profile-email").textContent;
  const currentContactInfo = document.getElementById("profile-contactInfo").textContent;
  const currentAddress = document.getElementById("profile-address").textContent;

  const modal = createModal(
    "Edit Profile",
    `
        <form id="editProfileForm">
            <div class="form-group">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" value="${currentUsername}" required>
            </div>
            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" value="${currentEmail}" required>
            </div>
            <div class="form-group">
                <label for="contactInfo">Contact Info</label>
                <input type="text" id="contactInfo" name="contactInfo" value="${currentContactInfo}" required>
            </div>
            <div class="form-group">
                <label for="address">Address</label>
                <input type="text" id="address" name="address" value="${currentAddress}" required>
            </div>
            <button type="submit" class="btn save-btn">Save Changes</button>
        </form>
    `
  );

  const form = modal.querySelector("form");
  if (form) {
    form.addEventListener("submit", async function (e) {
      e.preventDefault(); // Prevent default form submission
      await updateProfile(new FormData(form)); // Call the update function
    });
  }
}

function showNotificationSettingsModal() {
  const modal = createModal(
    "Notification Settings",
    `
        <form id="notificationSettingsForm">
            <div class="form-group">
                <label>
                    <input type="checkbox" name="emailNotifications" checked>
                    Email Notifications
                </label>
            </div>
            <div class="form-group">
                <label>
                    <input type="checkbox" name="orderUpdates" checked>
                    Order Updates
                </label>
            </div>
            <div class="form-group">
                <label>
                    <input type="checkbox" name="promotions">
                    Promotional Messages
                </label>
            </div>
            <button type="submit" class="btn save-btn">Save Preferences</button>
        </form>
    `
  );

  const form = modal.querySelector("form");
  if (form) {
    form.addEventListener("submit", async function (e) {
      e.preventDefault();
      await updateNotificationSettings(new FormData(form));
    });
  }
}


// --- API Call Functions ---

/**
 * Updates the user's profile data via a PUT request to /users/{id}.
 * On success, refreshes the displayed profile data.
 * @param {FormData} formData - Form data containing the new profile information.
 */
async function updateProfile(formData) {
  if (!currentUserId) {
    alert("Error: User ID not found for profile update. Please refresh the page.");
    console.error("Attempted to update profile without a valid currentUserId.");
    return;
  }

  try {
    const token = localStorage.getItem('jwtToken');
    const url = `/users/${currentUserId}`; // Use full URL with dynamic ID

    const response = await fetch(url, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        'Authorization': `Bearer ${token}` // Include Authorization header
      },
      body: JSON.stringify(Object.fromEntries(formData)), // Convert FormData to JSON object
    });

    if (response.ok) {
      alert("Profile updated successfully!");
      document.querySelector(".modal").remove(); // Close the modal
      window.location.reload(true); // Forces a reload from the server (true for hard reload)
    } else {
      const errorData = await response.json(); // Attempt to read error message from backend
      throw new Error(errorData.message || `Failed to update profile: Status ${response.status}`);
    }
  } catch (error) {
    console.error("Error updating profile:", error);
    alert("Failed to update profile: " + error.message);
  }
}

/**
 * Updates user's notification settings via a PUT request.
 * @param {FormData} formData - Form data containing notification preferences.
 */
async function updateNotificationSettings(formData) {
  if (!currentUserId) {
    alert("Error: User ID not found for notification settings. Please refresh the page.");
    console.error("Attempted to update notification settings without a valid currentUserId.");
    return;
  }

  try {
    const token = localStorage.getItem('jwtToken');
    // Adjust this URL to your actual notification settings endpoint that accepts user ID
    const url = `/users/${currentUserId}/notification-settings`; // Example endpoint structure

    const response = await fetch(url, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        emailNotifications: formData.get("emailNotifications") === "on", // Checkbox values are "on" or null
        orderUpdates: formData.get("orderUpdates") === "on",
        promotions: formData.get("promotions") === "on",
      }),
    });

    if (response.ok) {
      alert("Notification settings updated successfully!");
      document.querySelector(".modal").remove(); // Close modal
    } else {
      const errorData = await response.json();
      throw new Error(errorData.message || `Failed to update notification settings: Status ${response.status}`);
    }
  } catch (error) {
    console.error("Error updating notification settings:", error);
    alert("Failed to update notification settings: " + error.message);
  }
}
