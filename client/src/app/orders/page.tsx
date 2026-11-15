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
import { requireUser } from "@/lib/session";
import type { OrderSummary, Page } from "@/lib/types";

export default function OrdersPage({ searchParams }: PageProps<"/orders">) {
  return (
    <div className="flex flex-col gap-6">
      <PageHeader title="Your orders" description="Track, pay or cancel your orders." />
      <Suspense fallback={<Loading />}>
        <OrderList searchParams={searchParams} />
      </Suspense>
    </div>
  );
}

async function OrderList({ searchParams }: { searchParams: PageProps<"/orders">["searchParams"] }) {
  await requireUser("/orders");
  const page = Number(single((await searchParams).page) ?? 0);
  const orders = await api.get<Page<OrderSummary>>(`/orders?page=${page}&size=10`);

  if (orders.content.length === 0) {
    return (
      <EmptyState icon={Package} title="No orders yet" description="When you place an order it will show up here.">
        <Button asChild>
          <Link href="/">Start shopping</Link>
        </Button>
      </EmptyState>
    );
  }

  return (
    <>
      <OrderTable orders={orders.content} hrefFor={(id) => `/orders/${id}`} />
      <Pagination page={orders.page} totalPages={orders.totalPages} hrefFor={(target) => `/orders?page=${target}`} />
    </>
  );
}
