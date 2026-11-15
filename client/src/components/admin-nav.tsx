"use client";

import { Award, LayoutDashboard, Package, PackageSearch, Tags, TicketPercent, Truck } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const LINKS = [
  { href: "/admin", label: "Overview", icon: LayoutDashboard },
  { href: "/admin/orders", label: "Orders", icon: Package },
  { href: "/admin/products", label: "Products", icon: PackageSearch },
  { href: "/admin/categories", label: "Categories", icon: Tags },
  { href: "/admin/brands", label: "Brands", icon: Award },
  { href: "/admin/coupons", label: "Coupons", icon: TicketPercent },
  { href: "/admin/shipping", label: "Shipping", icon: Truck },
];

export function AdminNav() {
  return <AdminNavLinks pathname={usePathname()} />;
}

export function AdminNavLinks({ pathname }: { pathname: string }) {
  return (
    <nav className="flex gap-1 overflow-x-auto md:flex-col">
      {LINKS.map(({ href, label, icon: Icon }) => {
        const active = href === "/admin" ? pathname === "/admin" : pathname.startsWith(href);
        return (
          <Button
            key={href}
            variant="ghost"
            asChild
            className={cn("justify-start", active && "bg-accent text-accent-foreground hover:bg-accent")}
          >
            <Link href={href}>
              <Icon data-icon="inline-start" />
              {label}
            </Link>
          </Button>
        );
      })}
    </nav>
  );
}
