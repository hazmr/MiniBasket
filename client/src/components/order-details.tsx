import { MapPin, Truck } from "lucide-react";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { date, money } from "@/lib/format";
import type { Order } from "@/lib/types";
import { StatusBadge } from "./status-badge";

export function OrderDetails({ order }: { order: Order }) {
  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <Card className="lg:col-span-2">
        <CardHeader>
          <CardTitle>Items</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Product</TableHead>
                <TableHead className="text-right">Price</TableHead>
                <TableHead className="text-right">Qty</TableHead>
                <TableHead className="text-right">Total</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {order.items.map((item) => (
                <TableRow key={item.productId}>
                  <TableCell>
                    <Link href={`/products/${item.productId}`} className="font-medium hover:underline">
                      {item.productName}
                    </Link>
                  </TableCell>
                  <TableCell className="text-right">{money(item.unitPrice)}</TableCell>
                  <TableCell className="text-right">{item.quantity}</TableCell>
                  <TableCell className="text-right font-medium">{money(item.lineTotal)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Card className="h-fit">
        <CardHeader>
          <CardTitle>Summary</CardTitle>
          <div className="flex gap-2">
            <StatusBadge status={order.status} />
            <StatusBadge status={order.paymentStatus} />
          </div>
        </CardHeader>
        <CardContent className="flex flex-col gap-4 text-sm">
          <p className="text-muted-foreground">Placed {date(order.createdAt)}</p>
          <dl className="grid grid-cols-[1fr_auto] gap-y-2">
            <dt className="text-muted-foreground">Subtotal</dt>
            <dd className="text-right">{money(order.subtotal)}</dd>
            {order.couponCode && (
              <>
                <dt className="text-muted-foreground">Coupon <span className="font-mono">{order.couponCode}</span></dt>
                <dd className="text-right text-primary">−{money(order.discountAmount)}</dd>
              </>
            )}
            <dt className="text-muted-foreground">{order.shippingMethod}</dt>
            <dd className="text-right">{money(order.shippingFee)}</dd>
          </dl>
          <Separator />
          <div className="flex justify-between text-base font-semibold">
            <span>Total</span>
            <span>{money(order.total)}</span>
          </div>
          <Separator />
          <p className="flex gap-2">
            <MapPin className="size-4 shrink-0 text-muted-foreground" />
            {order.shippingStreet}, {order.shippingCity}
          </p>
          {order.trackingNumber && (
            <p className="flex gap-2">
              <Truck className="size-4 shrink-0 text-muted-foreground" />
              Tracking <span className="font-mono">{order.trackingNumber}</span>
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
