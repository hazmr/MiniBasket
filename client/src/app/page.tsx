import { SearchX, Tag } from "lucide-react";
import Link from "next/link";
import { Suspense, type ReactNode } from "react";
import { EmptyState } from "@/components/empty-state";
import { Loading } from "@/components/loading";
import { Pagination } from "@/components/pagination";
import { ProductCard } from "@/components/product-card";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { NativeSelect, NativeSelectOption } from "@/components/ui/native-select";
import { api } from "@/lib/api";
import { categoryLabel } from "@/lib/categories";
import { single } from "@/lib/format";
import type { Brand, Category, Page, ProductSummary } from "@/lib/types";

const SORT_OPTIONS = [
  { value: "id,asc", label: "Featured" },
  { value: "price,asc", label: "Price: low to high" },
  { value: "price,desc", label: "Price: high to low" },
  { value: "name,asc", label: "Name" },
  { value: "createdAt,desc", label: "Newest" },
];

export default function ShopPage({ searchParams }: PageProps<"/">) {
  return (
    <Suspense fallback={<Loading />}>
      <Shop searchParams={searchParams} />
    </Suspense>
  );
}

async function Shop({ searchParams }: { searchParams: PageProps<"/">["searchParams"] }) {
  const params = await searchParams;
  const filters = {
    name: single(params.name) ?? "",
    categoryId: single(params.categoryId) ?? "",
    brandId: single(params.brandId) ?? "",
    sort: single(params.sort) ?? "id,asc",
  };
  const page = Number(single(params.page) ?? 0);
  const isFiltered = Boolean(filters.name || filters.categoryId || filters.brandId);

  const query = new URLSearchParams({ ...filters, page: String(page), size: "12" });
  const [products, categories, brands] = await Promise.all([
    api.get<Page<ProductSummary>>(`/products?${query}`),
    api.get<Category[]>("/categories"),
    api.get<Brand[]>("/brands"),
  ]);

  const hrefFor = (target: number) => `/?${new URLSearchParams({ ...filters, page: String(target) })}`;
  const chipHref = (categoryId: string) => `/?${new URLSearchParams({ ...filters, categoryId })}`;

  return (
    <div className="flex flex-col gap-8">
      {!isFiltered && page === 0 && <Hero />}

      <div className="grid gap-8 md:grid-cols-[240px_1fr]">
        <aside>
          <Card className="md:sticky md:top-24">
            <CardContent>
              <form action="/" className="flex flex-col gap-5">
                <input type="hidden" name="name" value={filters.name} />
                <FieldGroup className="gap-4">
                  <Field>
                    <FieldLabel htmlFor="categoryId">Category</FieldLabel>
                    <NativeSelect id="categoryId" name="categoryId" defaultValue={filters.categoryId}>
                      <NativeSelectOption value="">All categories</NativeSelectOption>
                      {categories.map((category) => (
                        <NativeSelectOption key={category.id} value={category.id}>
                          {categoryLabel(category, categories)}
                        </NativeSelectOption>
                      ))}
                    </NativeSelect>
                  </Field>
                  <Field>
                    <FieldLabel htmlFor="brandId">Brand</FieldLabel>
                    <NativeSelect id="brandId" name="brandId" defaultValue={filters.brandId}>
                      <NativeSelectOption value="">All brands</NativeSelectOption>
                      {brands.map((brand) => (
                        <NativeSelectOption key={brand.id} value={brand.id}>{brand.name}</NativeSelectOption>
                      ))}
                    </NativeSelect>
                  </Field>
                  <Field>
                    <FieldLabel htmlFor="sort">Sort by</FieldLabel>
                    <NativeSelect id="sort" name="sort" defaultValue={filters.sort}>
                      {SORT_OPTIONS.map((option) => (
                        <NativeSelectOption key={option.value} value={option.value}>{option.label}</NativeSelectOption>
                      ))}
                    </NativeSelect>
                  </Field>
                </FieldGroup>
                <Button type="submit">Apply filters</Button>
                {isFiltered && (
                  <Button variant="ghost" asChild>
                    <Link href="/">Clear filters</Link>
                  </Button>
                )}
              </form>
            </CardContent>
          </Card>
        </aside>

        <section className="flex min-w-0 flex-col gap-6">
          <nav aria-label="Categories" className="flex flex-wrap gap-2">
            <CategoryChip href={chipHref("")} active={!filters.categoryId}>All</CategoryChip>
            {categories
              .toSorted((a, b) => categoryLabel(a, categories).localeCompare(categoryLabel(b, categories)))
              .map((category) => (
                <CategoryChip key={category.id} href={chipHref(String(category.id))} active={filters.categoryId === String(category.id)}>
                  {category.name}
                </CategoryChip>
              ))}
          </nav>
          <p className="text-sm text-muted-foreground">
            {filters.name ? <>Results for <span className="font-medium text-foreground">“{filters.name}”</span> · </> : null}
            {products.totalElements} {products.totalElements === 1 ? "product" : "products"}
          </p>
          {products.content.length === 0 ? (
            <EmptyState icon={SearchX} title="No products found" description="Try another search or clear the filters.">
              <Button variant="outline" asChild>
                <Link href="/">Show all products</Link>
              </Button>
            </EmptyState>
          ) : (
            <div className="grid grid-cols-2 gap-4 lg:grid-cols-3">
              {products.content.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          )}
          <Pagination page={products.page} totalPages={products.totalPages} hrefFor={hrefFor} />
        </section>
      </div>
    </div>
  );
}

function CategoryChip({ href, active, children }: { href: string; active: boolean; children: ReactNode }) {
  return (
    <Button asChild size="sm" variant={active ? "default" : "outline"} className="rounded-full">
      <Link href={href} aria-current={active ? "page" : undefined}>{children}</Link>
    </Button>
  );
}

function Hero() {
  return (
    <section className="relative overflow-hidden rounded-2xl bg-primary px-6 py-10 text-primary-foreground sm:px-10">
      <div className="relative z-10 flex max-w-xl flex-col gap-3">
        <h1 className="font-heading text-3xl font-semibold tracking-tight sm:text-4xl">Good things, small basket.</h1>
        <p className="text-primary-foreground/80">
          Phones, laptops and kitchen favourites, delivered next day with Express shipping.
        </p>
        <p className="mt-2 inline-flex w-fit items-center gap-2 rounded-full bg-white/15 px-3 py-1 text-sm">
          <Tag className="size-4" />
          10% off your first order with <span className="font-mono font-semibold">WELCOME10</span>
        </p>
      </div>
      <div aria-hidden className="absolute -top-16 -right-16 size-64 rounded-full bg-white/10" />
      <div aria-hidden className="absolute -right-4 -bottom-24 size-48 rounded-full bg-white/10" />
    </section>
  );
}
