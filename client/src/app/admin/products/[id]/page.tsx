import { ExternalLink, Trash2 } from "lucide-react";
import Link from "next/link";
import { Suspense } from "react";
import { deleteProduct, removeProductImage, updateProduct } from "@/actions/admin";
import { ActionButton } from "@/components/action-button";
import { ConfirmActionButton } from "@/components/confirm-action-button";
import { ImageUploader } from "@/components/image-uploader";
import { Loading } from "@/components/loading";
import { PageHeader } from "@/components/page-header";
import { ProductForm } from "@/components/product-form";
import { ProductImage } from "@/components/product-image";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { api, getOrNotFound } from "@/lib/api";
import { requireAdmin } from "@/lib/session";
import type { Brand, Category, Product } from "@/lib/types";

export default function EditProductPage({ params }: PageProps<"/admin/products/[id]">) {
  return (
    <Suspense fallback={<Loading />}>
      <EditProduct params={params} />
    </Suspense>
  );
}

async function EditProduct({ params }: { params: PageProps<"/admin/products/[id]">["params"] }) {
  await requireAdmin();
  const { id } = await params;
  const [product, categories, brands] = await Promise.all([
    getOrNotFound<Product>(`/products/${id}`),
    api.get<Category[]>("/categories"),
    api.get<Brand[]>("/brands"),
  ]);

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title={product.name}
        actions={
          <>
            <Button variant="outline" asChild>
              <Link href={`/products/${product.id}`}><ExternalLink data-icon="inline-start" />View in shop</Link>
            </Button>
            <ConfirmActionButton
              action={deleteProduct.bind(null, product.id)}
              variant="destructive"
              title={`Delete ${product.name}?`}
              description="It disappears from the shop and from every cart and wishlist. Products that were already ordered can't be deleted."
              confirmLabel="Delete product"
            >
              <Trash2 data-icon="inline-start" />
              Delete
            </ConfirmActionButton>
          </>
        }
      />

      <ProductForm
        action={updateProduct.bind(null, product.id)}
        product={product}
        categories={categories}
        brands={brands}
        submitLabel="Save changes"
      />

      <Card>
        <CardHeader>
          <CardTitle>Images</CardTitle>
          <CardDescription>The first image is the one shown in the shop.</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-6">
          {product.images.length > 0 && (
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
              {product.images.map((image) => (
                <div key={image.id} className="relative">
                  <ProductImage src={image.imageUrl} alt={product.name} className="border" />
                  <div className="absolute top-2 right-2">
                    <ActionButton
                      action={removeProductImage.bind(null, product.id, image.id)}
                      variant="secondary"
                      size="icon-sm"
                      aria-label="Remove image"
                    >
                      <Trash2 />
                    </ActionButton>
                  </div>
                </div>
              ))}
            </div>
          )}
          <ImageUploader productId={product.id} />
        </CardContent>
      </Card>
    </div>
  );
}
