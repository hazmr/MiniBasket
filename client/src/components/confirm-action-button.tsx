"use client";

import { useId, type ComponentProps, type ReactNode } from "react";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import type { FormAction } from "@/lib/action-state";
import { ActionForm } from "./action-form";

type Props = Omit<ComponentProps<typeof Button>, "action"> & {
  action: FormAction;
  title: string;
  description: string;
  confirmLabel: string;
  children: ReactNode;
};

export function ConfirmActionButton({ action, title, description, confirmLabel, children, variant = "outline", ...buttonProps }: Props) {
  const formId = useId();

  return (
    <ActionForm id={formId} action={action} errorsAs="toast" className="contents">
      <AlertDialog>
        <AlertDialogTrigger asChild>
          <Button type="button" variant={variant} {...buttonProps}>{children}</Button>
        </AlertDialogTrigger>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{title}</AlertDialogTitle>
            <AlertDialogDescription>{description}</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Go back</AlertDialogCancel>
            <AlertDialogAction type="submit" form={formId} variant="destructive">{confirmLabel}</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </ActionForm>
  );
}
