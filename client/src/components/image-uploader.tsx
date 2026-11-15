"use client";

import { ImageUp } from "lucide-react";
import { useRef, useState, useSyncExternalStore, type DragEvent } from "react";
import { toast } from "sonner";
import { attachProductImage, requestImageUpload } from "@/actions/admin";
import { Progress } from "@/components/ui/progress";
import { cn } from "@/lib/utils";

const ACCEPTED_TYPES = ["image/jpeg", "image/png", "image/webp"];
const MAX_BYTES = 5 * 1024 * 1024;

const subscribeNothing = () => () => {};

export function ImageUploader({ productId }: { productId: number }) {
  const interactive = useSyncExternalStore(subscribeNothing, () => true, () => false);
  const inputRef = useRef<HTMLInputElement>(null);
  const [progress, setProgress] = useState<number | null>(null);
  const [dragging, setDragging] = useState(false);
  const uploading = progress !== null;

  async function upload(file: File) {
    if (!ACCEPTED_TYPES.includes(file.type)) {
      toast.error("Choose a JPEG, PNG or WebP image");
      return;
    }
    if (file.size > MAX_BYTES) {
      toast.error("Images can be at most 5 MB");
      return;
    }

    setProgress(0);
    try {
      const signed = await requestImageUpload(productId, file.type, file.size);
      if ("error" in signed) throw new Error(signed.error);

      await putFile(signed.uploadUrl, file, setProgress);

      const attached = await attachProductImage(productId, signed.key);
      if (attached.error) throw new Error(attached.error);
      toast.success("Image added");
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "The upload failed, please try again");
    } finally {
      setProgress(null);
      if (inputRef.current) inputRef.current.value = "";
    }
  }

  function handleDrop(event: DragEvent<HTMLDivElement>) {
    event.preventDefault();
    setDragging(false);
    const file = event.dataTransfer.files[0];
    if (file && interactive && !uploading) void upload(file);
  }

  return (
    <div
      onDragOver={(event) => {
        event.preventDefault();
        setDragging(true);
      }}
      onDragLeave={() => setDragging(false)}
      onDrop={handleDrop}
      className={cn(
        "flex flex-col items-center gap-3 rounded-xl border-2 border-dashed p-6 text-center transition-colors",
        dragging ? "border-primary bg-accent" : "border-border",
      )}
    >
      <input
        ref={inputRef}
        id="product-image-file"
        type="file"
        accept={ACCEPTED_TYPES.join(",")}
        className="sr-only"
        disabled={!interactive || uploading}
        onChange={(event) => {
          const file = event.target.files?.[0];
          if (file) void upload(file);
        }}
      />
      <ImageUp className="size-8 text-muted-foreground" />
      {uploading ? (
        <div className="flex w-full max-w-xs flex-col gap-2">
          <Progress value={progress} aria-label="Upload progress" />
          <p className="text-sm text-muted-foreground tabular-nums">Uploading… {progress}%</p>
        </div>
      ) : (
        <>
          <p className="text-sm">
            Drop an image here, or{" "}
            <label htmlFor="product-image-file" className="cursor-pointer font-medium text-primary hover:underline">
              choose a file
            </label>
          </p>
          <p className="text-xs text-muted-foreground">JPEG, PNG or WebP, up to 5 MB</p>
        </>
      )}
    </div>
  );
}

/** Step 2: the browser sends the file straight to the bucket. XHR because fetch can't report upload progress. */
function putFile(url: string, file: File, onProgress: (percent: number) => void): Promise<void> {
  return new Promise((resolve, reject) => {
    const request = new XMLHttpRequest();
    request.open("PUT", url);
    request.setRequestHeader("Content-Type", file.type);
    request.upload.onprogress = (event) => {
      if (event.lengthComputable) onProgress(Math.round((event.loaded / event.total) * 100));
    };
    request.onload = () =>
      request.status >= 200 && request.status < 300
        ? resolve()
        : reject(new Error(`The storage refused the upload (HTTP ${request.status})`));
    request.onerror = () => reject(new Error("Couldn't reach the image storage. Check the bucket's CORS rule."));
    request.send(file);
  });
}
