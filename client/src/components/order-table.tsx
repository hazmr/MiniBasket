import Link from "next/link";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { date, money } from "@/lib/format";
import type { OrderSummary } from "@/lib/types";
import { StatusBadge } from "./status-badge";

export function OrderTable({ orders, hrefFor }: { orders: OrderSummary[]; hrefFor: (id: number) => string }) {
  return (
    <div className="overflow-hidden rounded-xl border bg-card">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="pl-4">Order</TableHead>
            <TableHead>Date</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Payment</TableHead>
            <TableHead className="pr-4 text-right">Total</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {orders.map((order) => (
            <TableRow key={order.id}>
              <TableCell className="pl-4">
                <Link href={hrefFor(order.id)} className="font-mono font-medium text-primary hover:underline">
                  {order.orderNumber}
                </Link>
              </TableCell>
              <TableCell className="text-muted-foreground">{date(order.createdAt)}</TableCell>
              <TableCell><StatusBadge status={order.status} /></TableCell>
              <TableCell><StatusBadge status={order.paymentStatus} /></TableCell>
              <TableCell className="pr-4 text-right font-medium">{money(order.total)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
