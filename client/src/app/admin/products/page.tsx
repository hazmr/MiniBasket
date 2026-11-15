import { Pencil, Plus, Trash2 } from "lucide-react";
import Link from "next/link";
import { Suspense } from "react";
import { deleteProduct } from "@/actions/admin";
import { ConfirmActionButton } from "@/components/confirm-action-button";
import { Loading } from "@/components/loading";
import { PageHeader } from "@/components/page-header";
import { Pagination } from "@/components/pagination";
import { Price } from "@/components/price";
import { ProductImage } from "@/components/product-image";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { api } from "@/lib/api";
import { single } from "@/lib/format";
import { requireAdmin } from "@/lib/session";
import type { Page, ProductSummary } from "@/lib/types";

export default function AdminProductsPage({ searchParams }: PageProps<"/admin/products">) {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title="Products"
        description="Everything in the shop."
        actions={
          <Button asChild>
            <Link href="/admin/products/new"><Plus data-icon="inline-start" />New product</Link>
          </Button>
        }
      />
      <Suspense fallback={<Loading />}>
        <ProductTable searchParams={searchParams} />
      </Suspense>
    </div>
  );
}

async function ProductTable({ searchParams }: { searchParams: PageProps<"/admin/products">["searchParams"] }) {
  await requireAdmin();
  const page = Number(single((await searchParams).page) ?? 0);
  const products = await api.get<Page<ProductSummary>>(`/products?page=${page}&size=20&sort=id,asc`);

  return (
    <>
      <div className="overflow-hidden rounded-xl border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="pl-4">Product</TableHead>
              <TableHead>SKU</TableHead>
              <TableHead>Price</TableHead>
              <TableHead>Stock</TableHead>
              <TableHead className="pr-4 text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {products.content.map((product) => (
              <TableRow key={product.id}>
                <TableCell className="pl-4">
                  <div className="flex items-center gap-3">
                    <ProductImage src={product.imageUrl} alt={product.name} className="size-10 shrink-0" />
                    <div>
                      <p className="font-medium">{product.name}</p>
                      <p className="text-xs text-muted-foreground">{product.brandName ?? product.categoryName}</p>
                    </div>
                  </div>
                </TableCell>
                <TableCell className="font-mono text-xs">{product.sku}</TableCell>
                <TableCell><Price price={product.price} discountPrice={product.discountPrice} /></TableCell>
                <TableCell>
                  {product.inStock ? <Badge variant="secondary">In stock</Badge> : <Badge variant="destructive">Sold out</Badge>}
                </TableCell>
                <TableCell className="pr-4">
                  <div className="flex justify-end gap-1">
                    <Button variant="ghost" size="icon" asChild>
                      <Link href={`/admin/products/${product.id}`} aria-label={`Edit ${product.name}`}><Pencil /></Link>
                    </Button>
                    <ConfirmActionButton
                      action={deleteProduct.bind(null, product.id)}
                      variant="ghost"
                      size="icon"
                      aria-label={`Delete ${product.name}`}
                      title={`Delete ${product.name}?`}
                      description="It disappears from the shop and from every cart and wishlist. Products that were already ordered can't be deleted."
                      confirmLabel="Delete product"
                    >
                      <Trash2 />
                    </ConfirmActionButton>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <Pagination page={products.page} totalPages={products.totalPages} hrefFor={(target) => `/admin/products?page=${target}`} />
    </>
  );
}
