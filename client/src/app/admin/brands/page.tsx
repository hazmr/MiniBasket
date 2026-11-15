import { Trash2 } from "lucide-react";
import { Suspense } from "react";
import { createBrand, deleteBrand } from "@/actions/admin";
import { ActionForm } from "@/components/action-form";
import { ConfirmActionButton } from "@/components/confirm-action-button";
import { Loading } from "@/components/loading";
import { PageHeader } from "@/components/page-header";
import { SubmitButton } from "@/components/submit-button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { api } from "@/lib/api";
import { requireAdmin } from "@/lib/session";
import type { Brand } from "@/lib/types";

export default function AdminBrandsPage() {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="Brands" description="A brand still used by a product can't be deleted." />
      <Suspense fallback={<Loading />}>
        <Brands />
      </Suspense>
    </div>
  );
}

async function Brands() {
  await requireAdmin();
  const brands = await api.get<Brand[]>("/brands");

  return (
    <div className="grid gap-6 xl:grid-cols-3">
      <Card className="py-0 xl:col-span-2">
        <ul className="divide-y">
          {brands.map((brand) => (
            <li key={brand.id} className="flex items-center justify-between gap-3 py-2 pr-2 pl-4 text-sm">
              {brand.name}
              <ConfirmActionButton
                action={deleteBrand.bind(null, brand.id)}
                variant="ghost"
                size="icon-sm"
                aria-label={`Delete ${brand.name}`}
                title={`Delete ${brand.name}?`}
                description="A brand still used by a product can't be deleted."
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
          <CardTitle>New brand</CardTitle>
        </CardHeader>
        <CardContent>
          <ActionForm action={createBrand} className="flex flex-col gap-5">
            <Field>
              <FieldLabel htmlFor="name">Name</FieldLabel>
              <Input id="name" name="name" required />
            </Field>
            <SubmitButton className="w-fit">Add brand</SubmitButton>
          </ActionForm>
        </CardContent>
      </Card>
    </div>
  );
}
