import { Check, CreditCard, Package, Truck, X } from "lucide-react";
import { cn } from "@/lib/utils";
import type { OrderStatus } from "@/lib/types";

const STEPS = [
  { status: "PENDING", label: "Placed", icon: Package },
  { status: "CONFIRMED", label: "Paid", icon: CreditCard },
  { status: "SHIPPED", label: "Shipped", icon: Truck },
  { status: "DELIVERED", label: "Delivered", icon: Check },
] as const;

export function OrderProgress({ status }: { status: OrderStatus }) {
  if (status === "CANCELLED") {
    return (
      <div className="flex items-center gap-3 rounded-xl border bg-card p-4 text-sm text-muted-foreground">
        <span className="flex size-8 items-center justify-center rounded-full bg-muted"><X className="size-4" /></span>
        This order was cancelled. Any payment was refunded and the items went back into stock.
      </div>
    );
  }

  const current = STEPS.findIndex((step) => step.status === status);

  return (
    <ol className="grid grid-cols-4 rounded-xl border bg-card p-4" aria-label="Order progress">
      {STEPS.map((step, index) => {
        const done = index <= current;
        const Icon = step.icon;
        return (
          <li key={step.status} className="relative flex flex-col items-center gap-2 text-center" aria-current={index === current ? "step" : undefined}>
            {index > 0 && (
              <span
                aria-hidden
                className={cn("absolute top-4 right-1/2 h-0.5 w-full -translate-y-1/2", index <= current ? "bg-primary" : "bg-border")}
              />
            )}
            <span
              className={cn(
                "relative flex size-8 items-center justify-center rounded-full border-2 bg-card",
                done ? "border-primary bg-primary text-primary-foreground" : "border-border text-muted-foreground",
              )}
            >
              <Icon className="size-4" />
            </span>
            <span className={cn("text-xs font-medium", done ? "text-foreground" : "text-muted-foreground")}>{step.label}</span>
          </li>
        );
      })}
    </ol>
  );
}
