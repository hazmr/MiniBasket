import type { LucideIcon } from "lucide-react";
import { Ban, CircleCheck, Clock, PackageCheck, Truck } from "lucide-react";
import Link from "next/link";
import { Suspense } from "react";
import { Loading } from "@/components/loading";
import { OrderTable } from "@/components/order-table";
import { PageHeader } from "@/components/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { api } from "@/lib/api";
import { requireAdmin } from "@/lib/session";
import type { OrderStatus, OrderSummary, Page } from "@/lib/types";

const TILES: { status: OrderStatus; label: string; hint: string; icon: LucideIcon }[] = [
  { status: "PENDING", label: "Waiting for payment", hint: "Placed, not paid yet", icon: Clock },
  { status: "CONFIRMED", label: "Ready to ship", hint: "Paid, needs a tracking number", icon: PackageCheck },
  { status: "SHIPPED", label: "On the way", hint: "Mark delivered when they arrive", icon: Truck },
  { status: "DELIVERED", label: "Delivered", hint: "Done", icon: CircleCheck },
  { status: "CANCELLED", label: "Cancelled", hint: "Stock was put back", icon: Ban },
];

export default function AdminOverviewPage() {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="Overview" description="What needs your attention today." />
      <Suspense fallback={<Loading />}>
        <Overview />
      </Suspense>
    </div>
  );
}

async function Overview() {
  await requireAdmin();
  const [counts, latest] = await Promise.all([
    Promise.all(TILES.map((tile) => api.get<Page<OrderSummary>>(`/admin/orders?status=${tile.status}&size=1`))),
    api.get<Page<OrderSummary>>("/admin/orders?size=5"),
  ]);

  return (
    <>
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-5">
        {TILES.map((tile, index) => {
          const Icon = tile.icon;
          const count = counts[index].totalElements;
          const needsAction = tile.status === "CONFIRMED" && count > 0;
          return (
            <Link key={tile.status} href={`/admin/orders?status=${tile.status}`} className="group">
              <Card className={needsAction ? "h-full border-primary ring-1 ring-primary" : "h-full transition-colors group-hover:border-primary/50"}>
                <CardContent className="flex flex-col gap-2">
                  <Icon className={needsAction ? "size-5 text-primary" : "size-5 text-muted-foreground"} />
                  <span className="text-3xl font-semibold tabular-nums">{count}</span>
                  <span className="text-sm font-medium">{tile.label}</span>
                  <span className="text-xs text-muted-foreground">{tile.hint}</span>
                </CardContent>
              </Card>
            </Link>
          );
        })}
      </div>

      <div className="flex items-center justify-between">
        <h2 className="font-heading text-lg font-semibold">Latest orders</h2>
        <Button variant="outline" size="sm" asChild>
          <Link href="/admin/orders">All orders</Link>
        </Button>
      </div>
      {latest.content.length === 0 ? (
        <p className="text-sm text-muted-foreground">No orders yet.</p>
      ) : (
        <OrderTable orders={latest.content} hrefFor={(id) => `/admin/orders/${id}`} />
      )}
    </>
  );
}
