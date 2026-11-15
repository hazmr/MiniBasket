import { MapPin, Trash2 } from "lucide-react";
import { Suspense } from "react";
import { addAddress, deleteAddress, makeDefaultAddress } from "@/actions/account";
import { ActionButton } from "@/components/action-button";
import { ConfirmActionButton } from "@/components/confirm-action-button";
import { ActionForm } from "@/components/action-form";
import { AddressFields } from "@/components/address-fields";
import { Loading } from "@/components/loading";
import { PageHeader } from "@/components/page-header";
import { SubmitButton } from "@/components/submit-button";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Separator } from "@/components/ui/separator";
import { api } from "@/lib/api";
import { date } from "@/lib/format";
import { requireUser } from "@/lib/session";
import type { Address } from "@/lib/types";

export default function AccountPage() {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="Your account" />
      <Suspense fallback={<Loading />}>
        <Account />
      </Suspense>
    </div>
  );
}

async function Account() {
  const user = await requireUser("/account");
  const addresses = await api.get<Address[]>("/addresses");

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <Card className="h-fit">
        <CardContent className="flex items-center gap-4">
          <Avatar className="size-12">
            <AvatarFallback className="bg-accent text-lg text-accent-foreground">{user.email[0].toUpperCase()}</AvatarFallback>
          </Avatar>
          <div className="flex min-w-0 flex-col gap-1">
            <p className="truncate font-medium">{user.email}</p>
            <p className="text-sm text-muted-foreground">Member since {date(user.createdAt)}</p>
            <Badge variant="secondary">{user.role === "ADMIN" ? "Administrator" : "Customer"}</Badge>
          </div>
        </CardContent>
      </Card>

      <Card className="lg:col-span-2">
        <CardHeader>
          <CardTitle>Addresses</CardTitle>
          <CardDescription>Your default address is pre-selected at checkout.</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-6">
          {addresses.length === 0 && <p className="text-sm text-muted-foreground">No addresses yet.</p>}
          <ul className="flex flex-col gap-3">
            {addresses.map((address) => (
              <li key={address.id} className="flex flex-wrap items-center gap-3 rounded-lg border p-3 text-sm">
                <MapPin className="size-4 text-muted-foreground" />
                <span className="flex-1">
                  {address.street}, {address.city}
                </span>
                {address.defaultAddress ? (
                  <Badge>Default</Badge>
                ) : (
                  <ActionButton action={makeDefaultAddress.bind(null, address)} size="sm">Make default</ActionButton>
                )}
                <ConfirmActionButton
                  action={deleteAddress.bind(null, address.id)}
                  variant="ghost"
                  size="icon-sm"
                  aria-label="Delete address"
                  title="Delete this address?"
                  description={`${address.street}, ${address.city} will be removed. Past orders keep their own copy.`}
                  confirmLabel="Delete"
                >
                  <Trash2 />
                </ConfirmActionButton>
              </li>
            ))}
          </ul>

          <Separator />

          <ActionForm action={addAddress} className="flex flex-col gap-5">
            <h3 className="font-medium">Add an address</h3>
            <FieldGroup className="grid gap-4 sm:grid-cols-2">
              <AddressFields />
            </FieldGroup>
            <Field orientation="horizontal">
              <Checkbox id="defaultAddress" name="defaultAddress" />
              <FieldLabel htmlFor="defaultAddress" className="font-normal">Use as my default address</FieldLabel>
            </Field>
            <SubmitButton className="w-fit">Save address</SubmitButton>
          </ActionForm>
        </CardContent>
      </Card>
    </div>
  );
}
