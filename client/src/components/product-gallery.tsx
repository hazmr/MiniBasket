"use client";

import { useState } from "react";
import { cn } from "@/lib/utils";
import type { ProductImage as Image } from "@/lib/types";
import { ProductImage } from "./product-image";

export function ProductGallery({ images, alt }: { images: Image[]; alt: string }) {
  const [selected, setSelected] = useState(0);
  const main = images[selected] ?? images[0];

  return (
    <div className="flex flex-col gap-3">
      <ProductImage src={main?.imageUrl ?? null} alt={alt} className="rounded-2xl border" />
      {images.length > 1 && (
        <div className="grid grid-cols-5 gap-2">
          {images.map((image, index) => (
            <button
              key={image.id}
              type="button"
              onClick={() => setSelected(index)}
              aria-label={`Show image ${index + 1} of ${images.length}`}
              aria-pressed={index === selected}
              className={cn(
                "rounded-lg ring-offset-2 ring-offset-background transition-opacity",
                index === selected ? "ring-2 ring-primary" : "opacity-70 hover:opacity-100",
              )}
            >
              <ProductImage src={image.imageUrl} alt="" className="border" />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
