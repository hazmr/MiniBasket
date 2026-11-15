import { Heart, PackageCheck, PackageX, ShoppingCart } from "lucide-react";
import Link from "next/link";
import { Suspense } from "react";
import { addToCart } from "@/actions/cart";
import { addToWishlist } from "@/actions/wishlist";
import { ActionButton } from "@/components/action-button";
import { ActionForm } from "@/components/action-form";
import { Loading } from "@/components/loading";
import { Price } from "@/components/price";
import { ProductGallery } from "@/components/product-gallery";
import { SubmitButton } from "@/components/submit-button";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { Card } from "@/components/ui/card";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { Table, TableBody, TableCell, TableRow } from "@/components/ui/table";
import { getOrNotFound } from "@/lib/api";
import type { Product } from "@/lib/types";

export default function ProductPage({ params }: PageProps<"/products/[id]">) {
  return (
    <Suspense fallback={<Loading />}>
      <ProductDetails params={params} />
    </Suspense>
  );
}

async function ProductDetails({ params }: { params: PageProps<"/products/[id]">["params"] }) {
  const { id } = await params;
  const product = await getOrNotFound<Product>(`/products/${id}`);
  const attributes = Object.entries(product.attributes);
  const inStock = product.stockQuantity > 0;

  return (
    <div className="flex flex-col gap-6">
      <Breadcrumb>
        <BreadcrumbList>
          <BreadcrumbItem>
            <BreadcrumbLink asChild><Link href="/">Shop</Link></BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbLink asChild>
              <Link href={`/?categoryId=${product.category.id}`}>{product.category.name}</Link>
            </BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbPage>{product.name}</BreadcrumbPage>
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>

      <div className="grid gap-10 md:grid-cols-2">
        <ProductGallery images={product.images} alt={product.name} />

        <div className="flex flex-col gap-6">
          <div className="flex flex-col gap-2">
            <p className="text-sm font-medium tracking-wide text-muted-foreground uppercase">
              {product.brand?.name ?? product.category.name}
            </p>
            <h1 className="font-heading text-3xl font-semibold tracking-tight">{product.name}</h1>
            <p className="font-mono text-xs text-muted-foreground">SKU {product.sku}</p>
          </div>

          <Price price={product.price} discountPrice={product.discountPrice} className="text-3xl" />

          <p className={inStock ? "flex items-center gap-2 text-sm text-primary" : "flex items-center gap-2 text-sm text-destructive"}>
            {inStock ? <PackageCheck className="size-4" /> : <PackageX className="size-4" />}
            {inStock ? `${product.stockQuantity} in stock, ready to ship` : "Sold out"}
          </p>

          <Separator />

          <ActionForm action={addToCart} className="flex flex-col gap-3">
            <input type="hidden" name="productId" value={product.id} />
            <input type="hidden" name="returnTo" value={`/products/${product.id}`} />
            <div className="flex items-end gap-3">
              <Field className="w-24">
                <FieldLabel htmlFor="quantity">Quantity</FieldLabel>
                <Input
                  id="quantity"
                  name="quantity"
                  type="number"
                  defaultValue={1}
                  min={1}
                  max={Math.max(product.stockQuantity, 1)}
                />
              </Field>
              <SubmitButton size="lg" className="flex-1" disabled={!inStock}>
                <ShoppingCart data-icon="inline-start" />
                Add to cart
              </SubmitButton>
            </div>
          </ActionForm>

          <ActionButton action={addToWishlist.bind(null, product.id)} size="lg">
            <Heart data-icon="inline-start" />
            Save to wishlist
          </ActionButton>

          {attributes.length > 0 && (
            <Card className="py-0">
              <Table>
                <TableBody>
                  {attributes.map(([key, value]) => (
                    <TableRow key={key}>
                      <TableCell className="w-1/3 pl-4 font-medium text-muted-foreground capitalize">{key}</TableCell>
                      <TableCell className="pr-4">{String(value)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
