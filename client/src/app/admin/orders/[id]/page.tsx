import { Suspense } from "react";
import { updateOrderStatus } from "@/actions/admin";
import { ActionForm } from "@/components/action-form";
import { Loading } from "@/components/loading";
import { OrderDetails } from "@/components/order-details";
import { OrderProgress } from "@/components/order-progress";
import { PageHeader } from "@/components/page-header";
import { SubmitButton } from "@/components/submit-button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { NativeSelect, NativeSelectOption } from "@/components/ui/native-select";
import { getOrNotFound } from "@/lib/api";
import { requireAdmin } from "@/lib/session";
import type { Order } from "@/lib/types";

export default function AdminOrderPage({ params }: PageProps<"/admin/orders/[id]">) {
  return (
    <Suspense fallback={<Loading />}>
      <AdminOrder params={params} />
    </Suspense>
  );
}

async function AdminOrder({ params }: { params: PageProps<"/admin/orders/[id]">["params"] }) {
  await requireAdmin();
  const { id } = await params;
  const order = await getOrNotFound<Order>(`/admin/orders/${id}`);
  const isFinished = order.status === "DELIVERED" || order.status === "CANCELLED";

  return (
    <div className="flex flex-col gap-6">
      <PageHeader title={<>Order <span className="font-mono">{order.orderNumber}</span></>} />

      {!isFinished && (
        <Card>
          <CardHeader>
            <CardTitle>Update status</CardTitle>
            <CardDescription>Orders go Confirmed → Shipped → Delivered. Cancelling puts the stock back.</CardDescription>
          </CardHeader>
          <CardContent>
            <ActionForm action={updateOrderStatus.bind(null, order.id)} className="flex flex-col gap-4">
              <div className="flex flex-wrap items-end gap-3">
                <Field className="w-48">
                  <FieldLabel htmlFor="status">New status</FieldLabel>
                  <NativeSelect id="status" name="status" defaultValue={order.status === "SHIPPED" ? "DELIVERED" : "SHIPPED"}>
                    <NativeSelectOption value="SHIPPED">Shipped</NativeSelectOption>
                    <NativeSelectOption value="DELIVERED">Delivered</NativeSelectOption>
                    <NativeSelectOption value="CANCELLED">Cancelled</NativeSelectOption>
                  </NativeSelect>
                </Field>
                <Field className="w-64">
                  <FieldLabel htmlFor="trackingNumber">Tracking number</FieldLabel>
                  <Input id="trackingNumber" name="trackingNumber" placeholder="Required for Shipped" />
                </Field>
                <SubmitButton>Update order</SubmitButton>
              </div>
            </ActionForm>
          </CardContent>
        </Card>
      )}

      <OrderProgress status={order.status} />
      <OrderDetails order={order} />
    </div>
  );
}
