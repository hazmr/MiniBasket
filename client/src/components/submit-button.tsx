"use client";

import type { ComponentProps } from "react";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useFormPending } from "./action-form";

export function SubmitButton({ children, disabled, size, ...props }: ComponentProps<typeof Button>) {
  const pending = useFormPending();
  const iconOnly = typeof size === "string" && size.startsWith("icon");

  return (
    <Button type="submit" disabled={pending || disabled} size={size} {...props}>
      {pending && <Spinner data-icon={iconOnly ? undefined : "inline-start"} />}
      {pending && iconOnly ? null : children}
    </Button>
  );
}
