import { Trash2 } from "lucide-react";
import { Suspense } from "react";
import { createCoupon, deleteCoupon } from "@/actions/admin";
import { ActionForm } from "@/components/action-form";
import { ConfirmActionButton } from "@/components/confirm-action-button";
import { Loading } from "@/components/loading";
import { PageHeader } from "@/components/page-header";
import { SubmitButton } from "@/components/submit-button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldDescription, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { NativeSelect, NativeSelectOption } from "@/components/ui/native-select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { api } from "@/lib/api";
import { date, money } from "@/lib/format";
import { requireAdmin } from "@/lib/session";
import type { Coupon, Page } from "@/lib/types";

export default function AdminCouponsPage() {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="Coupons" description="Codes customers can enter at checkout." />
      <Suspense fallback={<Loading />}>
        <Coupons />
      </Suspense>
    </div>
  );
}

async function Coupons() {
  await requireAdmin();
  const coupons = await api.get<Page<Coupon>>("/admin/coupons?size=100");

  return (
    <div className="grid gap-6 xl:grid-cols-3">
      <div className="overflow-hidden rounded-xl border bg-card xl:col-span-2">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="pl-4">Code</TableHead>
              <TableHead>Discount</TableHead>
              <TableHead>Expires</TableHead>
              <TableHead className="pr-4" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {coupons.content.map((coupon) => (
              <TableRow key={coupon.id}>
                <TableCell className="pl-4 font-mono font-medium">{coupon.code}</TableCell>
                <TableCell>
                  {coupon.discountType === "PERCENTAGE" ? `${coupon.discountValue}% off` : `${money(coupon.discountValue)} off`}
                </TableCell>
                <TableCell>
                  {coupon.expired ? (
                    <Badge variant="destructive">Expired</Badge>
                  ) : coupon.expiresAt ? (
                    date(coupon.expiresAt)
                  ) : (
                    <span className="text-muted-foreground">Never</span>
                  )}
                </TableCell>
                <TableCell className="pr-4 text-right">
                  <ConfirmActionButton
                    action={deleteCoupon.bind(null, coupon.id)}
                    variant="ghost"
                    size="icon-sm"
                    aria-label={`Delete ${coupon.code}`}
                    title={`Delete coupon ${coupon.code}?`}
                    description="Coupons already used by an order can't be deleted. Set an expiry date instead."
                    confirmLabel="Delete"
                  >
                    <Trash2 />
                  </ConfirmActionButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <Card className="h-fit">
        <CardHeader>
          <CardTitle>New coupon</CardTitle>
        </CardHeader>
        <CardContent>
          <ActionForm action={createCoupon} className="flex flex-col gap-5">
            <FieldGroup className="gap-4">
              <Field>
                <FieldLabel htmlFor="code">Code</FieldLabel>
                <Input id="code" name="code" required pattern="[A-Za-z0-9_\-]+" className="font-mono uppercase" />
              </Field>
              <Field>
                <FieldLabel htmlFor="discountType">Type</FieldLabel>
                <NativeSelect id="discountType" name="discountType">
                  <NativeSelectOption value="PERCENTAGE">Percentage off</NativeSelectOption>
                  <NativeSelectOption value="FIXED">Fixed amount off</NativeSelectOption>
                </NativeSelect>
              </Field>
              <Field>
                <FieldLabel htmlFor="discountValue">Value</FieldLabel>
                <Input id="discountValue" name="discountValue" type="number" step="0.01" min="0.01" required />
              </Field>
              <Field>
                <FieldLabel htmlFor="expiresAt">Expires</FieldLabel>
                <Input id="expiresAt" name="expiresAt" type="datetime-local" />
                <FieldDescription>Leave empty for a coupon that never expires.</FieldDescription>
              </Field>
            </FieldGroup>
            <SubmitButton className="w-fit">Create coupon</SubmitButton>
          </ActionForm>
        </CardContent>
      </Card>
    </div>
  );
}
