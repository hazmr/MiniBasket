import { CircleCheck, CreditCard, X } from "lucide-react";
import { Suspense } from "react";
import { cancelOrder, payOrder } from "@/actions/orders";
import { ActionButton } from "@/components/action-button";
import { ConfirmActionButton } from "@/components/confirm-action-button";
import { Loading } from "@/components/loading";
import { OrderDetails } from "@/components/order-details";
import { OrderProgress } from "@/components/order-progress";
import { PageHeader } from "@/components/page-header";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { getOrNotFound } from "@/lib/api";
import { single } from "@/lib/format";
import { requireUser } from "@/lib/session";
import type { Order } from "@/lib/types";

const NOTICES: Record<string, { title: string; description: string }> = {
  placed: { title: "Thank you! Your order was placed.", description: "Pay now to confirm it." },
  paid: { title: "Payment received, thank you!", description: "We'll ship your order soon." },
  cancelled: { title: "Your order was cancelled.", description: "Any payment has been refunded." },
};

export default function OrderPage({ params, searchParams }: PageProps<"/orders/[id]">) {
  return (
    <Suspense fallback={<Loading />}>
      <CustomerOrder params={params} searchParams={searchParams} />
    </Suspense>
  );
}

async function CustomerOrder({ params, searchParams }: Pick<PageProps<"/orders/[id]">, "params" | "searchParams">) {
  const { id } = await params;
  await requireUser(`/orders/${id}`);
  const notice = NOTICES[single((await searchParams).notice) ?? ""];
  const order = await getOrNotFound<Order>(`/orders/${id}`);

  const canPay = order.status === "PENDING" && order.paymentStatus === "UNPAID";
  const canCancel = order.status === "PENDING" || order.status === "CONFIRMED";

  return (
    <div className="flex flex-col gap-6">
      {notice && (
        <Alert className="border-primary/30 bg-accent text-accent-foreground">
          <CircleCheck />
          <AlertTitle>{notice.title}</AlertTitle>
          <AlertDescription className="text-accent-foreground/80">{notice.description}</AlertDescription>
        </Alert>
      )}
      <PageHeader
        title={<>Order <span className="font-mono">{order.orderNumber}</span></>}
        actions={
          <>
            {canPay && (
              <ActionButton action={payOrder.bind(null, order.id)} variant="default">
                <CreditCard data-icon="inline-start" />
                Pay now (demo)
              </ActionButton>
            )}
            {canCancel && (
              <ConfirmActionButton
                action={cancelOrder.bind(null, order.id)}
                variant="destructive"
                title={`Cancel order ${order.orderNumber}?`}
                description={order.paymentStatus === "PAID" ? "Your payment will be refunded and the items go back into stock." : "The items go back into stock."}
                confirmLabel="Cancel order"
              >
                <X data-icon="inline-start" />
                Cancel order
              </ConfirmActionButton>
            )}
          </>
        }
      />
      <OrderProgress status={order.status} />
      <OrderDetails order={order} />
    </div>
  );
}
