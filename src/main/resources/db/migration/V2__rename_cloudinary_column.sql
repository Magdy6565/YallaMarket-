-- Rename the column from cludinary_public_id to cloudinary_image_url
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS cloudinary_image_url VARCHAR(255);

-- Copy data from old column to new column
UPDATE products 
SET cloudinary_image_url = cludinary_public_id 
WHERE cludinary_public_id IS NOT NULL;

-- Drop the old column (if it exists)
ALTER TABLE products 
DROP COLUMN IF EXISTS cludinary_public_id;
