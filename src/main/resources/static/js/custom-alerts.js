/**
 * Beautiful Custom Alert System
 * Replaces native browser alerts with modern, customizable modals
 */

class CustomAlert {
  constructor() {
    this.alertQueue = [];
    this.isShowing = false;
    this.toastContainer = null;
    this.initializeToastContainer();
  }

  /**
   * Initialize the toast container for non-blocking notifications
   */
  initializeToastContainer() {
    if (!this.toastContainer) {
      this.toastContainer = document.createElement("div");
      this.toastContainer.className = "toast-container";
      document.body.appendChild(this.toastContainer);
    }
  }

  /**
   * Show a modal alert
   * @param {string} message - The alert message
   * @param {string} type - Alert type: 'success', 'error', 'warning', 'info'
   * @param {string} title - Optional title
   * @param {Array} buttons - Optional custom buttons
   * @returns {Promise} - Resolves with the button clicked
   */
  show(message, type = "info", title = "", buttons = null) {
    return new Promise((resolve) => {
      const alertConfig = {
        message,
        type,
        title: title || this.getDefaultTitle(type),
        buttons: buttons || [{ text: "OK", class: "primary", value: "ok" }],
        resolve,
      };

      if (this.isShowing) {
        this.alertQueue.push(alertConfig);
      } else {
        this.showAlert(alertConfig);
      }
    });
  }

  /**
   * Show a success alert
   */
  success(message, title = "Success!") {
    return this.show(message, "success", title);
  }

  /**
   * Show an error alert
   */
  error(message, title = "Error!") {
    return this.show(message, "error", title);
  }

  /**
   * Show a warning alert
   */
  warning(message, title = "Warning!") {
    return this.show(message, "warning", title);
  }

  /**
   * Show an info alert
   */
  info(message, title = "Information") {
    return this.show(message, "info", title);
  }

  /**
   * Show a confirmation dialog
   */
  confirm(message, title = "Confirm Action") {
    const buttons = [
      { text: "Cancel", class: "secondary", value: false },
      { text: "Confirm", class: "primary", value: true },
    ];
    return this.show(message, "warning", title, buttons);
  }

  /**
   * Show a toast notification (non-blocking)
   */
  toast(message, type = "info", duration = 4000) {
    const toast = this.createToastElement(message, type);
    this.toastContainer.appendChild(toast);

    // Trigger animation
    setTimeout(() => toast.classList.add("show"), 100);

    // Auto remove
    setTimeout(() => this.removeToast(toast), duration);

    return toast;
  }

  /**
   * Create toast element
   */
  createToastElement(message, type) {
    const toast = document.createElement("div");
    toast.className = `toast-alert ${type}`;

    const icon = this.getIcon(type);

    toast.innerHTML = `
            <div class="toast-icon">${icon}</div>
            <div class="toast-message">${message}</div>
            <button class="toast-close" onclick="customAlert.removeToast(this.parentElement)">&times;</button>
        `;

    return toast;
  }

  /**
   * Remove toast notification
   */
  removeToast(toast) {
    toast.classList.remove("show");
    setTimeout(() => {
      if (toast.parentElement) {
        toast.parentElement.removeChild(toast);
      }
    }, 400);
  }

  /**
   * Show the modal alert
   */
  showAlert(config) {
    this.isShowing = true;

    // Create overlay
    const overlay = document.createElement("div");
    overlay.className = "alert-overlay";

    // Create alert container
    const alertContainer = document.createElement("div");
    alertContainer.className = `custom-alert ${config.type}`;

    const icon = this.getIcon(config.type);

    // Build alert HTML
    alertContainer.innerHTML = `
            <button class="alert-close" onclick="customAlert.closeCurrentAlert()">&times;</button>
            <div class="alert-icon">${icon}</div>
            <div class="alert-title">${config.title}</div>
            <div class="alert-message">${config.message}</div>
            <div class="alert-buttons">
                ${config.buttons
                  .map(
                    (button, index) =>
                      `<button class="alert-btn ${button.class}" onclick="customAlert.handleButtonClick(${index})">${button.text}</button>`
                  )
                  .join("")}
            </div>
        `;

    overlay.appendChild(alertContainer);
    document.body.appendChild(overlay);

    // Store current config
    this.currentConfig = config;
    this.currentOverlay = overlay;

    // Show with animation
    setTimeout(() => overlay.classList.add("show"), 100);

    // Handle ESC key
    this.escapeHandler = (e) => {
      if (e.key === "Escape") {
        this.closeCurrentAlert();
      }
    };
    document.addEventListener("keydown", this.escapeHandler);
  }

  /**
   * Handle button click
   */
  handleButtonClick(buttonIndex) {
    const button = this.currentConfig.buttons[buttonIndex];
    this.closeCurrentAlert(button.value);
  }

  /**
   * Close current alert
   */
  closeCurrentAlert(result = null) {
    if (!this.currentOverlay) return;

    this.currentOverlay.classList.remove("show");

    setTimeout(() => {
      if (this.currentOverlay && this.currentOverlay.parentElement) {
        this.currentOverlay.parentElement.removeChild(this.currentOverlay);
      }

      this.isShowing = false;

      // Resolve promise
      if (this.currentConfig && this.currentConfig.resolve) {
        this.currentConfig.resolve(result);
      }

      // Clean up
      this.currentOverlay = null;
      this.currentConfig = null;

      if (this.escapeHandler) {
        document.removeEventListener("keydown", this.escapeHandler);
        this.escapeHandler = null;
      }

      // Show next alert in queue
      if (this.alertQueue.length > 0) {
        const nextAlert = this.alertQueue.shift();
        setTimeout(() => this.showAlert(nextAlert), 200);
      }
    }, 300);
  }

  /**
   * Get icon for alert type
   */
  getIcon(type) {
    const icons = {
      success: "✓",
      error: "✕",
      warning: "⚠",
      info: "ℹ",
    };
    return icons[type] || icons.info;
  }

  /**
   * Get default title for alert type
   */
  getDefaultTitle(type) {
    const titles = {
      success: "Success!",
      error: "Error!",
      warning: "Warning!",
      info: "Information",
    };
    return titles[type] || titles.info;
  }

  /**
   * Replace native alert function
   */
  replaceNativeAlert() {
    window.originalAlert = window.alert;
    window.alert = (message) => {
      return this.info(message);
    };
  }

  /**
   * Restore native alert function
   */
  restoreNativeAlert() {
    if (window.originalAlert) {
      window.alert = window.originalAlert;
      delete window.originalAlert;
    }
  }
}

// Create global instance
const customAlert = new CustomAlert();

// Replace native alert by default
customAlert.replaceNativeAlert();

// Export for module systems
if (typeof module !== "undefined" && module.exports) {
  module.exports = CustomAlert;
}

// Make available globally
window.CustomAlert = CustomAlert;
window.customAlert = customAlert;
