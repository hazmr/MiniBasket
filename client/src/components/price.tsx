import { cn } from "@/lib/utils";
import { money } from "@/lib/format";

type Props = {
  price: number;
  discountPrice: number | null;
  className?: string;
};

export function Price({ price, discountPrice, className }: Props) {
  if (discountPrice === null) return <span className={cn("font-semibold", className)}>{money(price)}</span>;

  return (
    <span className={cn("flex items-baseline gap-2", className)}>
      <span className="font-semibold text-destructive">{money(discountPrice)}</span>
      <span className="text-[0.8em] text-muted-foreground line-through">{money(price)}</span>
    </span>
  );
}
