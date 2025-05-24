document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("addProductForm");
  form.addEventListener("submit", function (e) {
    e.preventDefault();
    document.getElementById("successMsg").textContent = "";
    document.getElementById("errorMsg").textContent = "";
    
    // Build form data as URL parameters
    const formData = new FormData(form);
    const params = new URLSearchParams();
    
    for (let [key, value] of formData.entries()) {
      if (value) { // Only add non-empty values
        params.append(key, value);
      }
    }
    
    fetch("/api/my-products", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: params,
    })
      .then((res) => {
        if (res.ok) {
          document.getElementById("successMsg").textContent =
            "Product added successfully! Redirecting...";
          setTimeout(() => {
            window.location.href = "/products";
          }, 1200);
        } else {
          return res.text().then((text) => {
            throw new Error(text);
          });
        }
      })
      .catch((err) => {
        document.getElementById("errorMsg").textContent =
          err.message || "Failed to add product.";
      });
  });
});
