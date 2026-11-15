import { Suspense } from "react";
import { createProduct } from "@/actions/admin";
import { Loading } from "@/components/loading";
import { PageHeader } from "@/components/page-header";
import { ProductForm } from "@/components/product-form";
import { api } from "@/lib/api";
import { requireAdmin } from "@/lib/session";
import type { Brand, Category } from "@/lib/types";

export default function NewProductPage() {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="New product" description="You can add images after saving." />
      <Suspense fallback={<Loading />}>
        <NewProduct />
      </Suspense>
    </div>
  );
}

async function NewProduct() {
  await requireAdmin();
  const [categories, brands] = await Promise.all([api.get<Category[]>("/categories"), api.get<Brand[]>("/brands")]);

  return <ProductForm action={createProduct} categories={categories} brands={brands} submitLabel="Create product" />;
}
