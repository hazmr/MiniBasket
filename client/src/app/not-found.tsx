import { SearchX } from "lucide-react";
import Link from "next/link";
import { EmptyState } from "@/components/empty-state";
import { Button } from "@/components/ui/button";

export default function NotFound() {
  return (
    <EmptyState icon={SearchX} title="Page not found" description="The page or product you're looking for doesn't exist.">
      <Button asChild>
        <Link href="/">Back to the shop</Link>
      </Button>
    </EmptyState>
  );
}
