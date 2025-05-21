document.addEventListener("DOMContentLoaded", function () {
  const registerForm = document.getElementById("registerForm");

  if (registerForm) {
    registerForm.addEventListener("submit", function (event) {
      let isValid = true;
      clearAllErrors();

      // --- Username Validation ---
      const usernameInput = document.getElementById("username");
      const usernameError = document.getElementById("usernameError");
      const usernameValue = usernameInput.value.trim();
      if (!usernameValue) {
        isValid = false;
        displayFieldError(
          usernameInput,
          usernameError,
          "Username is required."
        );
      } else if (usernameValue.length < 3) {
        isValid = false;
        displayFieldError(
          usernameInput,
          usernameError,
          "Username must be at least 3 characters long."
        );
      } else if (usernameValue.length > 50) {
        isValid = false;
        displayFieldError(
          usernameInput,
          usernameError,
          "Username cannot exceed 50 characters."
        );
      }

      // --- Email Validation ---
      // IMPORTANT: Checking if an email already exists MUST be done on the server-side
      // before creating the user. Client-side checks for this are for UX improvement
      // (e.g., via an AJAX call to a dedicated endpoint) but are not a security measure.
      const emailInput = document.getElementById("email");
      const emailError = document.getElementById("emailError");
      const emailValue = emailInput.value.trim();
      if (!emailValue) {
        isValid = false;
        displayFieldError(emailInput, emailError, "Email is required.");
      } else if (!isValidEmailFormat(emailValue)) {
        isValid = false;
        displayFieldError(
          emailInput,
          emailError,
          "Please enter a valid email address."
        );
      }
      // To check if email exists client-side (optional UX enhancement):
      // You would typically make an asynchronous call (fetch/AJAX) here
      // to an endpoint like /api/check-email?email=...
      // and then set isValid = false and display an error if it exists.
      // This is more advanced and requires a backend endpoint.

      // --- Password Validation ---
      const passwordInput = document.getElementById("password");
      const passwordError = document.getElementById("passwordError");
      const passwordValue = passwordInput.value;
      if (!passwordValue) {
        isValid = false;
        displayFieldError(
          passwordInput,
          passwordError,
          "Password is required."
        );
      } else if (passwordValue.length < 6) {
        isValid = false;
        displayFieldError(
          passwordInput,
          passwordError,
          "Password must be at least 6 characters long."
        );
      }

      // --- Role Validation ---
      const roleInput = document.getElementById("role"); // This is now a select element
      const roleError = document.getElementById("roleError");
      const roleValue = roleInput.value; // Value of selected option ( "1" or "2" or "" )
      if (!roleValue) {
        // Checks if the default "-- Select Role --" (value="") is selected
        isValid = false;
        displayFieldError(roleInput, roleError, "Please select a role.");
      }
      // The select element naturally restricts choices to "1" or "2" if populated correctly.
      // The `required` attribute on select and the !roleValue check above are usually sufficient.

      // --- Address Validation ---
      const addressInput = document.getElementById("address");
      const addressError = document.getElementById("addressError");
      const addressValue = addressInput.value.trim();
      if (!addressValue) {
        isValid = false;
        displayFieldError(addressInput, addressError, "Address is required.");
      } else if (addressValue.length < 5) {
        isValid = false;
        displayFieldError(
          addressInput,
          addressError,
          "Address seems too short."
        );
      }

      // --- Contact Info Validation ---
      const contactInfoInput = document.getElementById("contactInfo");
      const contactInfoError = document.getElementById("contactInfoError");
      const contactInfoValue = contactInfoInput.value.trim();
      if (!contactInfoValue) {
        isValid = false;
        displayFieldError(
          contactInfoInput,
          contactInfoError,
          "Contact Info is required."
        );
      } else if (!isValidPhoneNumber(contactInfoValue)) {
        isValid = false;
        displayFieldError(
          contactInfoInput,
          contactInfoError,
          "Contact number must be 11 digits starting with 0 (e.g., 01xxxxxxxxx)."
        );
      }

      // --- Final Check ---
      if (!isValid) {
        event.preventDefault();
        const firstErrorInput = registerForm.querySelector(".error-input");
        if (firstErrorInput) {
          firstErrorInput.focus();
        }
        return;
      }
      // AJAX submit to /auth/signup
      event.preventDefault();
      const formData = {
        username: document.getElementById("username").value.trim(),
        email: document.getElementById("email").value.trim(),
        password: document.getElementById("password").value,
        role: document.getElementById("role").value,
        address: document.getElementById("address").value.trim(),
        contactInfo: document.getElementById("contactInfo").value.trim(),
      };
      fetch("/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      })
        .then((res) => {
          // Try to parse JSON, fallback to text if not JSON
          return res.text().then((text) => {
            let data;
            try {
              data = JSON.parse(text);
            } catch (e) {
              data = text;
            }
            return { status: res.status, body: data };
          });
        })
        .then(({ status, body }) => {
          if (status === 200) {
            window.location.href =
              "/verify?email=" + encodeURIComponent(formData.email);
          } else {
            document.getElementById("emailError").textContent =
              body && body.message ? body.message : body;
          }
        })
        .catch(() => {
          document.getElementById("emailError").textContent = "Network error.";
        });
    });
  }

  function displayFieldError(inputElement, errorElement, message) {
    if (inputElement) {
      inputElement.classList.add("error-input");
    }
    if (errorElement) {
      errorElement.textContent = message;
    }
  }

  function clearAllErrors() {
    const errorMessages = document.querySelectorAll(".error-message");
    errorMessages.forEach((msg) => (msg.textContent = ""));
    const errorInputs = document.querySelectorAll(
      ".error-input, .error-select"
    ); // Ensure select errors are also cleared
    errorInputs.forEach((input) => input.classList.remove("error-input"));
  }

  function isValidEmailFormat(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  function isValidPhoneNumber(phone) {
    // Validates Egyptian phone numbers: 11 digits, starting with 0.
    // Example: 01012345678
    const egyptianPhoneRegex = /^0\d{10}$/;
    return egyptianPhoneRegex.test(phone);
  }
});
