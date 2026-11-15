import { Trash2 } from "lucide-react";
import { Suspense } from "react";
import { createCategory, deleteCategory } from "@/actions/admin";
import { ActionForm } from "@/components/action-form";
import { ConfirmActionButton } from "@/components/confirm-action-button";
import { Loading } from "@/components/loading";
import { PageHeader } from "@/components/page-header";
import { SubmitButton } from "@/components/submit-button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { NativeSelect, NativeSelectOption } from "@/components/ui/native-select";
import { api } from "@/lib/api";
import { categoryLabel } from "@/lib/categories";
import { requireAdmin } from "@/lib/session";
import type { Category } from "@/lib/types";

export default function AdminCategoriesPage() {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="Categories" description="Categories can be nested. A category still used by a product can't be deleted." />
      <Suspense fallback={<Loading />}>
        <Categories />
      </Suspense>
    </div>
  );
}

async function Categories() {
  await requireAdmin();
  const all = await api.get<Category[]>("/categories");
  const categories = all.toSorted((a, b) => categoryLabel(a, all).localeCompare(categoryLabel(b, all)));

  return (
    <div className="grid gap-6 xl:grid-cols-3">
      <Card className="py-0 xl:col-span-2">
        <ul className="divide-y">
          {categories.map((category) => (
            <li key={category.id} className="flex items-center justify-between gap-3 py-2 pr-2 pl-4 text-sm">
              {categoryLabel(category, categories)}
              <ConfirmActionButton
                action={deleteCategory.bind(null, category.id)}
                variant="ghost"
                size="icon-sm"
                aria-label={`Delete ${category.name}`}
                title={`Delete ${categoryLabel(category, categories)}?`}
                description="Only empty categories can be deleted: move its products and subcategories first."
                confirmLabel="Delete"
              >
                <Trash2 />
              </ConfirmActionButton>
            </li>
          ))}
        </ul>
      </Card>

      <Card className="h-fit">
        <CardHeader>
          <CardTitle>New category</CardTitle>
        </CardHeader>
        <CardContent>
          <ActionForm action={createCategory} className="flex flex-col gap-5">
            <FieldGroup className="gap-4">
              <Field>
                <FieldLabel htmlFor="name">Name</FieldLabel>
                <Input id="name" name="name" required />
              </Field>
              <Field>
                <FieldLabel htmlFor="parentId">Inside</FieldLabel>
                <NativeSelect id="parentId" name="parentId">
                  <NativeSelectOption value="">Top level</NativeSelectOption>
                  {categories.map((category) => (
                    <NativeSelectOption key={category.id} value={category.id}>{categoryLabel(category, categories)}</NativeSelectOption>
                  ))}
                </NativeSelect>
              </Field>
            </FieldGroup>
            <SubmitButton className="w-fit">Add category</SubmitButton>
          </ActionForm>
        </CardContent>
      </Card>
    </div>
  );
}
