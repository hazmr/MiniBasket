import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import type { ProductSummary } from "@/lib/types";
import { Price } from "./price";
import { ProductImage } from "./product-image";

export function ProductCard({ product }: { product: ProductSummary }) {
  const percentOff = product.discountPrice
    ? Math.round((1 - product.discountPrice / product.price) * 100)
    : 0;

  return (
    <Link
      href={`/products/${product.id}`}
      className="group flex flex-col gap-3 rounded-xl border bg-card p-3 transition hover:-translate-y-0.5 hover:shadow-md"
    >
      <div className="relative">
        <ProductImage
          src={product.imageUrl}
          alt={product.name}
          className={product.inStock ? "transition group-hover:opacity-90" : "opacity-50 grayscale"}
        />
        <div className="absolute top-2 left-2 flex gap-1">
          {percentOff > 0 && <Badge variant="destructive" className="bg-destructive text-white">-{percentOff}%</Badge>}
          {!product.inStock && <Badge variant="secondary">Sold out</Badge>}
        </div>
      </div>
      <div className="flex flex-1 flex-col gap-1 px-1">
        <p className="text-xs font-medium tracking-wide text-muted-foreground uppercase">
          {product.brandName ?? product.categoryName}
        </p>
        <h3 className="leading-snug font-medium group-hover:text-primary">{product.name}</h3>
        <Price price={product.price} discountPrice={product.discountPrice} className="mt-auto pt-2" />
      </div>
    </Link>
  );
}
