document.addEventListener("DOMContentLoaded", function () {
  // Get product_id from URL path (e.g., /edit-product/123)
  const pathParts = window.location.pathname.split("/");
  const productId = pathParts[pathParts.length - 1];

  if (!productId || isNaN(productId)) {
    document.getElementById("errorMsg").textContent =
      "No valid product ID specified in URL.";
    return;
  }

  // Fetch product details
  fetch(`/api/my-products/${productId}`)
    .then((res) => {
      if (!res.ok) {
        return res.text().then((text) => {
          throw new Error(text || `HTTP error! status: ${res.status}`);
        });
      }
      return res.json();
    })    .then((product) => {
      document.getElementById("name").value = product.name || "";
      document.getElementById("description").value = product.description || "";
      document.getElementById("price").value = product.price || 0;
      document.getElementById("quantity").value = product.quantity || 0;
      document.getElementById("category").value = product.category || "";
      document.getElementById("imageUrl").value = product.imageUrl || "";
    })
    .catch((error) => {
      console.error("Error fetching product details:", error);
      document.getElementById(
        "errorMsg"
      ).textContent = `Failed to load product details: ${
        error.message || error
      }`;
    });
  // Handle form submission
  document
    .getElementById("editProductForm")
    .addEventListener("submit", function (e) {
      e.preventDefault();
      document.getElementById("successMsg").textContent = "";
      document.getElementById("errorMsg").textContent = "";

      const updatedProduct = {
        name: document.getElementById("name").value.trim(),
        description: document.getElementById("description").value.trim(),
        price: parseFloat(document.getElementById("price").value),
        quantity: parseInt(document.getElementById("quantity").value, 10),
        category: document.getElementById("category").value.trim(),
      };

      const imageUrl = document.getElementById("imageUrl").value.trim();
      let fetchPromise;
      
      if (imageUrl) {
        // Use the new endpoint for image URL updates
        const params = new URLSearchParams();
        params.append("name", updatedProduct.name);
        params.append("description", updatedProduct.description);
        params.append("price", updatedProduct.price);
        params.append("quantity", updatedProduct.quantity);
        params.append("category", updatedProduct.category);
        params.append("imageUrl", imageUrl);

        fetchPromise = fetch(`/api/my-products/${productId}/url`, {
          method: "PUT",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: params,
        });
      } else {
        // Use the original endpoint for regular updates
        fetchPromise = fetch(`/api/my-products/${productId}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(updatedProduct),
        });
      }

      fetchPromise
        .then((res) => {
          if (res.ok) {
            document.getElementById("successMsg").textContent =
              "Product updated successfully!";
            // Redirect to the products page after a short delay
            setTimeout(() => {
              window.location.href = "/products";
            }, 1500); // Wait 1.5 seconds to show success message
          } else {
            return res.text().then((text) => {
              let errorMessage = "Failed to update product.";
              try {
                const errorData = JSON.parse(text);
                errorMessage = errorData.message || errorData.error || text;
              } catch (e) {
                errorMessage = text;
              }
              throw new Error(errorMessage);
            });
          }
        })
        .catch((err) => {
          console.error("Error updating product:", err);
          document.getElementById("errorMsg").textContent =
            err.message || "Failed to update product.";
        });
    });
});
