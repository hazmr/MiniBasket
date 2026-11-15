import { Suspense } from "react";
import { AdminNav, AdminNavLinks } from "@/components/admin-nav";

export default function AdminLayout({ children }: LayoutProps<"/admin">) {
  return (
    <div className="grid gap-8 md:grid-cols-[200px_1fr]">
      <aside className="flex flex-col gap-3">
        <p className="px-2.5 text-xs font-semibold tracking-wide text-muted-foreground uppercase">Admin</p>
        <Suspense fallback={<AdminNavLinks pathname="" />}>
          <AdminNav />
        </Suspense>
      </aside>
      <div className="min-w-0">{children}</div>
    </div>
  );
}
