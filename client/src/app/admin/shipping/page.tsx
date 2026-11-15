import { Trash2, Truck } from "lucide-react";
import { Suspense } from "react";
import { createShippingMethod, deleteShippingMethod } from "@/actions/admin";
import { ActionForm } from "@/components/action-form";
import { ConfirmActionButton } from "@/components/confirm-action-button";
import { Loading } from "@/components/loading";
import { PageHeader } from "@/components/page-header";
import { SubmitButton } from "@/components/submit-button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { api } from "@/lib/api";
import { money } from "@/lib/format";
import { requireAdmin } from "@/lib/session";
import type { ShippingMethod } from "@/lib/types";

export default function AdminShippingPage() {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="Shipping methods" description="The delivery options customers choose at checkout." />
      <Suspense fallback={<Loading />}>
        <ShippingMethods />
      </Suspense>
    </div>
  );
}

async function ShippingMethods() {
  await requireAdmin();
  const methods = await api.get<ShippingMethod[]>("/shipping-methods");

  return (
    <div className="grid gap-6 xl:grid-cols-3">
      <Card className="py-0 xl:col-span-2">
        <ul className="divide-y">
          {methods.map((method) => (
            <li key={method.id} className="flex items-center gap-3 px-4 py-3 text-sm">
              <Truck className="size-4 text-muted-foreground" />
              <span className="flex-1 font-medium">{method.name}</span>
              <span>{money(method.fee)}</span>
              <ConfirmActionButton
                action={deleteShippingMethod.bind(null, method.id)}
                variant="ghost"
                size="icon-sm"
                aria-label={`Delete ${method.name}`}
                title={`Delete ${method.name}?`}
                description="Customers won't be able to choose it anymore. Methods used by past orders can't be deleted."
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
          <CardTitle>New shipping method</CardTitle>
        </CardHeader>
        <CardContent>
          <ActionForm action={createShippingMethod} className="flex flex-col gap-5">
            <FieldGroup className="gap-4">
              <Field>
                <FieldLabel htmlFor="name">Name</FieldLabel>
                <Input id="name" name="name" required placeholder="Standard (3-5 days)" />
              </Field>
              <Field>
                <FieldLabel htmlFor="fee">Fee</FieldLabel>
                <Input id="fee" name="fee" type="number" step="0.01" min="0" required />
              </Field>
            </FieldGroup>
            <SubmitButton className="w-fit">Add</SubmitButton>
          </ActionForm>
        </CardContent>
      </Card>
    </div>
  );
}
