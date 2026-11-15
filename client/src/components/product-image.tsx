import { ImageOff } from "lucide-react";
import Image from "next/image";
import { cn } from "@/lib/utils";

type Props = {
  src: string | null;
  alt: string;
  className?: string;
};

export function ProductImage({ src, alt, className }: Props) {
  return (
    <div className={cn("relative aspect-square overflow-hidden rounded-lg bg-muted", className)}>
      {src ? (
        <Image src={src} alt={alt} fill unoptimized className="object-cover" />
      ) : (
        <div className="flex h-full items-center justify-center text-muted-foreground">
          <ImageOff className="size-8" />
        </div>
      )}
    </div>
  );
}
