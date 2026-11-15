import { Badge } from "@/components/ui/badge";
import type { OrderStatus, PaymentStatus } from "@/lib/types";

const amber = "border-amber-200 bg-amber-50 text-amber-700 dark:border-amber-900 dark:bg-amber-950 dark:text-amber-300";
const sky = "border-sky-200 bg-sky-50 text-sky-700 dark:border-sky-900 dark:bg-sky-950 dark:text-sky-300";
const green = "border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-900 dark:bg-emerald-950 dark:text-emerald-300";
const indigo = "border-indigo-200 bg-indigo-50 text-indigo-700 dark:border-indigo-900 dark:bg-indigo-950 dark:text-indigo-300";
const grey = "border-border bg-muted text-muted-foreground";

const colors: Record<OrderStatus | PaymentStatus, string> = {
  PENDING: amber,
  UNPAID: amber,
  CONFIRMED: sky,
  PAID: green,
  SHIPPED: indigo,
  DELIVERED: green,
  CANCELLED: grey,
  REFUNDED: grey,
};

export function StatusBadge({ status }: { status: OrderStatus | PaymentStatus }) {
  return (
    <Badge variant="outline" className={colors[status]}>
      {status.charAt(0) + status.slice(1).toLowerCase()}
    </Badge>
  );
}
