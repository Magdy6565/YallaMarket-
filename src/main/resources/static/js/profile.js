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

  const changePasswordBtn = document.getElementById("changePasswordBtn");
  if (changePasswordBtn) {
    changePasswordBtn.addEventListener("click", showChangePasswordModal);
  }

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

    const response = await fetch("/users/me" , {
        method:"GET" ,
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

try {
    // Fetch data from the endpoint. No 'localhost' prefix needed if served from the same domain.
    const url = `/api/vendor/stats/${currentUserId}`;

    // Add Authorization header if your endpoint is secured (assuming JWT is stored in localStorage)
    const token = localStorage.getItem('jwtToken');
    const headers = token ? { 'Authorization': `Bearer ${token}` } : {};

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
    const rating = backendData.rating !== undefined ? backendData.rating : 0.0; // Assuming 'rating' might come from backend later

    // Update HTML elements with the fetched data
    // Ensure these IDs exist in your HTML (e.g., in your 'stats.html' page)
    const statsTotalProductsElement = document.getElementById("stats-totalProducts");
    if (statsTotalProductsElement) {
        statsTotalProductsElement.textContent = totalProducts;
    } else {
        console.warn("Element with ID 'stats-totalProducts' not found.");
    }

    const statsTotalOrdersElement = document.getElementById("stats-totalOrders");
    if (statsTotalOrdersElement) {
        statsTotalOrdersElement.textContent = totalOrders;
    } else {
        console.warn("Element with ID 'stats-totalOrders' not found.");
    }

    const statsTotalRevenueElement = document.getElementById("stats-totalRevenue");
    if (statsTotalRevenueElement) {
        statsTotalRevenueElement.textContent = `$${totalRevenue.toFixed(2)}`;
    } else {
        console.warn("Element with ID 'stats-totalRevenue' not found.");
    }

    const statsRatingElement = document.getElementById("stats-rating");
    if (statsRatingElement) {
        statsRatingElement.textContent = `${rating.toFixed(1)}/5`;
    } else {
        console.warn("Element with ID 'stats-rating' not found.");
    }

    // Adjust labels based on role (assuming 'role' 1 is 'vendor' and others are 'customer')
    // This part assumes 'role' is available in backendData or determined otherwise.
    // If 'role' is not in backendData, you'll need to fetch user role separately.
    const isVendor = backendData.role === 1; // Adjust this logic if 'role' is not directly in backendData
    const statsOrdersLabelElement = document.getElementById("stats-ordersLabel");
    if (statsOrdersLabelElement) {
        statsOrdersLabelElement.textContent = isVendor ? 'Orders Received' : 'Orders Placed';
    } else {
        console.warn("Element with ID 'stats-ordersLabel' not found.");
    }

    const statsRevenueLabelElement = document.getElementById("stats-revenueLabel");
    if (statsRevenueLabelElement) {
        statsRevenueLabelElement.textContent = isVendor ? 'Total Revenue' : 'Total Spent';
    } else {
        console.warn("Element with ID 'stats-revenueLabel' not found.");
    }

} catch (error) {
    console.error("Error fetching vendor statistics:", error);
    // You might want to display an error message on the page here,
    // e.g., by targeting a specific error div or the main container.
    const statsContainer = document.querySelector(".products-container"); // Assuming this is your main container
    if (statsContainer) {
        statsContainer.innerHTML = `<div class="error-message">Failed to load statistics: ${error.message || "Please try again later."}</div>`;
    }
}






  } catch (error) {
    console.error("Critical error loading user profile:", error);
    // Display robust error messages on the page if data cannot be loaded
    document.getElementById("profile-username").textContent = "Error loading profile data";
    document.getElementById("profile-role").textContent = ""; // Clear role
    document.getElementById("profile-email").textContent = "Failed to load profile. Please ensure you are logged in and refresh.";
    document.getElementById("profile-contactInfo").textContent = "";
    document.getElementById("profile-address").textContent = "";
//    document.getElementById("profile-memberSince").textContent = "";

    // Clear stats or show error
    document.getElementById("stats-totalProducts").textContent = "N/A";
    document.getElementById("stats-totalOrders").textContent = "N/A";
    document.getElementById("stats-totalRevenue").textContent = "N/A";
    document.getElementById("stats-rating").textContent = "N/A";

    // Optionally disable edit buttons if data couldn't load
    if (document.getElementById("editProfileBtn")) document.getElementById("editProfileBtn").disabled = true;
    if (document.getElementById("changePasswordBtn")) document.getElementById("changePasswordBtn").disabled = true;
//    if (document.getElementById("notificationSettingsBtn")) document.getElementById("notificationSettingsBtn").disabled = true;
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
      return "Customer";
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

function showChangePasswordModal() {
  const modal = createModal(
    "Change Password",
    `
        <form id="changePasswordForm">
            <div class="form-group">
                <label for="currentPassword">Current Password</label>
                <input type="password" id="currentPassword" name="currentPassword" required>
            </div>
            <div class="form-group">
                <label for="newPassword">New Password</label>
                <input type="password" id="newPassword" name="newPassword" required>
            </div>
            <div class="form-group">
                <label for="confirmPassword">Confirm New Password</label>
                <input type="password" id="confirmPassword" name="confirmPassword" required>
            </div>
            <button type="submit" class="btn save-btn">Change Password</button>
        </form>
    `
  );

  const form = modal.querySelector("form");
  if (form) {
    form.addEventListener("submit", async function (e) {
      e.preventDefault();
      await changePassword(new FormData(form));
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
//      fetchUserProfileAndDisplay(); // Re-fetch and display the latest data
      document.querySelector(".modal").remove(); // Close the modal
//        document.querySelector(".modal").remove(); // Close the modal first
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
 * Handles changing the user's password via a POST request.
 * Compares new password with confirmation and includes current user ID in the URL.
 * @param {FormData} formData - Form data containing current, new, and confirm passwords.
 */
async function changePassword(formData) {
  const newPassword = formData.get("newPassword");
  const confirmPassword = formData.get("confirmPassword");

  if (newPassword !== confirmPassword) {
    alert("New passwords do not match!");
    return;
  }

  if (!currentUserId) {
    alert("Error: User ID not found for password change. Please refresh the page.");
    console.error("Attempted to change password without a valid currentUserId.");
    return;
  }

  try {
    const token = localStorage.getItem('jwtToken');
    // Adjust this URL to your actual change password endpoint that accepts user ID
    const url = `/users/${currentUserId}/change-password`; // Example endpoint structure

    const response = await fetch(url, {
      method: "POST", // Often POST for password changes
      headers: {
        "Content-Type": "application/json",
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        currentPassword: formData.get("currentPassword"),
        newPassword: newPassword, // Use the stored newPassword
      }),
    });

    if (response.ok) {
      alert("Password changed successfully!");
      document.querySelector(".modal").remove(); // Close modal
    } else {
      const errorData = await response.json();
      throw new Error(errorData.message || `Failed to change password: Status ${response.status}`);
    }
  } catch (error) {
    console.error("Error changing password:", error);
    alert("Failed to change password: " + error.message);
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