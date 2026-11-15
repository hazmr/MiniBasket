import { Card, CardContent } from "@/components/ui/card";
import { Field, FieldDescription, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { NativeSelect, NativeSelectOption } from "@/components/ui/native-select";
import { Textarea } from "@/components/ui/textarea";
import type { FormAction } from "@/lib/action-state";
import { categoryLabel } from "@/lib/categories";
import type { Brand, Category, Product } from "@/lib/types";
import { ActionForm } from "./action-form";
import { SubmitButton } from "./submit-button";

type Props = {
  action: FormAction;
  categories: Category[];
  brands: Brand[];
  product?: Product;
  submitLabel: string;
};

export function ProductForm({ action, categories, brands, product, submitLabel }: Props) {
  return (
    <Card>
      <CardContent>
        <ActionForm action={action} className="flex flex-col gap-6">
          {product && <input type="hidden" name="version" value={product.version} />}
          <FieldGroup className="grid gap-5 md:grid-cols-2">
            <Field>
              <FieldLabel htmlFor="name">Name</FieldLabel>
              <Input id="name" name="name" required defaultValue={product?.name} />
            </Field>
            <Field>
              <FieldLabel htmlFor="sku">SKU</FieldLabel>
              <Input id="sku" name="sku" required defaultValue={product?.sku} className="font-mono" />
            </Field>
            <Field>
              <FieldLabel htmlFor="price">Price</FieldLabel>
              <Input id="price" name="price" type="number" step="0.01" min="0" required defaultValue={product?.price} />
            </Field>
            <Field>
              <FieldLabel htmlFor="discountPrice">Sale price</FieldLabel>
              <Input id="discountPrice" name="discountPrice" type="number" step="0.01" min="0" defaultValue={product?.discountPrice ?? ""} />
              <FieldDescription>Leave empty when the product is not on sale.</FieldDescription>
            </Field>
            <Field>
              <FieldLabel htmlFor="stockQuantity">Stock</FieldLabel>
              <Input id="stockQuantity" name="stockQuantity" type="number" min="0" required defaultValue={product?.stockQuantity ?? 0} />
            </Field>
            <Field>
              <FieldLabel htmlFor="categoryId">Category</FieldLabel>
              <NativeSelect id="categoryId" name="categoryId" required defaultValue={product?.category.id}>
                {categories.map((category) => (
                  <NativeSelectOption key={category.id} value={category.id}>{categoryLabel(category, categories)}</NativeSelectOption>
                ))}
              </NativeSelect>
            </Field>
            <Field>
              <FieldLabel htmlFor="brandId">Brand</FieldLabel>
              <NativeSelect id="brandId" name="brandId" defaultValue={product?.brand?.id ?? ""}>
                <NativeSelectOption value="">No brand</NativeSelectOption>
                {brands.map((brand) => (
                  <NativeSelectOption key={brand.id} value={brand.id}>{brand.name}</NativeSelectOption>
                ))}
              </NativeSelect>
            </Field>
            <Field className="md:col-span-2">
              <FieldLabel htmlFor="attributes">Attributes</FieldLabel>
              <Textarea
                id="attributes"
                name="attributes"
                rows={3}
                defaultValue={product ? JSON.stringify(product.attributes) : ""}
                placeholder='{"color": "black", "storage": "128GB"}'
                className="font-mono"
              />
              <FieldDescription>Extra details as JSON. They show as a table on the product page.</FieldDescription>
            </Field>
          </FieldGroup>
          <SubmitButton className="w-fit">{submitLabel}</SubmitButton>
        </ActionForm>
      </CardContent>
    </Card>
  );
}
