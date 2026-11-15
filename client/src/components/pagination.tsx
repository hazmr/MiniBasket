import { ChevronLeft, ChevronRight } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";

type Props = {
  page: number;
  totalPages: number;
  hrefFor: (page: number) => string;
};

export function Pagination({ page, totalPages, hrefFor }: Props) {
  if (totalPages <= 1) return null;

  return (
    <nav className="flex items-center justify-center gap-3 text-sm">
      <Button variant="outline" size="sm" asChild>
        {page > 0 ? (
          <Link href={hrefFor(page - 1)}><ChevronLeft data-icon="inline-start" />Previous</Link>
        ) : (
          <span aria-disabled className="pointer-events-none opacity-50"><ChevronLeft data-icon="inline-start" />Previous</span>
        )}
      </Button>
      <span className="text-muted-foreground">Page {page + 1} of {totalPages}</span>
      <Button variant="outline" size="sm" asChild>
        {page + 1 < totalPages ? (
          <Link href={hrefFor(page + 1)}>Next<ChevronRight data-icon="inline-end" /></Link>
        ) : (
          <span aria-disabled className="pointer-events-none opacity-50">Next<ChevronRight data-icon="inline-end" /></span>
        )}
      </Button>
    </nav>
  );
}
