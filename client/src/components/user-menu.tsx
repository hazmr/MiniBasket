"use client";

import { Heart, LayoutDashboard, LogOut, Package, ShoppingCart, User } from "lucide-react";
import Link from "next/link";
import { startTransition } from "react";
import { logout } from "@/actions/auth";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export function UserMenu({ email, isAdmin }: { email: string; isAdmin: boolean }) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon" className="rounded-full" aria-label="Account menu">
          <Avatar className="size-8">
            <AvatarFallback className="bg-accent text-accent-foreground">{email[0].toUpperCase()}</AvatarFallback>
          </Avatar>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-56">
        <DropdownMenuLabel className="flex flex-col">
          <span className="text-xs font-normal text-muted-foreground">Signed in as</span>
          <span className="truncate">{email}</span>
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuGroup>
          {isAdmin && (
            <DropdownMenuItem asChild>
              <Link href="/admin"><LayoutDashboard />Admin</Link>
            </DropdownMenuItem>
          )}
          <DropdownMenuItem asChild>
            <Link href="/orders"><Package />My orders</Link>
          </DropdownMenuItem>
          <DropdownMenuItem asChild>
            <Link href="/cart"><ShoppingCart />Cart</Link>
          </DropdownMenuItem>
          <DropdownMenuItem asChild>
            <Link href="/wishlist"><Heart />Wishlist</Link>
          </DropdownMenuItem>
          <DropdownMenuItem asChild>
            <Link href="/account"><User />Account</Link>
          </DropdownMenuItem>
        </DropdownMenuGroup>
        <DropdownMenuSeparator />
        <DropdownMenuItem variant="destructive" onSelect={() => startTransition(() => logout())}>
          <LogOut />
          Log out
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
