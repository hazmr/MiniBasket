"use client";

import { CircleAlert } from "lucide-react";
import { useRouter } from "next/navigation";
import { createContext, startTransition, use, useActionState, useEffect, useRef, type FormEvent, type ReactNode } from "react";
import { toast } from "sonner";
import { Alert, AlertDescription } from "@/components/ui/alert";
import type { FormAction } from "@/lib/action-state";

const PendingContext = createContext(false);

export function useFormPending() {
  return use(PendingContext);
}

type Props = {
  action: FormAction;
  children: ReactNode;
  className?: string;
  id?: string;
  errorsAs?: "inline" | "toast";
};

export function ActionForm({ action, children, className, id, errorsAs = "inline" }: Props) {
  const [state, formAction, pending] = useActionState(action, {});
  const formRef = useRef<HTMLFormElement>(null);
  const submitted = useRef(false);
  const router = useRouter();

  useEffect(() => {
    if (!submitted.current) return;
    submitted.current = false;

    if (state.error) {
      if (errorsAs === "toast") toast.error(state.error);
      return;
    }
    if (state.message) {
      const { href, hrefLabel } = state;
      toast.success(state.message, href && hrefLabel ? { action: { label: hrefLabel, onClick: () => router.push(href) } } : undefined);
    }
    formRef.current?.reset();
  }, [state, errorsAs, router]);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    submitted.current = true;
    const formData = new FormData(event.currentTarget);
    startTransition(() => formAction(formData));
  }

  return (
    <PendingContext value={pending}>
      <form ref={formRef} id={id} action={formAction} onSubmit={handleSubmit} className={className}>
        {children}
        {errorsAs === "inline" && state.error && (
          <Alert variant="destructive">
            <CircleAlert />
            <AlertDescription>{state.error}</AlertDescription>
          </Alert>
        )}
      </form>
    </PendingContext>
  );
}
