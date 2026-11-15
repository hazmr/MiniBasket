import { Package } from "lucide-react";
import Link from "next/link";
import { Suspense } from "react";
import { EmptyState } from "@/components/empty-state";
import { Loading } from "@/components/loading";
import { OrderTable } from "@/components/order-table";
import { PageHeader } from "@/components/page-header";
import { Pagination } from "@/components/pagination";
import { Button } from "@/components/ui/button";
import { api } from "@/lib/api";
import { single } from "@/lib/format";
import { requireAdmin } from "@/lib/session";
import type { OrderStatus, OrderSummary, Page } from "@/lib/types";

const STATUSES: OrderStatus[] = ["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"];

export default function AdminOrdersPage({ searchParams }: PageProps<"/admin/orders">) {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="Orders" description="Every order from every customer." />
      <Suspense fallback={<Loading />}>
        <AdminOrders searchParams={searchParams} />
      </Suspense>
    </div>
  );
}

async function AdminOrders({ searchParams }: { searchParams: PageProps<"/admin/orders">["searchParams"] }) {
  await requireAdmin();
  const params = await searchParams;
  const status = single(params.status) ?? "";
  const page = Number(single(params.page) ?? 0);

  const query = new URLSearchParams({ page: String(page), size: "20" });
  if (status) query.set("status", status);
  const orders = await api.get<Page<OrderSummary>>(`/admin/orders?${query}`);

  return (
    <>
      <div className="flex flex-wrap gap-2">
        {["", ...STATUSES].map((value) => (
          <Button key={value || "ALL"} size="sm" variant={value === status ? "default" : "outline"} asChild>
            <Link href={value ? `/admin/orders?status=${value}` : "/admin/orders"}>
              {value ? value.charAt(0) + value.slice(1).toLowerCase() : "All"}
            </Link>
          </Button>
        ))}
      </div>
      {orders.content.length === 0 ? (
        <EmptyState icon={Package} title="No orders here" description="Orders with this status will show up here." />
      ) : (
        <OrderTable orders={orders.content} hrefFor={(id) => `/admin/orders/${id}`} />
      )}
      <Pagination
        page={orders.page}
        totalPages={orders.totalPages}
        hrefFor={(target) => `/admin/orders?${new URLSearchParams({ status, page: String(target) })}`}
      />
    </>
  );
}
